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
package fi.tietoallas.incremental.common.commonincremental.repository;

import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Repository class for selecting and manipulting integration_status table
 * @author Antti Kalliokoski
 */
@Repository
public class StatusDatabaseRepository {


    private static final String SELECT_TIME_UPDATE_TABLES = "SELECT * FROM integration_status WHERE integration_name = ? AND parameter_type='TIME_COMPARISATION'";
    private static final String SELECT_SIMPLE_UPDATE_TABLES = "SELECT * FROM integration_status WHERE integration_name = ? AND parameter_type='FULL_TABLE'";
    private static final String SELECT_FOR_XON_INT_TABLE_USAGE = "SELECT * FROM integration_status WHERE integration_name = ? AND parameter_type='HISTORY_TABLE_LOOKUP'";
    private static final String SELECT_ALL_TABLES = "SELECT * FROM integration_status WHERE integration_name = ? AND parameter_type <> 'EXCLUDE'";
    private static final String SELECT_OLDEST_UPDATE = "SELECT MIN(last_run_at) FROM integration_status WHERE integration_name = ? AND last_run_at IS NOT NULL";

    private JdbcTemplate statusDatabaseJdbcTemplate;

    public StatusDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.statusDatabaseJdbcTemplate = jdbcTemplate;
    }


    /**
     * @return List of table's status information which are comparison is based to time column
     */
    public List<TableStatusInformation> getTimetableUpdateInfo(final String integration) {
        return statusDatabaseJdbcTemplate.query(SELECT_TIME_UPDATE_TABLES, new Object[]{integration}, new TableKeyLastIdMapper());
    }

    /**
     *
     * @param integration name of integration
     * @return oldest time of all tables in spesific integration when integration was run
     */

    public Timestamp getLastRunTimeForIntegration(final String integration) {
        return statusDatabaseJdbcTemplate.query(SELECT_OLDEST_UPDATE, new Object[]{integration}, new MinDayMapper()).get(0);
    }

    /**
     * @return List of status information of all tables
     */

    public List<TableStatusInformation> getAllTables(final String integrationName) {
        return statusDatabaseJdbcTemplate.query(SELECT_ALL_TABLES, new Object[]{integrationName}, new TableKeyLastIdMapper());
    }

    /**
     * @return List of status information for tables that are imported always fully
     */

    public List<TableStatusInformation> getTablePairs(final String integration) {
        return statusDatabaseJdbcTemplate.query(SELECT_SIMPLE_UPDATE_TABLES, new Object[]{integration}, new TableKeyLastIdMapper());
    }

    /**
     *
     * @param integration name
     * @return list of history table look type tables
     */

    public List<TableStatusInformation> selectUsingXON(final String integration) {
        return statusDatabaseJdbcTemplate.query(SELECT_FOR_XON_INT_TABLE_USAGE, new Object[]{integration}, new TableKeyLastIdMapper());
    }


    /**
     * Updates new biggest value to status database
     *
     * @param integration name of integration
     * @param table       name of the table
     * @param DBOID       new biggest value
     */
    public void updateTableInformation(final String integration, final String table, final BigDecimal DBOID) {

        String sql = "UPDATE integration_status SET last_used_value=?,last_run_at=? WHERE integration_name =? AND table_name=?";
        statusDatabaseJdbcTemplate.update(sql, preparedStatement -> {

            preparedStatement.setString(3, integration);
            if (DBOID == null) {
                preparedStatement.setNull(1, Types.NUMERIC);
            } else {
                preparedStatement.setBigDecimal(1, DBOID);
            }
            preparedStatement.setTimestamp(2, Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
            preparedStatement.setString(4, table);

        });

    }

    /**
     * Updates new last_run_at value to status database
     *
     * @param table name of Table
     */

    public void updateTableTimeStamp(final String integration, final String table) {
        String sql = "UPDATE integration_status SET last_run_at=? WHERE integration_name = ? AND table_name=?";
        statusDatabaseJdbcTemplate.update(sql, ps -> {
            ps.setTimestamp(1, Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
            ps.setString(2, integration);
            ps.setString(3, table);
        });

    }


    private static final class TableKeyLastIdMapper implements RowMapper<TableStatusInformation> {

        @Override
        public TableStatusInformation mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TableStatusInformation.Builder()
                    .withTableName(resultSet.getString("table_name"))
                    .withKeyColumn(resultSet.getString("key_column"))
                    .withLastUsedValue(resultSet.getBigDecimal("last_used_value"))
                    .withLassAccessAt(resultSet.getTimestamp("LAST_RUN_AT"))
                    .withTimeColumn(resultSet.getString("time_column"))
                    .withParameterType(resultSet.getString("parameter_type"))
                    .withSearchColumn(resultSet.getString("search_column"))
                    .withQuery(resultSet.getString("incremental_query"))
                    .withOriginalDatabase(resultSet.getString("original_database"))
                    .withColumnQuery(resultSet.getString("column_query"))
                    .withInterval(resultSet.getLong("interval"))
                    .build();
        }
    }

    private static final class MinDayMapper implements RowMapper<Timestamp> {
        @Override
        public Timestamp mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getTimestamp(1);
        }
    }
}
