package fi.tietoallas.integration.incremental.clinisoftincremental.service;

/*-
 * #%L
 * clinisoft-incremental
 * %%
 * Copyright (C) 2017 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
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
import fi.tietoallas.incremental.common.commonincremental.domain.MonitoringData;
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.incremental.clinisoftincremental.repository.ClinisoftRepository;
import fi.tietoallas.monitoring.commonmonitoring.MetricService;
import fi.tietoallas.monitoring.commonmonitoring.MetricServicePrometheusImpl;
import fi.tietoallas.monitoring.commonmonitoring.Tag;
import fi.tietoallas.monitoring.commonmonitoring.TaggedLogger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import static fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools.toIntegrationAvroSchema;
import static java.util.stream.Collectors.toList;

/**
 * Main service class for Clinisoft integration
 *
 * @author xxkallia
 */
@Component
public class ClinisoftIncrementalLoadService {

    private static final String PUSH_GATAWAY_PORT = "9091";
    @Autowired
    ClinisoftRepository clinisoftRepository;

    private StatusDatabaseRepository statusDatabaseRepository;
    private Environment environment;
    private MetricService metricService;

    private static TaggedLogger logger = new TaggedLogger(ClinisoftIncrementalLoadService.class,"Clinisoft_incr");

    private String pushGatawayAddress = "";
    TietoallasKafkaProducer producer;

    public ClinisoftIncrementalLoadService(
            @Autowired Environment environment,
            @Autowired StatusDatabaseRepository statusDatabaseRepository) {
        this.environment = environment;
        this.statusDatabaseRepository = statusDatabaseRepository;
        if (environment.getProperty("pushgateway.host") != null) {
            pushGatawayAddress = environment.getProperty("pushgateway.host") + ":" + PUSH_GATAWAY_PORT;
            metricService = new MetricServicePrometheusImpl(pushGatawayAddress);
        }


    }

    /**
     * Scheduled service witch processes all three (Patient, Department and System) databases
     * in the one Clinisoft instance first it processes Patient, then Department and last System
     */

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void loadIncrements() {
        try {
            String clinisoftInstace = environment.getProperty("fi.datalake.comp.instancename");
            List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables(clinisoftInstace);
            Properties kafkaProperties = getKafkaProperties();
            if (producer == null) {
                producer = new TietoallasKafkaProducer(kafkaProperties);
            }
            clinisoftRepository.changeDatabase("Patient");
            List<TableStatusInformation> patientTables = getTablesFromDataBase("Patient", allTables);
            processIncrementalInPatientDb(patientTables,clinisoftInstace);
            clinisoftRepository.changeDatabase("Department");
            List<TableStatusInformation> departmentTables = getTablesFromDataBase("Department",allTables);
            processIncrementsInDeparmentDb(departmentTables,clinisoftInstace);
            clinisoftRepository.changeDatabase("System");
            List<TableStatusInformation> systemTables =getTablesFromDataBase("System", allTables);
            processIncrementsInSystemDb(systemTables,clinisoftInstace);

        } catch (Exception e) {
            logger.error(Tag.DL_DEFAULT,"error in clinisoft", e);
        }
    }

    /**
     * Utility method for getting subset of all tables on one clinisoft instance
     * @param database name of the database (Patient, System, or Department)
     * @param allTables tables on that database
     * @return
     */
    protected List<TableStatusInformation> getTablesFromDataBase(final String database,final  List<TableStatusInformation> allTables) {

        return  allTables.stream()
                .filter(t -> t.originalDatabase.equalsIgnoreCase(database))
                .collect(toList());
    }

    /**
     * Fetches new or updated rows from System database and sends them
     * to kafka and then updates status database
     * @param systemTables Tables on system database
     * @param clinisoftInstace name of clinisoft instance
     * @throws Exception
     */

    protected void processIncrementsInSystemDb(final List<TableStatusInformation> systemTables,final String clinisoftInstace) throws Exception {
        for (TableStatusInformation statusInformation : systemTables) {
            String sql = getSystemSql(statusInformation);
            List<GenericRecord> newData = clinisoftRepository.getNewData(sql, statusInformation.tableName);
            if (!newData.isEmpty()) {
                List<SchemaGenerationInfo> infos = clinisoftRepository.getColumnInfo(statusInformation.tableName, statusInformation.columnQuery);
                logger.info(Tag.DL_STAT_INFO,MessageFormat.format("Got {0,number,integer} from {1} at {2}",infos.size(),statusInformation.tableName,clinisoftInstace));
                Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(clinisoftInstace, infos.get(0)));
                sendToKafka(producer, newData, recordSchema, statusInformation.tableName,clinisoftInstace);
                statusDatabaseRepository.updateTableTimeStamp(clinisoftInstace, statusInformation.tableName);
            }
        }

    }

    /**
     * Fetches new or updated rows from Department database and sends them
     * to kafka and then updates status database
     *
     * @param departmentTables Tables from Department database
     * @param clinisoftInstace name of clinisoft instance
     * @throws Exception
     */

    protected void processIncrementsInDeparmentDb(final List<TableStatusInformation> departmentTables,final String clinisoftInstace) throws Exception {
        for (TableStatusInformation statusInformation : departmentTables) {
            String sql = getDepartmentSql(statusInformation);
            List<GenericRecord> newData = clinisoftRepository.getNewData(sql, statusInformation.tableName);
            if (!newData.isEmpty()) {
                List<SchemaGenerationInfo> infos = clinisoftRepository.getColumnInfo(statusInformation.tableName, statusInformation.columnQuery);
                if (infos != null && !infos.isEmpty()) {
                    Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(clinisoftInstace, infos.get(0)));
                    logger.info(Tag.DL_STAT_INFO,MessageFormat.format("Got {0,number,integer} from {1} at {2}",infos.size(),statusInformation.tableName,clinisoftInstace));
                    sendToKafka(producer, newData, recordSchema, statusInformation.tableName,clinisoftInstace);
                }
                statusDatabaseRepository.updateTableTimeStamp(clinisoftInstace, statusInformation.tableName);
            }
        }
    }
    /**
     * Fetches new rows if patient case has been closed and rows haven't been integrated before from Patient database and sends them
     * to kafka and then updates status database
     *
     * @param patientTables Tables from Patient database
     * @param clinisoftInstace name of clinisoft instance
     * @throws Exception
     */
    protected void processIncrementalInPatientDb(final List<TableStatusInformation> patientTables,final String clinisoftInstace) throws Exception {
        List<String> patientIds = clinisoftRepository.findPatientIds(patientTables.get(0))
                .stream()
                .map(String::valueOf)
                .collect(toList());
        if (!patientIds.isEmpty()) {
            for (TableStatusInformation table : patientTables) {
                String sql = getSql(table.tableName, patientIds, "patientId", table.columnQuery, table.parameterType,
                        table.lastAccessAt, table.timeColumn);
                List<SchemaGenerationInfo> info = clinisoftRepository.getColumnInfo(table.tableName, table.columnQuery);
                List<GenericRecord> newData = clinisoftRepository.getNewData(sql, table.tableName);
                if (info != null && !info.isEmpty()) {
                    Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(clinisoftInstace, info.get(0)));
                    if (!newData.isEmpty()) {
                        logger.info(Tag.DL_STAT_INFO,MessageFormat.format("Got {0,number,integer} from {1} at {2}",newData.size(),table.tableName,clinisoftInstace));
                        sendToKafka(producer, newData, recordSchema, table.tableName,clinisoftInstace);
                        if (table.parameterType.equals("TIME_COMPARISATION")) {
                            statusDatabaseRepository.updateTableTimeStamp(clinisoftInstace, table.tableName);
                        } else if (table.parameterType.equalsIgnoreCase("HISTORY_TABLE_LOOKUP")) {
                            statusDatabaseRepository.updateTableInformation(clinisoftInstace, table.tableName, new BigDecimal(patientIds.get(0)));
                        }
                    }
                } else if (table.parameterType.equals("TIME_COMPARISATION")) {
                    statusDatabaseRepository.updateTableTimeStamp(clinisoftInstace, table.tableName);
                }
            }
        } else {
            logger.info(Tag.DL_DEFAULT,"no new patient data");
        }

    }

    private String getSystemSql(TableStatusInformation statusInformation) {
        return "select " + statusInformation.columnQuery + " from " + statusInformation.tableName;
    }

    private String getDepartmentSql(TableStatusInformation statusInformation) {
        if (statusInformation.parameterType.equalsIgnoreCase("TIME_COMPARISATION")) {
            return "select " + statusInformation.columnQuery + " from " + statusInformation.tableName + " where ArchTime > '" + statusInformation.lastAccessAt + "'";
        }
        return "select " + statusInformation.columnQuery + " from " + statusInformation.tableName;
    }

    private String getSql(final String tableName, final List<String> patientNames, final String keyColumn, final String columns, final String parameterType, final Timestamp lastAccessAt, final String timeColumn) {
        if (parameterType.equals("TIME_COMPARISATION")) {
            if (lastAccessAt != null) {
                return "select " + columns + " from " + tableName + " where " + timeColumn + " > '" + lastAccessAt.toLocalDateTime().toString() + "'";
            }
            return "select " + columns + " from " + tableName;
        } else if (parameterType.equals("HISTORY_TABLE_LOOKUP")) {
            return "select " + columns + " from " + tableName + " where " + keyColumn + " in ( " + StringUtils.join(patientNames, ",") + ")";
        } else if (parameterType.equals("FULL_TABLE")) {
            return "select " + columns + " from " + tableName;
        } else {
            throw new RuntimeException("unkown parameter type " + parameterType);
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




    private void sendToKafka(final TietoallasKafkaProducer tietoallasKafkaProducer,final List<GenericRecord> rows,final Schema schema,final String tableName,final String clinisoftInstace) throws Exception {
        GenericRecord message = new GenericData.Record(schema);
        message.put(DynamicAvroTools.ROWS_NAME, rows);
        List<MonitoringData> monitoringData = tietoallasKafkaProducer.run(schema, message, tableName, clinisoftInstace);
        int sum = monitoringData.stream()
                .mapToInt(i -> i.sendBytes)
                .sum();
        if (metricService != null){
            //Unit tests don't use metric service
            metricService.reportSendBytes(clinisoftInstace,tableName, (long) sum);
        }
    }

}
