package fi.tietoallas.integration.initializestatusdb.repository;

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
import fi.tietoallas.integration.initializestatusdb.domain.TableStatusInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class StatusDbRepository {

    private JdbcTemplate jdbcTemplate;
    
    public StatusDbRepository(@Autowired JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public List<TableStatusInfo> getAllTables(final String integration, final String originalDatabase){
        if (StringUtils.isBlank(originalDatabase)){
          return  jdbcTemplate.query("select table_name,parameter_type,key_column,time_column from integration_status where parameter_type <> 'EXCLUDE' and  integration_name = ?",new Object[]{integration},new TableNameMapper());
        }
        return jdbcTemplate.query("select table_name,parameter_type,key_column,time_column from integration_status where parameter_type <> 'EXCLUDE' and integration_name =? and original_database=? ",new Object[]{integration,originalDatabase},new TableNameMapper());
    }
    public void updateLastRunAt(final String integration, final String originalDatabase,final String table, final Timestamp lastRunAt){
        if (StringUtils.isBlank(originalDatabase)){
            jdbcTemplate.update("update integration_status set last_run_at=? where integration_name=? and table_name=?",lastRunAt,integration,table);
        } else {
            jdbcTemplate.update("update integration_status set last_run_at=? where integration_name=? and table_name=? and original_database = ?",lastRunAt,integration,table,originalDatabase);
        }
    }
    public void updateLastUsedValue(final String integration, final String originalDatabase,final String table, final BigDecimal lastUsedValue, final Timestamp lastRunAt){
        if (StringUtils.isBlank(originalDatabase)){
            jdbcTemplate.update("update integration_status set last_used_value=?,last_run_at=? where integration_name=? and table_name=?",lastUsedValue,lastRunAt,integration,table);
        } else {
            jdbcTemplate.update("update integration_status set last_used_value=?,last_run_at=?  where integration_name=? and table_name=? and original_database = ?",lastUsedValue,lastRunAt,integration,table,originalDatabase);
        }
    }

    public void updateWithLastUsedAndColumnQuery(String integration, String originalDatabase, String tableName, Timestamp lastUpdateAt, String columnQuery) {
        if (StringUtils.isEmpty(originalDatabase)){
            jdbcTemplate.update("update integration_status set last_run_at=?, column_query=? where integration_name=? and table_name=?",new Object[]{lastUpdateAt,columnQuery,integration,tableName});
        } else {
            jdbcTemplate.update("update integration_status set last_run_at=?, column_query=? where integration_name=? and table_name=? and original_database = ?",new Object[]{lastUpdateAt,columnQuery,integration,tableName,originalDatabase});

        }
    }
    public void updateLastUsedValueAndColumQuery(final String integration, final String originalDatabase,final String table, final BigDecimal lastUsedValue, final Timestamp lastRunAt, final String columnQuery){
        if (StringUtils.isBlank(originalDatabase)){
            jdbcTemplate.update("update integration_status set last_used_value=?,last_run_at=?,column_query=? where integration_name=? and table_name=?",lastUsedValue,lastRunAt,columnQuery,integration,table);
        } else {
            jdbcTemplate.update("update integration_status set last_used_value=?,last_run_at=?,column_query=? where integration_name=? and table_name=? and original_database = ?",lastUsedValue,lastRunAt,columnQuery,integration,table,originalDatabase);
        }
    }
    private class TableNameMapper implements RowMapper<TableStatusInfo> {
        @Override
        public TableStatusInfo mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TableStatusInfo.Builder()
                    .withKeyColoumn(resultSet.getString("key_column"))
                    .withParameterType(resultSet.getString("parameter_type"))
                    .withTableName(resultSet.getString("table_name"))
                    .withTimeColumn(resultSet.getString("time_column"))
                    .build();
        }
    }
}
