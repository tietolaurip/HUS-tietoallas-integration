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
package fi.tietoallas.integration.caresuiteincremental.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration class for Caresuite DB
 * @author xxkallia
 */
@Configuration
public class CaresuiteDbConfig {

    Environment environment;

    public CaresuiteDbConfig(@Autowired Environment environment){
        this.environment=environment;
    }

    @Bean
    @Primary
    public DataSource dataSource(){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setConnectionTimeout(600000);
        hikariDataSource.setDriverClassName(environment.getProperty("fi.datalake.sourcedb.driver"));
        hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.sourcedb.url"));
        hikariDataSource.setUsername(environment.getProperty("fi.datalake.sourcedb.username"));
        hikariDataSource.setPassword(environment.getProperty("fi.datalake.sourcedb.password"));
        return hikariDataSource;
    }
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }
}
