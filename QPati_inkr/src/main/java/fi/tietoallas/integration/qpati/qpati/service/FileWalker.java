package fi.tietoallas.integration.qpati.qpati.service;

/*-
 * #%L
 * qpati
 * %%
 * Copyright (C) 2017 - 2018 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.text.MessageFormat;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import fi.tietoallas.monitoring.commonmonitoring.MetricService;
import fi.tietoallas.monitoring.commonmonitoring.MetricServicePrometheusImpl;
import fi.tietoallas.monitoring.commonmonitoring.Tag;
import fi.tietoallas.monitoring.commonmonitoring.TaggedLogger;
import fi.tietoallas.incremental.common.commonincremental.domain.MonitoringData;
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import static fi.tietoallas.incremental.common.commonincremental.util.CommonConversionUtils.convertTableName;
import static fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools.toAvroSchema;
import static fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools.toIntegrationAvroSchema;
import static fi.tietoallas.integration.qpati.qpati.QpatiApplication.INTEGRATION_NAME;
import fi.tietoallas.integration.qpati.qpati.domain.HiveTypeAndDataColumnName;
import fi.tietoallas.integration.qpati.qpati.repository.MetadataRepository;

@Service
public class FileWalker {
	
	private static final String PUSH_GATAWAY_PORT = "9091";
	private static final String READY_FLAG = "_TIETOALLAS_VALMIS_";
    //private static final String PREVIOUS_REFS = "/mnt/datadisk/qpati/chroot/previous_refs/";
     private static final String PREVIOUS_REFS = "/mnt/datadisk/qpati/chroot/tmp_previous_refs/";
	// private static final String PREVIOUS_REFS = "/Users/huangyee/Desktop/datalake/repos/integrations/QPati_inkr/src/test/resources/package_service/previous_refs/";
    private static TaggedLogger logger = new TaggedLogger(FileWalker.class, "QPati_inkr");
    
    private Environment environment;
	private MetadataRepository metadataRepository;
	private MetricService metricService;
	private String pushGatawayAddress = "";

	public FileWalker(
			@Autowired MetadataRepository metadataRepository, 
			@Autowired Environment environment) {
		this.metadataRepository = metadataRepository;
		this.environment = environment;
		//if (environment.getProperty("pushgateway.host") != null) {
        //    pushGatawayAddress = environment.getProperty("pushgateway.host") + ":" + PUSH_GATAWAY_PORT;
        //    metricService = new MetricServicePrometheusImpl(pushGatawayAddress);
        //}
	}

	public void readFilesAndSendToKafka(String root, TietoallasKafkaProducer tietoallasKafkaProducer) throws Exception {
		logger.info(Tag.DL_DEFAULT, MessageFormat.format("=== BEGIN: readFilesAndSendToKafka. Ref-file path: {0}", PREVIOUS_REFS));

		List<Path> folders = listFoldersWithReadyFlag(root);

		if (!folders.isEmpty()) {
			logger.info(Tag.DL_STAT_INFO, MessageFormat.format("====== 1st Step => Folders with READY_FLAG: {0, number, integer}", folders.size()));
			for (Path folder : folders) {
				logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== 2nd Step => Start to find CSV files of folder: {0}", folder));
				List<Path> files = listCSVFilesOfFolderWithReadyFlag(folder);
				if (!files.isEmpty()) {
					logger.info(Tag.DL_STAT_INFO, MessageFormat.format("====== 3rd Step => CSV files: {0, number, integer}", files.size()));
					for (Path file : files) {
						String fileName = file.toFile().getName();
						logger.info(Tag.DL_DEFAULT, MessageFormat.format("====== 4th Step => Start to process file: {0}", fileName));
						if (checkFilePermission(file.toFile())) {
							long startTime = System.currentTimeMillis();
							List<String[]> rows = processFile(file.toFile(), PREVIOUS_REFS);
							long endTime = System.currentTimeMillis();
							logger.info(Tag.DL_STAT_INFO, MessageFormat.format("====== 5th Step => File: {0}, Rows: {1, number, integer}, Time Duration: {2, number, long}", fileName, rows.size(), endTime - startTime));
							if (rows != null && rows.size() > 1) {
								String tableName = normalizeFileName(file.toFile());
								List<HiveTypeAndDataColumnName> hiveTypes = metadataRepository.getHiveTypes(INTEGRATION_NAME, tableName);
								SchemaGenerationInfo schemaGenerationInfo = generateSchemaGenerationInfo(hiveTypes, rows.get(0), tableName);
								Schema schema = new Schema.Parser().parse(toIntegrationAvroSchema(INTEGRATION_NAME, schemaGenerationInfo));
								sendToKafka(tietoallasKafkaProducer, convertDataToAvro(rows, schemaGenerationInfo), schema, tableName);
								logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== 6th Step => CSV file {0} has been sent to Kafka.", fileName));
							} else {
								logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== 6th Step => CSV file {0} is either empty or only contains a header.", fileName));
							}
							logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== 7th Step => Start to clean CSV file: {0}", file));
							cleanCSVFile(file, PREVIOUS_REFS);
						} else {
							throw new RuntimeException("CSV File " + file + " doesn't have write permission. Please make sure all the CSV files with the permission 660.");
						}
					};
				} else {
					logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Skip 3rd, 4th, 5th, 6th, 7th Steps => No CSV files found in the folder: {0}", folder));
				}
				logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== 8th Step => Start to clean folder: {0}", folder));
				cleanFolder(folder);
			}
		} else {
			logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Skip 2nd, 3rd, 4th, 5th, 6th, 7th, 8th Steps => No folders with READY_FLAG found from the root: {0}", root));
		}

		logger.info(Tag.DL_DEFAULT, "=== END: readFilesAndSendToKafka");
	}

	public List<Path> listFoldersWithReadyFlag(String root) throws Exception {
		List<Path> foldersWithReadyFlag = new ArrayList<>();

		Stream<Path> walker = Files.walk(Paths.get(root));
		foldersWithReadyFlag = walker
				.filter(f -> f.toFile().isDirectory())
				.filter(f -> Arrays.asList(f.toFile().list()).contains(READY_FLAG))
				.collect(Collectors.toList());
		walker.close();
		
		return foldersWithReadyFlag;
	}

	public List<Path> listCSVFilesOfFolderWithReadyFlag(Path folderWithReadyFlag) throws Exception {
		List<Path> csvFiles = new ArrayList<>();

		Stream<Path> walker = Files.walk(Paths.get(folderWithReadyFlag.toAbsolutePath().toString()));
		csvFiles = walker
				.filter(f -> Files.isRegularFile(f))
				.filter(f -> f.toString().toLowerCase().endsWith(".csv"))
				.filter(f -> !f.getFileName().toString().contains(READY_FLAG))
				.collect(Collectors.toList());
		walker.close();

		return csvFiles;
	}

	public List<String[]> processFile(File file, String prevRefsPath) throws Exception {
		List<String[]> result = new ArrayList<>();

		if (!file.getName().startsWith("Ref_")) {
			result = getAllLines(file);
			logger.debug(Tag.DL_DEFAULT, MessageFormat.format("========= END: Normal data file - processed and deleted: {0}", file.getName()));
		} else {
			FileInputStream newInputStream = null;
			FileInputStream oldInputStream = null;

			File oldRefFile = new File(prevRefsPath + file.getName());
			if (oldRefFile.exists()) { // Get diff only if previous file exists
				newInputStream = new FileInputStream(file);
				oldInputStream = new FileInputStream(oldRefFile);
				result = getDiffLines(oldInputStream, newInputStream);
				logger.debug(Tag.DL_DEFAULT, MessageFormat.format("========= END: processFile (reference file). Old Ref file found. Differences processed: {0}", file.getName()));
			} else {
				logger.debug(Tag.DL_DEFAULT, MessageFormat.format("========= END: processFile (reference file). Old Ref file not found. The whole file processed: {0}", file.getName()));
				result = getAllLines(file);
			}
		}
		
		return result;
	}

	public List<String[]> getDiffLines(InputStream oldStream, InputStream newStream) throws Exception {
		Scanner oldScanner = new Scanner(oldStream);
		Scanner newScanner = new Scanner(newStream);

		List<String> difference = new ArrayList<>();

		String newHeader = newScanner.nextLine();
		String oldHeader = oldScanner.nextLine();
		if (!newHeader.equalsIgnoreCase(oldHeader)) {
			logger.debug(Tag.DL_DEFAULT, "============ getDiffLines: The old CSV header is not equal to the new CSV header.");
		}
		difference.add(newHeader);

		while (oldScanner.hasNextLine() && newScanner.hasNextLine()) {
			String newLine = newScanner.nextLine();
			String oldLine = oldScanner.nextLine();
			if (!newLine.equalsIgnoreCase(oldLine)) {
				difference.add(newLine);
			}
		}

		while (newScanner.hasNextLine()) {
			difference.add(newScanner.nextLine());
		}

		oldScanner.close();
		newScanner.close();

		List<String> diffLinesList = new ArrayList<>();
		List<String[]> differences = new ArrayList<>();

		Stream<String> diffStream = difference.stream();
		diffLinesList = diffStream.filter(l -> l.length() > 0).collect(Collectors.toList());
		diffStream.close();

		differences = trimList(diffLinesList, '\u0009');

		return differences;
	}

	public List<String[]> getAllLines(File file) throws Exception {
		List<String> allLinesList = new ArrayList<>();
		List<String[]> trimmedList = new ArrayList<>();

		Stream<String> fileStream = Files.readAllLines(file.toPath()).stream();
		allLinesList = fileStream.filter(l -> l.length() > 0).collect(Collectors.toList());
		fileStream.close();

		trimmedList = trimList(allLinesList, '\u0009');
		
		return trimmedList;
	}

	public List<String[]> trimList(List<String> origList, Character delimiter) throws Exception {
		List<String[]> trimmedList = new ArrayList<>();

		String[] header = Iterables.toArray(Splitter.on(delimiter).split(origList.get(0)), String.class);
		List<String> headerList = new ArrayList<>();
		for (int i = 0; i < header.length; i++) {
			if (StringUtils.isNotBlank(header[i])) {
				headerList.add(header[i]);
			} else if (StringUtils.isBlank(header[i]) && (i + 1 == header.length)) {
				logger.debug(Tag.DL_DEFAULT, "========= Skip extra delimiter(s) of the header at the end.");
			} else {
				logger.debug(Tag.DL_DEFAULT, "========= The original list contains empty field(s) in the header.");
				throw new RuntimeException("The original list contains empty field(s) in the header.");
			}
		}
		String[] trimmedHeader = new String[headerList.size()];
		trimmedHeader = headerList.toArray(trimmedHeader);
		trimmedList.add(trimmedHeader);

		List<String[]> recordsList = new ArrayList<>();
		Stream<String> recordsStream = origList.subList(1, origList.size()).stream();
		recordsList = recordsStream.map(l -> Iterables.toArray(Splitter.on(delimiter).split(l), String.class)).collect(Collectors.toList());
		recordsStream.close();

		List<String[]> trimmedRecords = new ArrayList<>();
		for (int i = 0; i < recordsList.size(); i++) {
			String[] elem = recordsList.get(i);
			int gap = headerList.size() - elem.length;
			if (gap < 0 && ArrayUtils.isNotEmpty(elem)) {
				trimmedRecords.add(Arrays.copyOf(elem, headerList.size()));
				if (gap < -1) {
					logger.info(Tag.DL_STAT_INFO, MessageFormat.format("========= Row {0, number, integer} "
							+ "contains {1, number, integer} columns. {2, number, integer} column(s) been removed from the end.",
							i+2, elem.length, gap));
				}
			} else if (gap == 0 && ArrayUtils.isNotEmpty(elem)) {
				trimmedRecords.add(elem);
			} else if (gap > 0 && ArrayUtils.isNotEmpty(elem)) {
				String[] extraElem = new String[gap];
				for (int j = 0; j < gap; j++) {
					extraElem[j] = null;
				}
				trimmedRecords.add(ArrayUtils.addAll(elem, extraElem));
				logger.info(Tag.DL_STAT_INFO, MessageFormat.format("========= {0, number, integer} elements with NULL value have been added to Row {1, number, integer}", gap, i+2));
			} else {
				throw new RuntimeException("The length of row " + (i+2) + " is not equal to the length of header.");
			}
		}

		if (trimmedRecords.size() > 0)
			trimmedList.addAll(trimmedRecords);

		return trimmedList;
	}
	
	public static String normalizeFileName(final File file) {
		String rowName = file.getName();
		if (rowName.startsWith("Ref_")) {
			rowName = rowName.substring(4);
		}
		return rowName.substring(0, rowName.length() - 4).toLowerCase();
	}

	public List<GenericRecord> convertDataToAvro(List<String[]> data, SchemaGenerationInfo schemaGenerationInfo) throws Exception {
		String[] headers = data.get(0); // Skip the header and continue reading data after header row
		List<GenericRecord> records = new ArrayList<>();
		logger.info(Tag.DL_DEFAULT, "=== START: convertDataToAvro.");
		for (int i = 1; i < data.size(); i++) {
			GenericRecord record = new GenericData.Record(new Schema.Parser().parse(toAvroSchema(schemaGenerationInfo)));
			logger.debug(Tag.DL_STAT_INFO, MessageFormat.format("=============== Extracting row: {0, number, integer} / {1, number, integer}", i, data.size()));
			for (int k = 0; k < headers.length; k++) {
				if (StringUtils.isNotBlank(headers[k])) {
					logger.debug(Tag.DL_STAT_INFO, MessageFormat.format("================== Extracting column: {0, number, integer} / {1, number, integer} : {2}", k, headers.length, headers[k]));
					int typePos = schemaGenerationInfo.names.indexOf(convertTableName(headers[k]));
					if (typePos != -1) {
						String typeString = schemaGenerationInfo.types.get(typePos);
						if ("int".equalsIgnoreCase(typeString)) {
							logger.debug(Tag.DL_DEFAULT, MessageFormat.format("================== The type of {0} is int => convertTableName: {1}", convertTableName(headers[k]), data.get(i)[k]));
							record.put(convertTableName(headers[k]), Integer.valueOf(data.get(i)[k]));
						} else {
							logger.debug(Tag.DL_DEFAULT, MessageFormat.format("================== The type of {0} is not int => convertTableName: {1}", convertTableName(headers[k]), data.get(i)[k]));
							record.put(convertTableName(headers[k]), data.get(i)[k]);
						}
					} else {
						logger.debug(Tag.DL_DEFAULT, "================== TypePos equals to -1. Add the original record to the list.");
					}
				}
			}
			records.add(record);
		}

		logger.info(Tag.DL_STAT_INFO, MessageFormat.format("=== END: convertDataToAvro. Records processed => {0, number, integer}", records.size()));
		return records;
	}
	
	public SchemaGenerationInfo generateSchemaGenerationInfo(List<HiveTypeAndDataColumnName> hiveTypes, String[] titles, String table) {
        List<String> names = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<Map<String, String>> logicalTypes = new ArrayList<>();
        
        for (int i = 0; i < titles.length; i++) {
            String title = convertTableName(titles[i]);
            names.add(title);
    			
			String type = hiveTypes.stream()
					.filter(h -> h.columnName.equalsIgnoreCase(title))
					.map(h -> h.hiveType)
					.findFirst()
					.get();
    			types.add(type);
            
            logicalTypes.add(null);
        }
        	
        return new SchemaGenerationInfo(table, names, types, logicalTypes);
    }

	private void sendToKafka(TietoallasKafkaProducer tietoallasKafkaProducer, List<GenericRecord> records, Schema schema, String tableName) {
		try {
			GenericRecord message = new GenericData.Record(schema);
			message.put(DynamicAvroTools.ROWS_NAME, records);
			List<MonitoringData> monitoringData = tietoallasKafkaProducer.run(schema, message, tableName, INTEGRATION_NAME);
			int sum = monitoringData.stream()
	                .mapToInt(i -> i.sendBytes)
	                .sum();
			if (metricService != null){
	            //Unit tests don't use metric service
	            metricService.reportSendBytes(INTEGRATION_NAME, tableName, (long) sum);
	        }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void cleanCSVFile(Path csvFile, String prevRefFolderPath) throws Exception {
		String fileName = csvFile.toFile().getName();
		logger.info(Tag.DL_DEFAULT, MessageFormat.format("=== BEGIN: cleanCSVFile => {0}", fileName));
		if (!fileName.startsWith("Ref_")) {
			deleteFile(csvFile);
			logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Normal CSV File {0} has been deleted.", fileName));
		} else {
			replaceReferenceFile(csvFile, prevRefFolderPath);
			logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Reference CSV File {0} has been moved to previous_refs folder.", fileName));
		}
	}
	
	// Folder permission: 770 | File permission: 660
	public boolean checkFilePermission(File file) throws Exception {
		if (file.exists()) {
			return file.getParentFile().canWrite() && file.canRead() && file.canWrite();
		} else {
			return false;
		}
	}
	
	private void deleteFile(Path file) throws IOException {
		try {
			Files.delete(file);
		} catch (AccessDeniedException e) {
			throw new RuntimeException(e);
		}
	}

	private void replaceReferenceFile(Path refFilePath, String prevRefFolderPath) throws IOException {
		Path previousRef = Paths.get(prevRefFolderPath, refFilePath.getFileName().toString());
		Files.move(refFilePath, previousRef, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void cleanFolder(Path folder) throws Exception {
		String folderName = folder.toFile().getName();
		logger.debug(Tag.DL_DEFAULT, MessageFormat.format("=== BEGIN: cleanFolder => {0}", folderName));
		List<Path> remainedCSVFiles = listCSVFilesOfFolderWithReadyFlag(folder);
		if (remainedCSVFiles.isEmpty()) {
			FileUtils.forceDelete(folder.toFile());
			logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Folder {0} has been cleaned.", folderName));
		} else {
			logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Folder {0} still contains unprocessed CSV file(s). Please investigate the reason.", folderName));
		}
	}
}
