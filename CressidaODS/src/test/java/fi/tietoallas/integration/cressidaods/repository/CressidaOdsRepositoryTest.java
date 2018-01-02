package fi.tietoallas.integration.cressidaods.repository;

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

import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.cressidaods.config.CressidaOdsConfig;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
public class CressidaOdsRepositoryTest {

    @Mock
    private Environment environment;

    private StatusDatabaseRepository statusDatabaseRepository;
    private CressidaOdsRepository cressidaOdsRepository;
    private JdbcTemplate cressidaOdbcTemplate;
    private JdbcTemplate statusDbTemplate;

    @Before
    public void setUp(){
        when(environment.getProperty(eq("fi.datalake.sourcedb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.sourcedb.url"))).thenReturn("jdbc:h2:~/test");
        when(environment.getProperty(eq("fi.datalake.statusdb.url"))).thenReturn("jdbc:h2:~/test");
        when(environment.getProperty(eq("fi.datalake.statusdb.driver"))).thenReturn("org.h2.Driver");
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        CressidaOdsConfig cressidaOdsConfig = new CressidaOdsConfig(environment);
        cressidaOdbcTemplate= cressidaOdsConfig.jdbcTemplate();
        statusDbTemplate=dbConfiguration.jdbcTemplate();
        statusDatabaseRepository=new StatusDatabaseRepository(statusDbTemplate);
        cressidaOdsRepository=new CressidaOdsRepository(cressidaOdbcTemplate);
        cressidaOdbcTemplate.execute("drop table IF EXISTS ods");
        cressidaOdbcTemplate.execute("create table ods (NUMERO VARCHAR(255),N_O VARCHAR(255),A_N VARCHAR(255),PAIVITYSHETKI_ODS TIMESTAMP)");
        cressidaOdbcTemplate.update("insert into ods (NUMERO,N_O,A_N,PAIVITYSHETKI_ODS) VALUES ('otsikkolinkki numero','otsikko numero','attribuutti numero','2020-02-02 10:10:10.000')");
    }
    @Test
    public void testFetchColumns() throws Exception{
        String ods = cressidaOdsRepository.fetchColumns("ods");
        assertThat(ods,is("NUMERO,N_O,A_N,PAIVITYSHETKI_ODS"));
    }
    @Test
    public void testGetColumnInfo(){
        List<Column> columns = cressidaOdsRepository.getColumnInfo("ods");
        assertThat(columns,hasSize(4));
        assertThat(columns.get(0).getName(),is("numero"));
        assertThat(columns.get(0).getDataSetName(),is("cressidaods"));
        assertThat(columns.get(0).getOrigColumnName(),is("NUMERO"));
        assertThat(columns.get(0).getPseudonymizationFunction(),nullValue());
    }
    @Test
    public void testCreateStatement(){
        String statement = cressidaOdsRepository.createStatement("ods", "cressidaods", "adl://testa", true);
        assertThat(statement,is("create table staging_cressidaods.ods (`numero` STRING,`n_o` STRING,`a_n` STRING,`paivityshetki_ods` TIMESTAMP,allas__pvm_partitio STRING) stored as orc LOCATION 'adl:///staging/cressidaods/ods'"));
        statement = cressidaOdsRepository.createStatement("ods", "cressidaods", "adl://testa/", false);
        assertThat(statement,is("create table varasto_cressidaods_historia_log.ods (`numero` STRING,`n_o` STRING,`a_n` STRING,`paivityshetki_ods` TIMESTAMP,allas__pvm_partitio STRING) stored as orc LOCATION 'adl://testa/cressidaods/ods'"));
    }
}
