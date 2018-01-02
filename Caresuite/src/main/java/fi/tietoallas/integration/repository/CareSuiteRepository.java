package fi.tietoallas.integration.repository;

import fi.tietoallas.integration.common.mapper.ColumnNamesResultSetExtractor;
import fi.tietoallas.integration.common.mapper.TableCreateScriptMapper;
import fi.tietoallas.integration.configuration.MultibleSqlServerConfiguration;
import fi.tietoallas.integration.domain.TableIdColumnInformation;
import fi.tietoallas.integration.domain.TableIdValuePair;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;

@Repository
public class CareSuiteRepository {

    @Qualifier(MultibleSqlServerConfiguration.MSSQL_JDBC_TEMPLATE)
    private JdbcTemplate jdbcTemplate;

    public CareSuiteRepository(@Qualifier(MultibleSqlServerConfiguration.MSSQL_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TableIdValuePair getTablesWithUpdatingIdNotInDatalakeTable(final String table, final String keyField) {
        if (StringUtils.isBlank(keyField)) {
            return new TableIdValuePair(table, null);
        }
        String sql = "select max(" + keyField + ") from " + table;
        BigDecimal maxValue = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return new TableIdValuePair(table, maxValue);
    }

    public String getQueryColumns(final String table) {
        String sql = "select top 1 * from " + table ;
        return jdbcTemplate.query(sql, new ColumnNamesResultSetExtractor());
    }

    public String createStatement(String table, String integration, boolean staging, String columns,String storageLocation) {
        String sql = "select " + columns + " from " + table + " where 1=0";
        return jdbcTemplate.query(sql, new TableCreateScriptMapper(table, integration, staging,storageLocation));
    }

    public List<String> getAllTables() throws Exception {

        return jdbcTemplate.query("SELECT name FROM sys.objects WHERE type = 'U'", new TableNameRowMapper());
    }

    public List<String> getPrimaryKeys(final String tableName) throws Exception {
        return (List<String>) JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new PrimaryKeyCallBack(tableName));
    }

    public List<Column> getColumnInfos(final String tableName, final String integration, List<String> primaryKeys) throws Exception {
        String sql = "select top 1 * from " + tableName;
        return jdbcTemplate.query(sql, new RowInfoMapper(tableName, primaryKeys, integration));
    }


    private static final class OnIntDatalakeMapper implements RowMapper<TableIdColumnInformation> {

        @Override
        public TableIdColumnInformation mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TableIdColumnInformation(resultSet.getString("SOURCETABLE"), resultSet.getBigDecimal("SOURCETABLE_PK_DBOID"), resultSet.getString("SOURCETABLE_PK_COLUMNNAME"), resultSet.getTimestamp("CREATED"));
        }
    }

    private class TableNameRowMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(1);
        }
    }

    private static class RowInfoMapper implements ResultSetExtractor<List<Column>> {

        private String tableName;
        private String integration;
        private List<String> primaryKeys;

        public RowInfoMapper(String tableName, List<String> primaryKeys, String integration) {
            this.tableName = tableName;
            this.integration = integration;
            this.primaryKeys = primaryKeys;
        }

        @Override
        public List<Column> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<Column> columns = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                Column column = new Column(this.integration, convertTableName(metaData.getColumnName(i)), metaData.getColumnName(i));
                column.setPrimaryKey(primaryKeys == null ? Boolean.FALSE : primaryKeys.indexOf(metaData.getColumnName(i)) > -1);
                column.setName(convertTableName(metaData.getColumnName(i)));
                column.setOrigType(metaData.getColumnTypeName(i) + "(" + metaData.getPrecision(i) + ")");
                column.setOrigColumnName(metaData.getColumnName(i));
                column.setDataSetName(this.integration);
                column.setTableName(convertTableName(this.tableName));
                columns.add(column);
            }
            return columns;
        }
    }

    private class PrimaryKeyCallBack implements DatabaseMetaDataCallback {
        private String tableName;

        public PrimaryKeyCallBack(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
            ResultSet resultSet = dbmd.getPrimaryKeys(null, null, tableName);
            List<String> primaryKeys = new ArrayList<>();
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));

            }
            return primaryKeys;

        }
    }
}
