package fi.tietoallas.integration.caresuiteincremental.service;

/*-
 * #%L
 * caresuite-incremental
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
import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.domain.MonitoringData;
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.caresuiteincremental.config.CaresuiteDbConfig;
import fi.tietoallas.integration.caresuiteincremental.domain.TableSqlQueryPair;
import fi.tietoallas.integration.caresuiteincremental.repository.CareSuiteRepository;
import fi.tietoallas.monitoring.commonmonitoring.MetricService;
import fi.tietoallas.monitoring.commonmonitoring.MetricServicePrometheusImpl;
import fi.tietoallas.monitoring.commonmonitoring.Tag;
import fi.tietoallas.monitoring.commonmonitoring.TaggedLogger;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import static fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools.ROWS_NAME;
import static fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools.toIntegrationAvroSchema;

/**
 * Service which fetches increments from caresuite database using
 * Statusdb and other services. After gathering information as Avro General record
 * it sends them to kafka and send message to PushGateway to allow
u * monitoring to follow incremental integration
 *
 * @author xxkallia
 */
@Component
public class CareSuiteIncrementaLoad {

    private static final String PUSH_GATAWAY_PORT = "9091";

    private LoadDbOidBasedDataService loadDbOidBasedDataService;

    private String integrationName;

    StatusDatabaseRepository statusRepository;
    TietoallasKafkaProducer producer;
    private CareSuiteRepository careSuiteRepository;
    private Environment environment;
    private MetricService metricService;

    private TaggedLogger logger = new TaggedLogger(CareSuiteIncrementaLoad.class,"caresuite");

    public CareSuiteIncrementaLoad (@Autowired Environment environment,
                                    @Autowired CaresuiteDbConfig caresuiteDbConfig,
                                    @Autowired CareSuiteRepository careSuiteRepository) {
        this.environment = environment;
        this.careSuiteRepository=careSuiteRepository;
        this.integrationName =environment.getProperty("fi.datalake.comp.instancename");
        if (environment.getProperty("pushgateway.host") != null) {
            String pushGatawayAddress = environment.getProperty("pushgateway.host") + ":" + PUSH_GATAWAY_PORT;
            metricService = new MetricServicePrometheusImpl(pushGatawayAddress);
        }
    }

    /**
     *  Main scheduled loop calls different type of integrations once in hour
     * @throws Exception
     */

    @Scheduled(fixedDelay = 60*1000*60)
    public void incrementalLoad() throws Exception {
        if (this.statusRepository == null){
            DbConfiguration dbConfiguration = new DbConfiguration(this.environment);
            this.statusRepository=new StatusDatabaseRepository(dbConfiguration.jdbcTemplate());
        }
        if (producer == null) {
            producer = new TietoallasKafkaProducer(getKafkaProperties());
        }
        loadDbOidBasedDataService = new LoadDbOidBasedDataService(statusRepository,careSuiteRepository);
         new TietoallasKafkaProducer(getKafkaProperties());
        List<TableStatusInformation> simpleTablePairs = statusRepository.getTablePairs(integrationName);
        loadSmallAndStaticTables(simpleTablePairs);
        loadChangesUsingChangeTable();
        List<TableStatusInformation> timetableUpdateInfo = statusRepository.getTimetableUpdateInfo(integrationName);
        loadTablesBasedOnTime(timetableUpdateInfo);
    }

     void loadSmallAndStaticTables(final List<TableStatusInformation> tables) throws Exception {

        for (TableStatusInformation table : tables) {
            logger.info("table: " + table.tableName);
            if (careSuiteRepository == null){
                logger.error("caresuite repo null");
            }
            List<SchemaGenerationInfo> infos = careSuiteRepository.getColumnNames(table.tableName,table.columnQuery);
            if (infos == null || infos.isEmpty()){
                logger.error("no info");
            } else {
                SchemaGenerationInfo info = infos.get(0);
                Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(integrationName, info));
                String sql = "select " + String.join(",", table.columnQuery) + " from " + table.tableName;
                List<GenericRecord> rows = careSuiteRepository.getNewData(sql, table.tableName);
                logger.info(Tag.DL_STAT_INFO, MessageFormat.format("size of rows in {0,number,integer} ", rows.size()));
                if (!rows.isEmpty()) {

                    sendToKafka( rows, recordSchema, table.tableName,false,null);
                }
            }
        }

    }


     private void sendToKafka( List<GenericRecord> rows,
                             Schema messageSchema, String tableName,
                             boolean insertNewValue, BigDecimal lastValue) {
        GenericRecord message = new GenericData.Record(messageSchema);
        message.put(ROWS_NAME, rows);
        logger.info(Tag.DL_DEFAULT,MessageFormat.format("topic {0}", integrationName));
        try {
            List<MonitoringData> monitoringData = producer.run(messageSchema, message, tableName, integrationName);
            int sum = monitoringData.stream()
                    .mapToInt(i -> i.sendBytes)
                    .sum();

            if (insertNewValue && lastValue != null) {
                statusRepository.updateTableInformation(integrationName, tableName, lastValue);
            } else {
                statusRepository.updateTableTimeStamp(integrationName, tableName);
            }
            if (metricService != null) {
                metricService.reportSendBytes(integrationName, tableName, (long) sum);
            }
        } catch (Exception e){
            logger.error("Kafka send error ",e);
        }
    }

     void loadTablesBasedOnTime(List<TableStatusInformation> timetableUpdateInfos) throws Exception {
        logger.info("LoadTablesBasedOnTime");
        for (TableStatusInformation table : timetableUpdateInfos) {
            List<SchemaGenerationInfo> infos = careSuiteRepository.getColumnNames(table.tableName,table.columnQuery);
            if (infos != null && !infos.isEmpty()) {
                List<GenericRecord> newData = careSuiteRepository.getNewData(generateTimeSql(table.timeColumn, table.lastAccessAt, table.tableName,table.columnQuery), table.tableName);
                logger.info(Tag.DL_STAT_INFO,MessageFormat.format("size of newDate {0,number,integer} ",  newData.size()));
                if (!newData.isEmpty()) {
                    Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(integrationName, infos.get(0)));
                    sendToKafka( newData,recordSchema,table.tableName,false,null);
                }
            }
        }
    }

    private String generateTimeSql(String timeColumn, Timestamp lastAccessedAt, String table, String columns) {
        if (lastAccessedAt != null){
            return "select "+columns +" from "+table+" where "+timeColumn+" >= '"+lastAccessedAt.toString()+"'";
        }
        else {
            return "select "+columns +" from "+table;
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

     void loadChangesUsingChangeTable() throws Exception {
        logger.info("Changes using changes ");
        if (loadDbOidBasedDataService == null){
            loadDbOidBasedDataService = new LoadDbOidBasedDataService(statusRepository,careSuiteRepository);
        }
        List<TableSqlQueryPair> statusInformations = loadDbOidBasedDataService.gatherData(integrationName);
        for (TableSqlQueryPair tableSqlQueryPair : statusInformations) {
            List<SchemaGenerationInfo> info = careSuiteRepository.getColumnNames(tableSqlQueryPair.tableName,tableSqlQueryPair.columnQuery);
            if (info != null && !info.isEmpty()) {
                Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(integrationName, info.get(0)));
                List<GenericRecord> rows = careSuiteRepository.getNewData(tableSqlQueryPair.sql, tableSqlQueryPair.tableName);

                if (rows != null && !rows.isEmpty()) {
                    logger.info(Tag.DL_STAT_INFO,MessageFormat.format("rows size {0,number,integer} ",rows.size()));

                    sendToKafka( rows, recordSchema, tableSqlQueryPair.tableName, true, getNewBiggestValue(rows,tableSqlQueryPair.columnName));

                }
            }
        }
    }
    private BigDecimal getNewBiggestValue(final List<GenericRecord> newData,final String keyFieldName) {

        BigDecimal tempBigDecimal = new BigDecimal(0);
        Schema schema = newData.get(0).getSchema();
        List<Schema.Field> fields = schema.getFields();
        Schema.Field keyField = fields.stream()
                .filter(f -> f.name().equalsIgnoreCase(keyFieldName))
                .findFirst()
                .get();
        String logicalType = keyField.getProp("logicalType");
        if (logicalType != null) {
            for (GenericRecord genericRecord : newData) {
                Object value = genericRecord.get(keyField.name());
                if (logicalType.equals("decimal")) {
                    LogicalTypes.Decimal decimal = LogicalTypes.decimal(Integer.parseInt(keyField.getProp("precision")), Integer.parseInt(keyField.getProp("scale")));
                    BigDecimal bigDecimal = new Conversions.DecimalConversion().fromBytes((ByteBuffer) value, null, decimal);
                    if (bigDecimal.compareTo(tempBigDecimal) > 0) {
                        tempBigDecimal = bigDecimal;
                    }
                }
            }
        }
        else {
            return newData.stream()
                    .map(k -> new BigDecimal(String.valueOf(k.get(keyField.name()))))
                    .max(BigDecimal::compareTo)
                    .get();

        }
        return tempBigDecimal;

    }
}
