package fi.tietoallas.integration.cressidaods.repository;

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
import fi.tietoallas.integration.common.mapper.TableCreateScriptMapper;
import fi.tietoallas.integration.cressidaods.config.CressidaOdsConfig;
import fi.tietoallas.integration.cressidaods.service.CressidaOdsImportService;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;
import static fi.tietoallas.integration.cressidaods.CressidaodsApplication.INTEGRATION_NAME;

/**
 * Repository class for cressida ods
 */
@Repository
public class CressidaOdsRepository {

    private JdbcTemplate jdbcTemplate;

    public CressidaOdsRepository(@Qualifier(CressidaOdsConfig.CRESSIDA_ODBC_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public List<String> getAllTables(){
        return jdbcTemplate.query("select table_name from all_tables where tablespace_name='MDODS_TBL'",new TableNameMapper());
    }


    public String fetchColumns(String tableName) throws Exception {
        String sql = "select * from "+tableName+" where 1=0";
        return jdbcTemplate.query(sql, new TableColumnMapper());
    }
    public String createStatement(String table,String integration,String location,boolean staging){
        String sql = "select * from "+table+" where 1=0";
        return jdbcTemplate.query(sql,new TableCreateScriptMapper(table,integration,staging,location));
    }
    public List<Column> getColumnInfo(String table) {
        String sql = "select * from " + table+" where 1=0" ;
        return jdbcTemplate.query(sql, new LineInfoMapper(table));
    }

    private class TableColumnMapper implements ResultSetExtractor<String> {

        @Override
        public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            StringBuffer buffer = new StringBuffer();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int k=1; k<=metaData.getColumnCount(); k++){
                buffer.append(metaData.getColumnName(k));
                if (k<metaData.getColumnCount()){
                    buffer.append(",");
                }
            }
            return buffer.toString();
        }
    }
    
    private static class LineInfoMapper implements ResultSetExtractor<List<Column>> {

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
    private static class TableNameMapper implements ResultSetExtractor<List<String>>{

        @Override
        public List<String> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<String> tables  = new ArrayList<>();
            while (resultSet.next()){
                tables.add(resultSet.getString(1));
            }
            return tables;
        }
    }
}
