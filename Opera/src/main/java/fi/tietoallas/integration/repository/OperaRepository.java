package fi.tietoallas.integration.repository;

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

import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo.TableType;
import fi.tietoallas.integration.common.mapper.ColumnNamesResultSetExtractor;
import fi.tietoallas.integration.common.mapper.TableCreateScriptMapper;
import fi.tietoallas.integration.configuration.SourceDbConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OperaRepository {

    @Qualifier(SourceDbConfiguration.SOURCE_JDBC_TEMPLATE)
    private JdbcTemplate jdbcTemplate;

    private static final String GET_ALL_VIWS="SELECT name FROM sys.objects WHERE type = 'V'";

    public OperaRepository(@Qualifier(SourceDbConfiguration.SOURCE_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }
    public List<String> getAllDbViews(){
        return jdbcTemplate.query(GET_ALL_VIWS,new ViewNameMapper());
    }

    public String getColumnOnlyNames(final String table) {
        String sql = "select * from "+table+" where 1=0";
        return jdbcTemplate.query(sql,new ColumnNamesResultSetExtractor());
    }
    public String createStatement(String table, String integration, boolean staging,String columns,String storageLocation) {
        String sql = "select "+ columns +" from "+table+" where 1=0";
        return jdbcTemplate.query(sql,new TableCreateScriptMapper(table,integration,staging,storageLocation));
    }

    public ParseTableInfo getPatientTableInfo(final String table,final String integration,final String columnQuery) {
        return new ParseTableInfo.Builder()
                .withTableType(TableType.HISTORY_TABLE_LOOKUP)
                .withColumnQuery(columnQuery)
                .withTableName(table)
                .withLines(getLineInfo(table))
                .build();
    }

    private List<LineInfo> getLineInfo(String table) {
        String sql = "select * from "+table + " where 1=0";
        return jdbcTemplate.query(sql,new LineInfoMapper());
    }

    private static final class ViewNameMapper implements RowMapper<String>{

        @Override
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(1);
        }
    }


    private static final class BiggestValueMapper implements RowMapper<BigDecimal>{

        @Override
        public BigDecimal mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getBigDecimal(1);
        }
    }

    private class LineInfoMapper implements ResultSetExtractor<List<LineInfo>>{

        @Override
        public List<LineInfo> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<LineInfo> lineInfos = new ArrayList<>();
            for (int i=1; i<=metaData.getColumnCount(); i++){
                lineInfos.add(new LineInfo.Builder()
                        .withRowName(metaData.getColumnName(i))
                        .withDataType(JDBCType.valueOf(metaData.getColumnType(i)).getName())
                        .withIsPrimary(Boolean.FALSE)
                        .withDataChangeColumn(Boolean.FALSE)
                        .build());
            }
            return lineInfos;
        }
    }
}
