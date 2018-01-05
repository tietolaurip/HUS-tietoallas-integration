package fi.tietoallas.integration.configuration;

/*-
 * #%L
 * opera
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SourceDbConfiguration {
    public static final String SOURCE_DB = "sourceDb";
    public static final String SOURCE_JDBC_TEMPLATE = "operaJdbcTemplate";

    @Autowired
    private Environment environment;

    @Bean(name =SOURCE_DB)
    public DataSource  caDataSource(){
        return DataSourceBuilder.create()
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .url(environment.getProperty("fi.datalake.sourcedb.url"))
                .username(environment.getProperty("fi.datalake.sourcedb.username"))
                .password(environment.getProperty("fi.datalake.sourcedb.password"))
                .build();
    }
    @Bean(name = SOURCE_JDBC_TEMPLATE)
    public JdbcTemplate sourceJdbcTemplate(@Qualifier(SOURCE_DB) DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

}
