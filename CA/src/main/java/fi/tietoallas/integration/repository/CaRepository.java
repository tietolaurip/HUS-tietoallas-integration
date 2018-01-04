package fi.tietoallas.integration.repository;

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
import fi.tietoallas.integration.common.mapper.ColumnNamesResultSetExtractor;
import fi.tietoallas.integration.common.mapper.TableCreateScriptMapper;
import fi.tietoallas.integration.configuration.CADBConfig;
import fi.tietoallas.integration.common.domain.Column;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;
import static fi.tietoallas.integration.service.CALoadService.INTEGRATION_NAME;

@Repository
public class CaRepository {

    private JdbcTemplate jdbcTemplate;
    private static final String GET_ALL_VIWS = "SELECT name FROM sys.objects WHERE type = 'V'";

    public CaRepository(@Qualifier(CADBConfig.SOURCE_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getAllViews() {
        return jdbcTemplate.query(GET_ALL_VIWS, new FistStringRowMapper());
    }


    public String createStatement(String table, String integration, boolean staging, String columns,String storageLocation) {
        String sql = "select " + columns + " from " + table ;
        return jdbcTemplate.query(sql, new TableCreateScriptMapper(table, integration, staging,storageLocation));
    }

    public String getColumnOnlyNames(final String table) {
        String sql = "select top(1) * from " + table ;
        return jdbcTemplate.query(sql, new ColumnNamesResultSetExtractor());
    }

    public List<Column> getColumnInfo(String table) {
        String sql = "select top(1) * from " + table ;
        return jdbcTemplate.query(sql, new LineInfoMapper(table));
    }

    private class LineInfoMapper implements ResultSetExtractor<List<Column>> {

        private String tableName;

        public LineInfoMapper(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public List<Column> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Column> lineInfos = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                Column column = new Column(INTEGRATION_NAME, convertTableName(tableName), convertTableName(metaData.getColumnName(i)));
                column.setOrigColumnName(metaData.getColumnName(i));
                column.setOrigType(JDBCType.valueOf(metaData.getColumnType(i)).getName());
                lineInfos.add(column);
            }
            return lineInfos;
        }
    }

    private static final class FistStringRowMapper implements RowMapper<String> {

        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(1);
        }
    }

}
