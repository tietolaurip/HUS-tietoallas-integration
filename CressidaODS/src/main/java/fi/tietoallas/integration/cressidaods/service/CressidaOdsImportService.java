package fi.tietoallas.integration.cressidaods.service;

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
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.cressidaods.repository.CressidaOdsRepository;
import fi.tietoallas.integration.metadata.jpalib.domain.Table;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;
import static fi.tietoallas.integration.cressidaods.CressidaodsApplication.INTEGRATION_NAME;

/**
 * Initial setup class for CressidaOds
 */

@Service
public class CressidaOdsImportService  {

    private StatusDatabaseRepository statusDatabaseRepository;
    private Logger logger = LoggerFactory.getLogger(CressidaOdsImportService.class);
    private Environment environment;

    private JdbcTemplate hiveJdbcTemplate;
    private ColumnRepository columnRepository;
    private TableRepository tableRepository;
    private CressidaOdsRepository cressidaOdsRepository;

    public CressidaOdsImportService(@Autowired Environment environment, @Autowired ColumnRepository columnRepository,
                                    @Autowired TableRepository tableRepository, @Autowired CressidaOdsRepository cressidaOdsRepository,
                                    @Autowired StatusDatabaseRepository statusDatabaseRepository){
        this.environment=environment;
        if (statusDatabaseRepository == null) {
            this.statusDatabaseRepository = new StatusDatabaseRepository(new DbConfiguration(environment).jdbcTemplate());
        }
        DbConfiguration dbConfiguration = new DbConfiguration(environment);
        this.hiveJdbcTemplate =dbConfiguration.hiveJdbcTemplate(dbConfiguration.hiveDatasource());
        this.columnRepository=columnRepository;
        this.tableRepository=tableRepository;
        this.cressidaOdsRepository=cressidaOdsRepository;
    }
    public void initialSetup(final String integration, boolean createHiveDb) throws Exception {
        String storageLocation = environment.getProperty("fi.datalake.storage.adl.location");
        if (createHiveDb) {
            createHiveDB();
        }
        List<String> allTables = cressidaOdsRepository.getAllTables();
        for (String table : allTables) {
            logger.info("creating table " + table);
            try {
                String columns = cressidaOdsRepository.fetchColumns(table);
                Table jpaTable = new Table(INTEGRATION_NAME, convertTableName(table));
                jpaTable.setOrigTableName(table);
                tableRepository.saveAndFlush(jpaTable);
                columnRepository.save(cressidaOdsRepository.getColumnInfo(table));
                columnRepository.flush();
                statusDatabaseRepository.insertTableInfo(new ParseTableInfo.Builder()
                        .withColumnQuery(columns)
                        .withTableType(ParseTableInfo.TableType.TIME_COMPARISATION)
                        .withIntegrationName(INTEGRATION_NAME)
                        .withTableName(table)
                        .withTimeColumn("PAIVITYSHETKI_ODS")
                        .withLines(new ArrayList<>())
                .build());
                String stagingStatement = cressidaOdsRepository.createStatement(table, INTEGRATION_NAME, storageLocation,true);
                hiveJdbcTemplate.execute(stagingStatement);
                String storageStatement = cressidaOdsRepository.createStatement(table,INTEGRATION_NAME,storageLocation,false);
                hiveJdbcTemplate.execute(storageStatement);
                logger.info(table + " created");
            } catch (Exception e){
                logger.error("error handling table "+table,e);
            }
        }
    }

    private void createHiveDB(){
        String hiveStrorage = "varasto_"+INTEGRATION_NAME+"_historia_log";
        String sql = "CREATE DATABASE IF NOT EXISTS staging_"+INTEGRATION_NAME;
        hiveJdbcTemplate.execute(sql);
        hiveJdbcTemplate.equals("CREATE DATABASE IF NOT EXISTS "+hiveStrorage);
    }
}
