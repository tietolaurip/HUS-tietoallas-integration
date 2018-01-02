package fi.tietoallas.integration.incremental.cressidaods.service;

/*-
 * #%L
 * cressidaods
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
import fi.tietoallas.integration.incremental.cressidaods.repository.CressidaOdsRepository;
import fi.tietoallas.monitoring.commonmonitoring.MetricService;
import fi.tietoallas.monitoring.commonmonitoring.MetricServicePrometheusImpl;
import fi.tietoallas.monitoring.commonmonitoring.Tag;
import fi.tietoallas.monitoring.commonmonitoring.TaggedLogger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * Main class for incremental imports from CressidaODS system
 * @author xxkallia
 */
@Component
public class CressidaOdsIncrementaService {

    private CressidaOdsRepository cressidaOdsRepository;
    private StatusDatabaseRepository statusDatabaseRepository;
    private Environment environment;
    private static final String PUSH_GATAWAY_PORT = "9091";

    private static final String INTEGRATION_NAME="cressidaods";
    private MetricService metricService;

    private static TaggedLogger logger = new TaggedLogger(CressidaOdsIncrementaService.class,"cressidaods_incr");

    TietoallasKafkaProducer tietoallasKafkaProducer =null;

    public CressidaOdsIncrementaService(@Autowired CressidaOdsRepository cressidaOdsRepository,@Autowired StatusDatabaseRepository statusDatabaseRepository,
                                        @Autowired Environment environment){
        this.cressidaOdsRepository=cressidaOdsRepository;
        this.statusDatabaseRepository=statusDatabaseRepository;
        this.environment=environment;
        if (environment.getProperty("pushgateway.host") != null) {
            String pushGatawayAddress = environment.getProperty("pushgateway.host") + ":" + PUSH_GATAWAY_PORT;
            metricService = new MetricServicePrometheusImpl(pushGatawayAddress);
        }

    }

    /**
     * Method witch fetches new or updated data every table has timestamp
     * column which shows when it's updated and it's used to find updated or new rows.
     * Rows are then send to kafka and status database is updated
     */

    @Scheduled(fixedDelay = 5*60*1000)
    public void updateIncrements(){
        try {
            if (tietoallasKafkaProducer == null) {
                tietoallasKafkaProducer = new TietoallasKafkaProducer(getKafkaProperties());
            }
            List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables(INTEGRATION_NAME);
            for (TableStatusInformation tableStatusInformation : allTables) {
                logger.info(Tag.DL_STAT_INFO, MessageFormat.format("Stating processing table {0}", tableStatusInformation.tableName));
                List<SchemaGenerationInfo> infos = cressidaOdsRepository.getColumnInfo(tableStatusInformation.tableName,tableStatusInformation.columnQuery);
                List<GenericRecord> newData = cressidaOdsRepository.getNewData(generateSql(tableStatusInformation), tableStatusInformation.tableName, tableStatusInformation.lastAccessAt);
                logger.info(Tag.DL_STAT_INFO, MessageFormat.format("Stating got {0},number,integer} from  table {1}",newData.size(), tableStatusInformation.tableName));
                SchemaGenerationInfo info = infos.get(0);
                Schema recordSchema = new Schema.Parser().parse(DynamicAvroTools.toIntegrationAvroSchema(INTEGRATION_NAME, info));
                sendToKafka(tietoallasKafkaProducer, newData, recordSchema, tableStatusInformation.tableName);
                logger.info(Tag.DL_DEFAULT,MessageFormat.format("Data from table {0} send",tableStatusInformation.tableName));
                statusDatabaseRepository.updateTableTimeStamp(INTEGRATION_NAME, tableStatusInformation.tableName);
            }
        } catch (Exception e){
            logger.error("error in cressidaods integration ",e);
        }

    }

    private void sendToKafka(TietoallasKafkaProducer tietoallasKafkaProducer, List<GenericRecord> rows, Schema schema, String tableName) throws Exception {
        GenericRecord message = new GenericData.Record(schema);
        message.put(DynamicAvroTools.ROWS_NAME, rows);
        List<MonitoringData> monitoringData = tietoallasKafkaProducer.run(schema, message, tableName, INTEGRATION_NAME);
        int sum = monitoringData.stream()
                .mapToInt(i -> i.sendBytes)
                .sum();

        statusDatabaseRepository.updateTableTimeStamp(INTEGRATION_NAME, tableName);
        if (metricService != null) {
            metricService.reportSendBytes(INTEGRATION_NAME, tableName, (long) sum);
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

    private static String generateSql(final TableStatusInformation tableStatusInformation){
        return "select "+tableStatusInformation.columnQuery+ " from "+tableStatusInformation.tableName
                +" where "+tableStatusInformation.timeColumn+ " >= ?";
    }

}
