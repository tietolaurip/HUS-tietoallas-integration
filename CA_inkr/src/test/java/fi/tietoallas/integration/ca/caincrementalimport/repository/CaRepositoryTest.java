package fi.tietoallas.integration.ca.caincrementalimport.repository;

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

import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.ca.caincrementalimport.configuration.CaDbConfig;
import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class CaRepositoryTest {

    private StatusDatabaseRepository statusDatabaseRepository;
    private CaRepository caRepository;
    private JdbcTemplate statusJdbcTemplate;
    private JdbcTemplate caJdbcTemplate;
    @Mock
    private Environment environment;

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
    }
    @Test
    public void testGetColumnNames(){
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_T");
        caJdbcTemplate.execute("CREATE TABLE V_T (T_ID NUMERIC(9, 0) NOT NULL, T_name NVARCHAR NOT NULL, T_mapcode NVARCHAR NOT NULL, T_code NVARCHAR NOT NULL, T_scode NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO V_T (T_ID, T_name, T_mapcode, T_code, T_scode) VALUES (1, 'Case start', 'Case start', 'CaseStart', 'Case start')");
        List<SchemaGenerationInfo> schemaGenerationInfos = caRepository.getColumnInfo("V_T", "T_ID, T_name,T_mapcode, T_code, T_scode");
        SchemaGenerationInfo schemaGenerationInfo = schemaGenerationInfos.get(0);
        assertThat(schemaGenerationInfo.names,hasSize(5));
        assertThat( schemaGenerationInfo.names,is(Arrays.asList("t_id", "t_name", "t_mapcode", "t_code", "t_scode")));
        assertThat(schemaGenerationInfo.types,is(Arrays.asList("bytes","string","string","string","string")));
        assertThat(schemaGenerationInfo.params.get(0).get("logicalType"),is("decimal"));
        assertThat(schemaGenerationInfo.params.get(0).get("precision"),is("9"));
        assertThat(schemaGenerationInfo.params.get(0).get("scale"),is("0"));
        assertThat(schemaGenerationInfo.params.get(1).isEmpty(),is(true));
        assertThat(schemaGenerationInfo.params.get(2).isEmpty(),is(true));
        assertThat(schemaGenerationInfo.params.get(3).isEmpty(),is(true));
        assertThat(schemaGenerationInfo.params.get(4).isEmpty(),is(true));
    }
    @Test
    public void testGetNewData(){
        caJdbcTemplate.execute("DROP TABLE IF EXISTS V_T");
        caJdbcTemplate.execute("CREATE TABLE V_T (T_ID NUMERIC(9, 0) NOT NULL, T_name NVARCHAR NOT NULL, T_mapcode NVARCHAR NOT NULL, T_code NVARCHAR NOT NULL, T_scode NVARCHAR NOT NULL)");
        caJdbcTemplate.update("INSERT INTO V_T (T_ID, T_name, T_mapcode, T_code, T_scode) VALUES (1, 'Case start', 'Case start', 'CaseStart', 'Case start')");
        List<GenericRecord> genericRecords = caRepository.getNewData("select T_ID, T_name, T_mapcode, T_code, T_scode from V_T", "V_T");
        assertThat(genericRecords,hasSize(1));
        assertThat(genericRecords.get(0),notNullValue());
    }

    private void setupTestStatusData() {
        statusJdbcTemplate.execute("DROP TABLE IF EXISTS integration_status");
        statusJdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_P', NULL, 'TIME_COMPARISATION', 'P_Update', NULL, '2016-02-17 04:00:48.610', 'ca', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'P_ID, P_Update, P_name', NULL, 'TIME_COMPARISATION', NULL, NULL, NULL)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_U', NULL, 'FULL_TABLE', NULL, NULL, NULL, 'ca', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ADMTYPEDBOID,ADMTYPEDESC,ISDELETED', NULL, 'FULL_TABLE', NULL, NULL, NULL)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('T_ID', 'DBOID', 'STATUSDB_TABLE_LOOKUP', null, 1, null, 'ca', null, null, null, null, null, null, null, 'T_ID, T_name,T_mapcode, T_code, T_scode', null, 'STATUSDB_TABLE_LOOKUP', null, null, null)");
    }
}
