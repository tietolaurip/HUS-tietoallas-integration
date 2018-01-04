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
import fi.tietoallas.integration.common.domain.KeyTableName;
import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.domain.TableStatusInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class StatusDatabaseRepository {


    private static final String INSERT_TABLE_INFO = "INSERT INTO integration_status (table_name,key_column,parameter_type,time_column,integration_name,original_database,column_query) VALUES (?,?,?,?,?,?,?)";
    private static final String UPDATE_COLUMN_QUERY="UPDATE integration_status set column_query=? where table_name=? and integration_name=?";


    private JdbcTemplate statusDatabaseJdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(StatusDatabaseRepository.class);

    public StatusDatabaseRepository(@Qualifier(DbConfiguration.STATUS_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate){
        this.statusDatabaseJdbcTemplate= jdbcTemplate;
    }

    public void setUpdateColumnQuery(final ParseTableInfo parseTableInfo){
        statusDatabaseJdbcTemplate.update(UPDATE_COLUMN_QUERY,new Object[]{parseTableInfo.columnQuery,parseTableInfo.tableName,parseTableInfo.integrationName});
    }


       public void crateExtraStatusTables(List<KeyTableName> keyTableNames){
        logger.info("size of keytablenames "+keyTableNames.size());
        for (KeyTableName keyTableName: keyTableNames){
            String sql = "create table "+keyTableName.table +" ("+keyTableName.key+" nvarchar(255))";
            logger.info("running sql "+sql);
            statusDatabaseJdbcTemplate.execute(sql);
        }
    }

    /**
     * Creates new status entry for status database for single table
     *
     * @param info data created from metadata CSV (for one table)
     */
    public void insertTableInfo(ParseTableInfo info) {
        Optional<LineInfo> lineInfo = info.lines.stream()
                .filter(l -> l.isPrimary)
                .findFirst();
            if (lineInfo.isPresent()) {
                statusDatabaseJdbcTemplate.update(INSERT_TABLE_INFO, info.tableName, lineInfo.get().rowName, info.tableType.toString(), info.timeColunm,info.integrationName,info.originalDatabase,info.columnQuery);
            } else {
                statusDatabaseJdbcTemplate.update(INSERT_TABLE_INFO, info.tableName, null, info.tableType.toString(), info.timeColunm,info.integrationName,info.originalDatabase,info.columnQuery);
            }
    }

}
