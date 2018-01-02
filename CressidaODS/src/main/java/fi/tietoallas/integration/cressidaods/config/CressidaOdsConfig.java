package fi.tietoallas.integration.cressidaods.config;

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
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class CressidaOdsConfig {

    public static final String CRESSIDA_ODS_DB = "cressidaODS";
    public static final String CRESSIDA_ODBC_JDBC_TEMPLATE = "cressidaODSJdbcTemplate";
    private Environment environment;

    public CressidaOdsConfig(@Autowired Environment environment){
        this.environment=environment;
    }

    @Bean(name = CRESSIDA_ODS_DB)
    public DataSource getDatasource(){
        HikariDataSource hikariDataSource = new HikariDataSource();
        if (environment.getProperty("fi.datalake.sourcedb.driver") != null){
           hikariDataSource.setDriverClassName(environment.getProperty("fi.datalake.sourcedb.driver"));
        }else {
            hikariDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        }
        hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.sourcedb.url"));
        hikariDataSource.setUsername(environment.getProperty("fi.datalake.sourcedb.username"));
        hikariDataSource.setPassword(environment.getProperty("fi.datalake.sourcedb.password"));
        return hikariDataSource;
    }
    @Bean(name = CRESSIDA_ODBC_JDBC_TEMPLATE)
    public JdbcTemplate jdbcTemplate()
    {
        return new JdbcTemplate(getDatasource());
    }
}
