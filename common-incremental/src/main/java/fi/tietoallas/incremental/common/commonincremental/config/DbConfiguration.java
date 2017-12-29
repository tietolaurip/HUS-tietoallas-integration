
/*-
 * #%L
 * common-incremental
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

package fi.tietoallas.incremental.common.commonincremental.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * DBconfiguration for Status database
 *
 * @author Antti Kalliokoski
 */
public class DbConfiguration {

    public static final String STATUS_JDBC_TEMPLATE = "statusJdbcTemplate";
    public static final String STATUS_DB = "statusDb";
    private Environment environment;

    public DbConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = STATUS_DB)
    public DataSource msSqlDatasource() {
        HikariDataSource hikariDataSource = new HikariDataSource();

        hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.statusdb.url"));
        hikariDataSource.setUsername(environment.getProperty("fi.datalake.statusdb.username"));
        hikariDataSource.setPassword(environment.getProperty("fi.datalake.statusdb.password"));
        hikariDataSource.setDriverClassName(environment.getProperty("fi.datalake.statusdb.driver"));
        return hikariDataSource;
    }

    /**
     * @return JdbcTemplate to statusdatabase
     */
    @Bean(name = STATUS_JDBC_TEMPLATE)
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(msSqlDatasource());
    }

}
