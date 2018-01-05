package fi.tietoallas.integration.initializestatusdb.config;

/*-
 * #%L
 * initialize-status-db
 * %%
 * Copyright (C) 2017 - 2018 Pivotal Software, Inc.
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
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;

@Configuration
public class DbConfig {
    public static final String HIVE_JDBC_TEMPLATE="hive_jdbc_Template";
    public static final String STATUS_DB_JDBC_TEMPLATE="status_db_jdbc_template";
    private static final String STATUS_DB = "status_db";
    private static final String HIVE_DB="hive_db";
    @Autowired
    private Environment environment;
    @Primary
    @Bean(name = STATUS_DB)
    public DataSource dataSource(){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.statusdb.url"));
        hikariDataSource.setUsername(environment.getProperty("fi.datalake.statusdb.username"));
        hikariDataSource.setPassword(environment.getProperty("fi.datalake.statusdb.password"));
        hikariDataSource.setDriverClassName("fi.datalake.statusdb.driver");
        return hikariDataSource;
    }
    @Bean(name = HIVE_DB)
    public DataSource hiveDataSource(){
        SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();
        if (environment.getProperty("fi.datalake.targetdb.driver") == null) {
            simpleDriverDataSource.setDriverClass(org.apache.hive.jdbc.HiveDriver.class);
            simpleDriverDataSource.setUrl("jdbc:hive2://localhost:10001/;transportMode=http");
        } else {
            try {
            simpleDriverDataSource.setDriverClass((Class<? extends Driver>) Class.forName(environment.getProperty("fi.datalake.targedb.driver")));
            simpleDriverDataSource.setUrl(environment.getProperty("fi.datalake.targedb.driver"));
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        return simpleDriverDataSource;
    }
    @Primary
    @Bean(name = STATUS_DB_JDBC_TEMPLATE)
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }
    @Bean(name = HIVE_JDBC_TEMPLATE)
    public JdbcTemplate hiveJdbcTemplate(){
        return new JdbcTemplate(hiveDataSource());
    }
}
