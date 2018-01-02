package fi.tietoallas.integration.ca.caincrementalimport.service;

/*-
 * #%L
 * ca-incremental-import
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
import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.ca.caincrementalimport.configuration.CaDbConfig;
import fi.tietoallas.integration.ca.caincrementalimport.repository.CaRepository;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CaIncrementalLoadServiceTest {

    private StatusDatabaseRepository statusDatabaseRepository;
    private CaRepository caRepository;
    private JdbcTemplate statusJdbcTemplate;
    private JdbcTemplate caJdbcTemplate;
    @Mock
    private Environment environment;

    private CaIncrementalLoadService caIncrementalLoadService;

    MockProducer<String,byte[]> producer;
    @Mock
    TietoallasKafkaProducer tietoallasKafkaProducer;

    @Before
    public void setUp(){
        when(environment.getProperty(eq("fi.datalake.sourcedb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.sourcedb.url"))).thenReturn("jdbc:h2:mem:test");
        when(environment.getProperty(eq("fi.datalake.statusdb.url"))).thenReturn("jdbc:h2:mem:test");
        when(environment.getProperty(eq("fi.datalake.statusdb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.comp.instancename"))).thenReturn("ca");
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        CaDbConfig caDbConfig = new CaDbConfig(environment);
        statusJdbcTemplate = dbConfiguration.jdbcTemplate();
        caJdbcTemplate = caDbConfig.jdbcTemplate();
        statusDatabaseRepository = new StatusDatabaseRepository(statusJdbcTemplate);
        caRepository = new CaRepository(caJdbcTemplate);
        setupTestStatusData();
        producer = new MockProducer<>(true, new StringSerializer(),new ByteArraySerializer());

        caIncrementalLoadService = new CaIncrementalLoadService(environment,caRepository);
        caIncrementalLoadService.statusDatabaseRepository=statusDatabaseRepository;
        caIncrementalLoadService.producer=new TietoallasKafkaProducer(producer);

    }

    @Test
    public void testloadIncrements(){
        List<TableStatusInformation> beforeRun = statusDatabaseRepository.getAllTables("ca");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_T");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_P");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_U");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS TType");
        caJdbcTemplate.execute("CREATE TABLE V_T (T_ID NUMERIC(9, 0) NOT NULL, T_name NVARCHAR NOT NULL, T_mapcode NVARCHAR NOT NULL, T_code NVARCHAR NOT NULL, T_scode NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO V_T (T_ID, T_name, T_mapcode, T_code, T_scode) VALUES (2, 'Case start', 'Case start', 'CaseStart', 'Case start')");
        caJdbcTemplate.execute("CREATE TABLE V_P(P_ID NVARCHAR NOT NULL, P_Update DATETIME2, P_name NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO V_P (P_ID, P_Update, P_name) VALUES ('1000000000005N008B', '2017-10-04 04:05:25.5870000', 'sample')");
        caJdbcTemplate.execute("create TABLE V_U (U_ID nvarchar not null, U_DID nvarchar not null, U_name nvarchar not null, U_bb nvarchar not null, U_D nvarchar, U_Co float not null, U_MC nvarchar not null, U_MN nvarchar, U_USC nvarchar not null, U_USCN nvarchar, U_BUID nvarchar, U_QN nvarchar)");
        caJdbcTemplate.update("INSERT INTO V_U (U_ID, U_DID, U_name, U_bb, U_D, U_Co, U_MC, U_MN, U_USC, U_USCN, U_BUID, U_QN) VALUES ('1L000000000013032A', 'J0000000003L300BO', 'µg/h', 'µg/h', 'µg/h relative unit', 1, 'µg/h', 'µg/h', 'µg/h', 'µg/h', NULL, NULL)");
        caJdbcTemplate.execute("CREATE TABLE TType (T_ID int, T_name NVARCHAR NOT NULL, T_mapcode NVARCHAR NOT NULL, T_code NVARCHAR NOT NULL, T_scode NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO TType (T_ID, T_name, T_mapcode, T_code, T_scode) VALUES (2, 'Case start', 'Case start', 'CaseStart', 'Case start')");

        caIncrementalLoadService.loadIncrements();
        assertThat(producer.history(),hasSize(4));
        List<TableStatusInformation> afterRun = statusDatabaseRepository.getAllTables("ca");
        for (TableStatusInformation tableStatusInformation : beforeRun){
            if (tableStatusInformation.tableName.equals("V_P")){
                TableStatusInformation information = afterRun.stream()
                        .filter(a -> a.tableName.equals("V_P"))
                        .findFirst()
                        .get();
                assertThat(tableStatusInformation.lastAccessAt,notNullValue());
                assertThat(information.lastAccessAt,notNullValue());
                assertThat(tableStatusInformation.lastAccessAt,not(information.lastAccessAt));
            }
            else if (tableStatusInformation.tableName.equals("V_T")){
                TableStatusInformation information = afterRun.stream()
                        .filter(a -> a.tableName.equals("V_T"))
                        .findFirst()
                        .get();
                assertThat(tableStatusInformation.lastUsedValue,notNullValue());
                assertThat(information.lastUsedValue,notNullValue());
                assertThat(tableStatusInformation.lastUsedValue,not(information.lastUsedValue));
            } else if(tableStatusInformation.tableName.equals("V_U")){
                TableStatusInformation information = afterRun.stream()
                        .filter(a -> a.tableName.equals("V_U"))
                        .findFirst()
                        .get();
                assertThat(tableStatusInformation.lastAccessAt,not(information.lastAccessAt));
            }
        }
    }
    @Test
    public void testInKafkaErrorStatusDbDontUpdate() throws Exception{
        List<TableStatusInformation> beforeRun = statusDatabaseRepository.getAllTables("ca");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_T");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_P");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_U");
        caJdbcTemplate.execute("DROP TABLE IF EXISTS TType");
        caJdbcTemplate.execute("CREATE TABLE V_T (T_ID NUMERIC(9, 0) NOT NULL, T_name NVARCHAR NOT NULL, T_mapcode NVARCHAR NOT NULL, T_code NVARCHAR NOT NULL, T_scode NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO V_T (T_ID, T_name, T_mapcode, T_code, T_scode) VALUES (2, 'Case start', 'Case start', 'CaseStart', 'Case start')");
        caJdbcTemplate.execute("CREATE TABLE V_P(P_ID NVARCHAR NOT NULL, P_Update DATETIME2, P_name NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO V_P (P_ID, P_Update, P_name) VALUES ('1000000000005N008B', '2017-10-04 04:05:25.5870000', 'sample')");
        caJdbcTemplate.execute("create TABLE V_U (U_ID nvarchar not null, U_DID nvarchar not null, U_name nvarchar not null, U_bb nvarchar not null, U_D nvarchar, U_Co float not null, U_MC nvarchar not null, U_MN nvarchar, U_USC nvarchar not null, U_USCN nvarchar, U_BUID nvarchar, U_QN nvarchar)");
        caJdbcTemplate.update("INSERT INTO V_U (U_ID, U_DID, U_name, U_bb, U_D, U_Co, U_MC, U_MN, U_USC, U_USCN, U_BUID, U_QN) VALUES ('1L000000000013032A', 'J0000000003L300BO', 'µg/h', 'µg/h', 'µg/h relative unit', 1, 'µg/h', 'µg/h', 'µg/h', 'µg/h', NULL, NULL)");
        caJdbcTemplate.execute("CREATE TABLE TType (T_ID int, T_name NVARCHAR NOT NULL, T_mapcode NVARCHAR NOT NULL, T_code NVARCHAR NOT NULL, T_scode NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO TType (T_ID, T_name, T_mapcode, T_code, T_scode) VALUES (2, 'Case start', 'Case start', 'CaseStart', 'Case start')");
        when(tietoallasKafkaProducer.run(Mockito.any(Schema.class), Mockito.any(GenericRecord.class),Mockito.anyString(),Mockito.anyString())).thenThrow(new RuntimeException());
        caIncrementalLoadService.producer=tietoallasKafkaProducer;
        caIncrementalLoadService.loadIncrements();
        List<TableStatusInformation> afterRun = statusDatabaseRepository.getAllTables("ca");
        for (TableStatusInformation tableStatusInformation : beforeRun){
            if (tableStatusInformation.tableName.equals("V_P")){
                TableStatusInformation information = afterRun.stream()
                        .filter(a -> a.tableName.equals("V_P"))
                        .findFirst()
                        .get();
                assertThat(tableStatusInformation.lastAccessAt,is(information.lastAccessAt));
            }
            else if (tableStatusInformation.tableName.equals("V_T")){
                TableStatusInformation information = afterRun.stream()
                        .filter(a -> a.tableName.equals("V_T"))
                        .findFirst()
                        .get();
                assertThat(tableStatusInformation.lastUsedValue,is(information.lastUsedValue));
            } else if(tableStatusInformation.tableName.equals("V_U")){
                TableStatusInformation information = afterRun.stream()
                        .filter(a -> a.tableName.equals("V_U"))
                        .findFirst()
                        .get();
                assertThat(tableStatusInformation.lastAccessAt,is(information.lastAccessAt));
            }
        }
    }

    private void setupTestStatusData() {
        statusJdbcTemplate.execute("DROP TABLE IF EXISTS integration_status");
        statusJdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_P', NULL, 'TIME_COMPARISATION', 'P_Update', NULL, '2016-02-17 04:00:48.610', 'ca', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'P_ID, P_Update, P_name', NULL, 'TIME_COMPARISATION', NULL, NULL, NULL)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_U', NULL, 'FULL_TABLE', NULL, NULL, NULL, 'ca', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'U_ID, U_DID, U_name, U_bb, U_D, U_Co, U_MC, U_MN, U_USC, U_USCN, U_BUID, U_QN', NULL, 'FULL_TABLE', NULL, NULL, NULL)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_T', 'T_ID', 'STATUSDB_TABLE_LOOKUP', null, 1, null, 'ca', null, null, null, null, null, null, null, 'T_ID, T_name,T_mapcode, T_code, T_scode', null, 'STATUSDB_TABLE_LOOKUP', null, null, null)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('TType', 'T_ID', 'STATUSDB_TABLE_LOOKUP', null, 1, null, 'ca', null, null, null, null, null, null, null, 'T_ID, T_name,T_mapcode, T_code, T_scode', null, 'STATUSDB_TABLE_LOOKUP', null, null, null)");
    }
}
