/*-
 * #%L
 * ca-incremental-import
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
package fi.tietoallas.integration.ca.caincrementalimport.service;

import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.domain.MonitoringData;
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.ca.caincrementalimport.repository.CaRepository;
import fi.tietoallas.monitoring.commonmonitoring.MetricService;
import fi.tietoallas.monitoring.commonmonitoring.MetricServicePrometheusImpl;
import fi.tietoallas.monitoring.commonmonitoring.Tag;
import fi.tietoallas.monitoring.commonmonitoring.TaggedLogger;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static java.text.MessageFormat.format;

/**
 * Main service class for incremental loading from CA
 * Handles all incremental types in one scheduled  method
 * @author xxkallia
 */

@Component
public class CaIncrementalLoadService {

    CaRepository caRepository;
    StatusDatabaseRepository statusDatabaseRepository;
    TietoallasKafkaProducer producer;
    private Environment environment;
    private EncoderFactory encoderFactory;
    private MetricService metricService;
    private static final String PUSH_GATAWAY_PORT = "9091";

    private static TaggedLogger logger = new TaggedLogger(CaIncrementalLoadService.class,"ca");

    private static final String INTEGRATION_NAME = "ca";

    public CaIncrementalLoadService(@Autowired Environment environment, @Autowired CaRepository caRepository) {
        this.environment = environment;
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        statusDatabaseRepository = new StatusDatabaseRepository(dbConfiguration.jdbcTemplate());
        this.caRepository = caRepository;
        this.encoderFactory = EncoderFactory.get();
        if (environment.getProperty("pushgateway.host") != null) {
            String pushGatawayAddress = environment.getProperty("pushgateway.host") + ":" + PUSH_GATAWAY_PORT;
            metricService = new MetricServicePrometheusImpl(pushGatawayAddress);
        }
    }

    /**
     * Method which fetches data from CA database and sends it to Kafka
     */
    @Scheduled(fixedDelay = 60 * 60 * 24 * 1000)
    public void loadIncrements() {


        logger.info(Tag.DL_DEFAULT, format("Starting CA incremental load at {0}", LocalDateTime.now().toString()));
        try {
            if (producer == null) {
                producer = new TietoallasKafkaProducer(getKafkaProperties());
            }
            List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables(INTEGRATION_NAME);
            logger.debug(format("Found {0,number,integer} tables...", allTables.size()));
            for (TableStatusInformation tableStatusInformation : allTables) {
                try {
                    List<SchemaGenerationInfo> infos = caRepository.getColumnInfo(tableStatusInformation.tableName,
                            tableStatusInformation.columnQuery);
                    if (infos == null || infos.isEmpty()) {
                        logger.warn(Tag.DL_EXTERNAL_CONNECTION_FAILURE, format("No column names defined for table {0}. Tried query {1}", tableStatusInformation.tableName,
                                tableStatusInformation.columnQuery));
                    } else {
                        SchemaGenerationInfo info = infos.get(0);
                        Schema recordSchema = new Schema.Parser().parse(DynamicAvroTools.toIntegrationAvroSchema(INTEGRATION_NAME, info));
                        logger.info(Tag.DL_STAT_INFO, format("Stating processing table {0}, table type is {1}.", tableStatusInformation.tableName,
                                tableStatusInformation.parameterType));
                        String sql = getSql(tableStatusInformation, tableStatusInformation.columnQuery);
                        List<GenericRecord> newData = caRepository.getNewData(sql, tableStatusInformation.tableName);
                        if (!newData.isEmpty()) {
                            logger.info(Tag.DL_STAT_INFO, format("Sending data to kafka for table {0}. Found {1,number,insteger} records...", tableStatusInformation.tableName, newData.size()));
                            sendToKafka(producer, newData, recordSchema, tableStatusInformation.tableName);
                            logger.debug(Tag.DL_DEFAULT, format("Done. Updating Status DB timestamp for table {0}.", tableStatusInformation.tableName));
                            if (tableStatusInformation.parameterType.equals("HISTORY_TABLE_LOOKUP")
                                    || tableStatusInformation.parameterType.equals("STATUSDB_TABLE_LOOKUP")) {
                                long max = getNewBiggestValue(newData,tableStatusInformation.keyColumn);
                                logger.debug(Tag.DL_STAT_INFO, format("Done. Updating Status DB index for table {0}. New index is {1}.", tableStatusInformation.tableName, String.valueOf(max)));
                                statusDatabaseRepository.updateTableInformation(INTEGRATION_NAME, tableStatusInformation.tableName,
                                        new BigDecimal(max));
                            } else {
                                statusDatabaseRepository.updateTableTimeStamp(INTEGRATION_NAME, tableStatusInformation.tableName);
                            }
                        } else {
                            logger.info(Tag.DL_STAT_INFO, format("No new lines for {0} ", tableStatusInformation.tableName));
                        }

                    }
                } catch (Exception e) {
                    logger.error("Error writing committing AVRO to Kafka for CA, table " + tableStatusInformation.tableName, e);
                }
            }
            logger.info(Tag.DL_DEFAULT, format("CA Incremental load done at {0}", LocalDateTime.now().toString()));
        } catch (Exception e) {
            logger.error("Error fetching status info from database.", e);
        }
    }

    private Long getNewBiggestValue(final List<GenericRecord> newData,final String keyFieldName) {

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
                    .map(k -> new Long(String.valueOf(k.get(keyField.name()))))
                    .max(Long::compareTo)
                    .get();

        }
        return tempBigDecimal.longValue();

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

    private void sendToKafka(TietoallasKafkaProducer tietoallasKafkaProducer, List<GenericRecord> rows, Schema schema, String tableName)
            throws Exception {
        GenericRecord message = new GenericData.Record(schema);
        message.put(DynamicAvroTools.ROWS_NAME, rows);
        List<MonitoringData> monitoringData = tietoallasKafkaProducer.run(schema, message, tableName, INTEGRATION_NAME);
        int sum = monitoringData.stream()
                .mapToInt(i -> i.sendBytes)
                .sum();
        if (this.environment.getProperty("pushgateway.host") != null) {
            metricService.reportSendBytes(INTEGRATION_NAME,tableName,(long)sum);
        }

    }

    private String getSql(final TableStatusInformation information, String columns) {
        if (information.parameterType.equals("TIME_COMPARISATION")) {
            if (information.lastAccessAt != null) {
                return "select " + columns + " from " + information.tableName + " where " + information.timeColumn + " > '"
                        + information.lastAccessAt.toLocalDateTime().toString() + "'";
            }
            return "select " + columns + " from " + information.tableName;
        }  else if (information.parameterType.equals("STATUSDB_TABLE_LOOKUP")) {
            return "select " + columns + " from " + information.tableName + " where " + information.keyColumn + " > "
                    + information.lastUsedValue.toPlainString();
        } else if (information.parameterType.equals("FULL_TABLE")) {
            return "select " + columns + " from " + information.tableName;
        } else {
            throw new RuntimeException("unkown parameter type " + information.parameterType);
        }
    }
}
