package fi.tietoallas.integration.common.repository;

/*-
 * #%L
 * common-java
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
import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class StatusDatabaseRepositoryTest {

    public static final String INTEGRATION_NAME = "integration_name";
    private StatusDatabaseRepository repository;
    private JdbcTemplate statusJdbcTemplate;

    @Mock
    private Environment environment;

    private String tableName="";
    @Before
    public void setUp()
    {
        when(environment.getProperty(eq("fi.datalake.statusdb.url"))).thenReturn("jdbc:h2:mem:test");
        when(environment.getProperty(eq("fi.datalake.statusdb.driver"))).thenReturn("org.h2.Driver");
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        statusJdbcTemplate = dbConfiguration.jdbcTemplate();
        repository = new StatusDatabaseRepository(statusJdbcTemplate);
        tableName= UUID.randomUUID().toString();
        statusJdbcTemplate.execute("DROP TABLE IF EXISTS integration_status");
        statusJdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");

    }

    @Test
    public void testInsert() {
        ParseTableInfo info = getTestTable();
        repository.insertTableInfo(info);
    }
    @Test
    public void testa(){
        ParseTableInfo originalInfo = getTestTable();
        ParseTableInfo updatedInfo = new ParseTableInfo.Builder(getTestTable())
                .withColumnQuery("this,is,new,column,query")
        .build();
        repository.setUpdateColumnQuery(updatedInfo);
    }
    private ParseTableInfo getTestTable(){
        return new ParseTableInfo.Builder()
                .withDescription("description")
                .withIntegrationName(INTEGRATION_NAME)
                .withTableName(tableName)
                .withColumnQuery("columnQuery")
                .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
                .withTimeColumn("time_column")
                .withLines(Arrays.asList(new LineInfo.Builder()
                        .withDataChangeColumn(Boolean.TRUE)
                        .withDataType("datatype")
                        .withDescription("description")
                        .withIsPrimary(Boolean.TRUE)
                        .withRowName("row_name")
                        .build()))
                .build();
    }
}
