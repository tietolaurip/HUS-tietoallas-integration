package fi.tietoallas.integration.common.services;

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
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class LoadService {

    private Environment environment;
    private StatusDatabaseRepository statusRepository;
    private JdbcTemplate jdbcTemplate;
    private DbConfiguration dbConfiguration;


    public LoadService(Environment environment){
        this.environment = environment;
        dbConfiguration = new DbConfiguration(environment);
        jdbcTemplate = dbConfiguration.hiveJdbcTemplate(dbConfiguration.hiveDatasource());
        JdbcTemplate statusJdbc = new JdbcTemplate(dbConfiguration.msSqlDatasource());
        statusRepository = new StatusDatabaseRepository(statusJdbc);
    }

    public void initialSetup(final String integration, boolean useViews) throws Exception {

    }

}
