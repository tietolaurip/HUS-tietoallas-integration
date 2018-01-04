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
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.domain.Column;
import fi.tietoallas.integration.common.domain.Table;
import fi.tietoallas.integration.common.repository.ColumnRepository;
import fi.tietoallas.integration.common.repository.TableRepository;
import fi.tietoallas.integration.repository.CaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;

@Service
public class CALoadService  {

    public static final String INTEGRATION_NAME = "ca";

    private CaRepository caRepository;

    private Environment environment;

    private StatusDatabaseRepository statusDatabaseRepository;
    @Autowired
    @Qualifier(DbConfiguration.HIVE_JDBC_TEMPLATE)
    JdbcTemplate hiveJdbcTemplate;
    private ColumnRepository columnRepository;
    private TableRepository tableRepository;

    private Logger logger = LoggerFactory.getLogger(CALoadService.class);

    public CALoadService(@Autowired Environment environment,
                         @Autowired StatusDatabaseRepository statusDatabaseRepository,
                         @Autowired CaRepository caRepository,
                         @Autowired ColumnRepository columnRepository,
                         @Autowired TableRepository tableRepository) {
        this.environment=environment;
        this.statusDatabaseRepository=statusDatabaseRepository;
        this.caRepository=caRepository;
        this.columnRepository=columnRepository;
        this.tableRepository=tableRepository;
    }

    public void initialSetup(boolean createHiveDB) throws Exception {

        if (createHiveDB){
            createHiveDb();
        }
        List<String> allViews = caRepository.getAllViews().stream()
                .collect(Collectors.toList());
        String storageLocation = environment.getProperty("fi.datalake.storage.adl.location");
        for (String table : allViews) {
            logger.info("creating table " + table);
            String columnQuery = caRepository.getColumnOnlyNames(table);

            statusDatabaseRepository.insertTableInfo(new ParseTableInfo.Builder()
                    .withTableName(table)
                    .withIntegrationName(INTEGRATION_NAME)
                    .withColumnQuery(columnQuery)
                    .withLines(new ArrayList<>())
                    .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
            .build());
            logger.info("status db saved "+convertTableName(table));
            Table jpaTable = new Table(INTEGRATION_NAME,convertTableName(table));
            jpaTable.setOrigTableName(table);
            jpaTable.setMetadataLastUpdated(new Date());
            tableRepository.save(jpaTable);
            logger.info("metadata table saved" +convertTableName(table));
            List<Column> columnList = caRepository.getColumnInfo(table);
            columnRepository.save(columnList);
            logger.info("rows table saved " +convertTableName(table));
            String statement = caRepository.createStatement(table, INTEGRATION_NAME, true,columnQuery,storageLocation);
            hiveJdbcTemplate.execute(statement);
            String storageStatement = caRepository.createStatement(table, INTEGRATION_NAME, false,columnQuery,storageLocation);
            hiveJdbcTemplate.execute(storageStatement);
            logger.info(table + " created");
        }
    }
    private void createHiveDb(){
        String sql = "CREATE DATABASE IF NOT EXISTS staging_ca ";
        hiveJdbcTemplate.execute(sql);
        String storageSql = "CREATE DATABASE IF NOT EXISTS varasto_ca_historia_log";
        hiveJdbcTemplate.execute(storageSql);

    }


}
