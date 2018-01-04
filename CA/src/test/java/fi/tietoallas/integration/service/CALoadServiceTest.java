package fi.tietoallas.integration.service;

/*-
 * #%L
 * ca
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
import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.domain.Column;
import fi.tietoallas.integration.common.mapper.TableCreateScriptMapper;
import fi.tietoallas.integration.common.repository.ColumnRepository;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.repository.TableRepository;
import fi.tietoallas.integration.configuration.CADBConfig;
import fi.tietoallas.integration.repository.CaRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
@RunWith(SpringRunner.class)
@EnableJpaRepositories("fi.tietoallas.integration.common")
@SpringBootTest(classes = {DbConfiguration.class, TableRepository.class,ColumnRepository.class})
public class CALoadServiceTest {


    @Mock
    Environment environment;
    @MockBean
    CADBConfig cadbConfig;

    private StatusDatabaseRepository statusDatabaseRepository;
    @Mock
    private CaRepository caRepository;
    private JdbcTemplate statusDbJdbcTemplate;
    private JdbcTemplate caJdbcTemplate;
    private JdbcTemplate hiveJdbcTemplate;
    @Autowired
    private ColumnRepository columnRepository;
    @Autowired
    private TableRepository tableRepository;

    private CALoadService caLoadService;




    @Before
    public void setUp(){
        when(environment.getProperty(eq("fi.datalake.sourcedb.url"))).thenReturn("jdbc:h2:~/test");
        when(environment.getProperty(eq("fi.datalake.sourcedb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.statusdb.url"))).thenReturn("jdbc:h2:~/test");
        when(environment.getProperty(eq("fi.datalake.statusdb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.targetdb.url"))).thenReturn("jdbc:h2:~/hive");
        when(environment.getProperty(eq("fi.datalake.targetdb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.storage.adl.location"))).thenReturn("testPath");
        when(caRepository.getAllViews()).thenReturn(Arrays.asList("A"));
        when(caRepository.getColumnInfo("A")).thenReturn(Arrays.asList(new Column.Builder()
                .withDataSetName("ca")
                .withHiveType("STRING")
                .withName("a_coloumn")
                .withOrigColumnName("A_Column")
                .withOrigType("varchar(20)")
                .withTableName("a")
        .build()));
        CADBConfig cadbConfig = new CADBConfig(environment);
        caJdbcTemplate= cadbConfig.sourceJdbcTemplate();
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        statusDbJdbcTemplate = dbConfiguration.jdbcTemplate();
        statusDatabaseRepository = new StatusDatabaseRepository(statusDbJdbcTemplate);
        caJdbcTemplate.execute("DROP TABLE IF EXISTS A ");
        caJdbcTemplate.execute("CREATE TABLE A (A_Column VARCHAR(20))");
        statusDbJdbcTemplate.execute("DROP TABLE IF EXISTS integration_status");
        String stagingStatement ="create table staging_ca.a (a_column varchar(255),allas__pvm_partitio varchar(255))";
        statusDbJdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");
        when(caRepository.createStatement(anyString(),anyString(),eq(Boolean.TRUE),anyString(), anyString())).thenReturn(stagingStatement);
        String storageStatement = "create table varasto_ca_historia_log.a (a_column varchar(255),allas__pvm_partitio varchar(255))";
        when(caRepository.createStatement(anyString(),anyString(),eq(Boolean.FALSE),anyString(), anyString())).thenReturn(storageStatement);
        when(caRepository.getColumnOnlyNames(eq("A"))).thenReturn("A_Column");
        hiveJdbcTemplate=dbConfiguration.testHiveDataSource();
        hiveJdbcTemplate.execute("drop table if EXISTS staging_ca.a");
        hiveJdbcTemplate.execute("drop table if EXISTS varasto_ca_historia_log.a");
        hiveJdbcTemplate.execute("drop SCHEMA if exists staging_ca");
        hiveJdbcTemplate.execute("create SCHEMA staging_ca");
        hiveJdbcTemplate.execute("DROP SCHEMA if EXISTS varasto_ca_historia_log");
        hiveJdbcTemplate.execute("CREATE SCHEMA varasto_ca_historia_log");

    }
    @Test
    public void testCreateStatement(){
        String stagingStatement = caJdbcTemplate.query("SELECT A_column from A", new TableCreateScriptMapper("A", "ca", true, null));
        String storageStatement = caJdbcTemplate.query("SELECT A_column from A", new TableCreateScriptMapper("A", "ca", false, "HDFS://testi/"));

        assertThat(stagingStatement,is("create table staging_ca.a (`a_column` STRING,allas__pvm_partitio STRING) stored as orc LOCATION 'adl:///staging/ca/a'"));
        assertThat(storageStatement,is ("create table varasto_ca_historia_log.a (`a_column` STRING,allas__pvm_partitio STRING) stored as orc LOCATION 'HDFS://testi/ca/a'"));
    }
    @Test
    public void testTableCreation()throws Exception{
        caLoadService = new CALoadService(environment,statusDatabaseRepository,caRepository,columnRepository,tableRepository);
        caLoadService.hiveJdbcTemplate=hiveJdbcTemplate;
        caLoadService.initialSetup(false);
        String columnName = hiveJdbcTemplate.query("select * from staging_ca.a", new MetadataMapper());
        assertThat(columnName,is("a_column"));
        columnName= hiveJdbcTemplate.query("select * from varasto_ca_historia_log.a",new MetadataMapper());
        assertThat(columnName,is("a_column"));


    }

    private class MetadataMapper implements ResultSetExtractor<String> {
        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
           return rs.getMetaData().getColumnName(1);
        }
    }
}
