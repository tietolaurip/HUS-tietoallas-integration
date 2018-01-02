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
import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.incremental.cressidaods.config.SourceDbConfig;
import fi.tietoallas.integration.incremental.cressidaods.repository.CressidaOdsRepository;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CressidaOdsIncrementaServiceTest {

    private StatusDatabaseRepository statusDatabaseRepository;
    private CressidaOdsRepository cressidaOdsRepository;
    private JdbcTemplate statusJdbcTemplate;
    private JdbcTemplate cressidaOdsJdbcTemplate;
    @Mock
    private Environment environment;

    @Mock
    private TietoallasKafkaProducer tietoallasKafkaProducer;

    private CressidaOdsIncrementaService incrementalLoadService;

    MockProducer<String,byte[]> producer;

    @Before
    public void setUp(){
        when(environment.getProperty(eq("fi.datalake.sourcedb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.sourcedb.url"))).thenReturn("jdbc:h2:~/test");
        when(environment.getProperty(eq("fi.datalake.statusdb.url"))).thenReturn("jdbc:h2:~/test");
        when(environment.getProperty(eq("fi.datalake.statusdb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.comp.instancename"))).thenReturn("cressidaods");
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        SourceDbConfig caDbConfig = new SourceDbConfig(environment);
        statusJdbcTemplate = dbConfiguration.jdbcTemplate();
        cressidaOdsJdbcTemplate = caDbConfig.jdbcTemplate();
        statusDatabaseRepository = new StatusDatabaseRepository(statusJdbcTemplate);
        cressidaOdsRepository = new CressidaOdsRepository(cressidaOdsJdbcTemplate);
        setupTestStatusData();
        producer = new MockProducer<>(true, new StringSerializer(),new ByteArraySerializer());
        }
    @Test
    public void testIncremntal(){
        incrementalLoadService = new CressidaOdsIncrementaService(cressidaOdsRepository,statusDatabaseRepository,environment);
        incrementalLoadService.tietoallasKafkaProducer=new TietoallasKafkaProducer(producer);
        cressidaOdsJdbcTemplate.execute("drop table IF EXISTS otsikko_attribuutti_ods");
        cressidaOdsJdbcTemplate.execute("create table otsikko_attribuutti_ods (OTSIKKOLINKKI_NUMERO VARCHAR(255),OTSIKKO_NUMERO VARCHAR(255),ATTRIBUUTTI_NUMERO VARCHAR(255),PAIVITYSHETKI_ODS TIMESTAMP)");
        cressidaOdsJdbcTemplate.update("insert into otsikko_attribuutti_ods (OTSIKKOLINKKI_NUMERO,OTSIKKO_NUMERO,ATTRIBUUTTI_NUMERO,PAIVITYSHETKI_ODS) VALUES ('otsikkolinkki numero','otsikko numero','attribuutti numero','2020-02-02 10:10:10.000')");
        List<TableStatusInformation> beforeUpdate = statusDatabaseRepository.getAllTables("cressidaods");
        assertThat(beforeUpdate,hasSize(1));
        incrementalLoadService.updateIncrements();
        assertThat(producer.history(),hasSize(1));
        List<TableStatusInformation> afterUpdate = statusDatabaseRepository.getAllTables("cressidaods");
        assertThat(afterUpdate,hasSize(1));
        assertThat(beforeUpdate.get(0).lastAccessAt,not(afterUpdate.get(0).lastAccessAt));

    }
    @Test
    public void testKafkaError() throws Exception{
        incrementalLoadService = new CressidaOdsIncrementaService(cressidaOdsRepository,statusDatabaseRepository,environment);
        when(tietoallasKafkaProducer.run(Mockito.any(Schema.class), Mockito.any(GenericRecord.class),Mockito.anyString(),Mockito.anyString())).thenThrow(new RuntimeException());;
        incrementalLoadService.tietoallasKafkaProducer=tietoallasKafkaProducer;
        cressidaOdsJdbcTemplate.execute("drop table IF EXISTS otsikko_attribuutti_ods");
        cressidaOdsJdbcTemplate.execute("create table otsikko_attribuutti_ods (OTSIKKOLINKKI_NUMERO VARCHAR(255),OTSIKKO_NUMERO VARCHAR(255),ATTRIBUUTTI_NUMERO VARCHAR(255),PAIVITYSHETKI_ODS TIMESTAMP)");
        cressidaOdsJdbcTemplate.update("insert into otsikko_attribuutti_ods (OTSIKKOLINKKI_NUMERO,OTSIKKO_NUMERO,ATTRIBUUTTI_NUMERO,PAIVITYSHETKI_ODS) VALUES ('otsikkolinkki numero','otsikko numero','attribuutti numero','2020-02-02 10:10:10.000')");
        List<TableStatusInformation> beforeUpdate = statusDatabaseRepository.getAllTables("cressidaods");
        assertThat(beforeUpdate,hasSize(1));
        incrementalLoadService.updateIncrements();
        List<TableStatusInformation> afterUpdate = statusDatabaseRepository.getAllTables("cressidaods");
        assertThat(afterUpdate,hasSize(1));
        assertThat(beforeUpdate.get(0).lastAccessAt,is(afterUpdate.get(0).lastAccessAt));

    }
    private void setupTestStatusData() {
        statusJdbcTemplate.execute("DROP TABLE IF EXISTS integration_status");
        statusJdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('otsikko_attribuutti_ods', NULL, 'TIME_COMPARISATION', 'PAIVITYSHETKI_ODS', NULL, '2016-02-17 04:00:48.610', 'cressidaods', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'OTSIKKOLINKKI_NUMERO,OTSIKKO_NUMERO,ATTRIBUUTTI_NUMERO,PAIVITYSHETKI_ODS', NULL, 'TIME_COMPARISATION', NULL, NULL, NULL)");
    }
}
