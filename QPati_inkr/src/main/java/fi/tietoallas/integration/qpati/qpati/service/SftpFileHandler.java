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
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.qpati.qpati.repository.MetadataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;

import fi.tietoallas.monitoring.commonmonitoring.Tag;
import fi.tietoallas.monitoring.commonmonitoring.TaggedLogger;

@Service
public class SftpFileHandler {
	
    //private static final String DATA_PATH = "/mnt/datadisk/qpati/chroot/data/";
	 private static final String DATA_PATH = "/mnt/datadisk/qpati/chroot/tmp/";
	// private static final String DATA_PATH = "/Users/huangyee/Desktop/datalake/repos/integrations/QPati_inkr/src/test/resources/package_service/tmp/";
	private static boolean isAllDeleted = false;
	private TietoallasKafkaProducer producer = null;
	private MetadataRepository metadataRepository;
	private Environment environment;
	private static TaggedLogger logger = new TaggedLogger(SftpFileHandler.class, "QPati_inkr");

	public SftpFileHandler(
			@Autowired MetadataRepository metadataRepository, 
			@Autowired Environment environment) {
		this.metadataRepository = metadataRepository;
		this.environment = environment;
	}

	@Scheduled(fixedDelay = 10 * 60 * 1000)
	public void start() throws Exception {
		logger.info(Tag.DL_DEFAULT, MessageFormat.format("Starting QPati incremental (v1.0.84) load at {0}", LocalDateTime.now().toString()));
		long startTime = System.currentTimeMillis();
		if (producer == null) {
			producer = new TietoallasKafkaProducer(getKafkaProperties());
		}

		FileWalker fileWalker = new FileWalker(metadataRepository, environment);

		// DATA_PATH = environment.getProperty("fi.datalake.comp.sftp.datapath");
		fileWalker.readFilesAndSendToKafka(DATA_PATH, producer);
		deleteFromRoot(DATA_PATH);
		long endTime = System.currentTimeMillis();
		logger.info(Tag.DL_STAT_INFO, MessageFormat.format("Stoping QPati incremental (V1.0.81). Total duration: {0, number, long}", endTime - startTime));
	}

	public void deleteFromRoot(String dir) throws Exception {
		logger.debug(Tag.DL_DEFAULT, MessageFormat.format("=== BEGIN: Recursively Delete Empty Folders From Root: {0}", dir));
		do {
			isAllDeleted = true;
			recursiveDeleteEmptyFolder(Paths.get(dir).toFile());
		} while (!isAllDeleted);
		logger.debug(Tag.DL_DEFAULT, "=== END: Recursively Delete Empty Folders From Root");
	}

	private void recursiveDeleteEmptyFolder(File file) throws Exception {
		if (!file.exists()) {
			isAllDeleted = true;
		} else if (file.listFiles().length == 0) {
			if (file.toPath().equals(Paths.get(DATA_PATH))) {
				logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Recursively Delete Empty Folders Done. Folder: {0}", file.toString()));
				isAllDeleted = true;
			} else {
				file.delete();
				logger.debug(Tag.DL_DEFAULT, MessageFormat.format("====== Folder removed: {0}", file));
				isAllDeleted = false;
			}
		} else {
			for (File f : file.listFiles()) {
				if (f.isDirectory())
					recursiveDeleteEmptyFolder(f);
			}
		}
	}

	private Properties getKafkaProperties() {
		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", environment.getProperty("bootstrap.servers"));
		properties.setProperty("max.request.size", environment.getProperty("max.request.size"));
		properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		properties.setProperty("compression.type", "gzip");
		properties.setProperty("max.request.size", "20971520");
		return properties;
	}
}
