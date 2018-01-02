package fi.tietoallas.integration.clinisoft.repository;

import fi.tietoallas.integration.clinisoft.ClinisoftIntegrationApplication;
import fi.tietoallas.integration.clinisoft.configuration.SourceDbConfiguration;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.domain.SchemaGenerationInfo;
import fi.tietoallas.integration.common.domain.TableStatusInformation;
import fi.tietoallas.integration.common.mapper.ColumnNamesResultSetExtractor;
import fi.tietoallas.integration.common.mapper.ColumnRowMapper;
import fi.tietoallas.integration.common.mapper.GenericRecordMapper;
import fi.tietoallas.integration.common.mapper.TableCreateScriptMapper;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;

@Repository
public class ClinisoftRepository {

    private static Logger logger = LoggerFactory.getLogger(ClinisoftRepository.class);


    @Qualifier(SourceDbConfiguration.SOURCE_JDBC_TEMPLATE)
    private JdbcTemplate jdbcTemplate;

    public ClinisoftRepository(@Qualifier(SourceDbConfiguration.SOURCE_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getAllTables() {
        if (jdbcTemplate == null) {
            logger.error("jdbctemplate null");
        }
        return jdbcTemplate.query("SELECT name FROM sysobjects WHERE type='U'", new TableNameRowMapper());
    }

    public void changeDatabase(final String database) {
        jdbcTemplate.execute("use " + database);
    }

    public String createStatement(String table, String integration, boolean staging, String columns) {
        String sql = "select top 1 " + columns + " from " + table;
        return jdbcTemplate.query(sql, new TableCreateScriptMapper(table, integration, staging));
    }

    public ParseTableInfo getPatientTableInfo(String table, String integration) {
        String sql = "SELECT top 1 * FROM " + table;
        return new ParseTableInfo.Builder()
                .withTableName(table)
                .withColumnQuery(jdbcTemplate.query(sql, new ColumnNamesResultSetExtractor()))
                .withIntegrationName(integration)
                .withOriginalDatabase("Patient")
                .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
                .withLines(new ArrayList<>())
                .build();
    }

    public ParseTableInfo getSystemTableInfo(String table, String integration) {
        String sql = "SELECT top 1  * FROM " + table;
        return new ParseTableInfo.Builder()
                .withTableName(table)
                .withColumnQuery(jdbcTemplate.query(sql, new ColumnNamesResultSetExtractor()))
                .withIntegrationName(integration)
                .withOriginalDatabase("System")
                .withTableType(ParseTableInfo.TableType.FULL_TABLE)
                .withLines(new ArrayList<>())
                .build();

    }

    public ParseTableInfo getDepartmentTableInfo(String table, String integration) {
        String sql = "SELECT top 1 * FROM " + table ;
        return new ParseTableInfo.Builder()
                .withTableName(table)
                .withColumnQuery(jdbcTemplate.query(sql, new ColumnNamesResultSetExtractor()))
                .withOriginalDatabase("Department")
                .withIntegrationName(integration)
                .withTableType(ParseTableInfo.TableType.FULL_TABLE)
                .withLines(new ArrayList<>())
                .build();
    }

    public List<Column> getColumnInfo(final String tableName, final String primaryKeyColumn, final String integration) {
        String sql = "select top 1  * from " + tableName;
        return jdbcTemplate.query(sql, new RowInfoMapper(tableName, primaryKeyColumn, integration));
    }

    public List<GenericRecord> getNewData(final String sql, final String table) {
        logger.info("sql is " + sql);
        return jdbcTemplate.query(sql, new GenericRecordMapper(table));
    }

    public List<Long> findPatientIds(final TableStatusInformation tableStatusInformation) {
        String sql = "SELECT patientid FROM p_generaldata WHERE status = 8 AND patientId IN (SELECT patientId FROM P_DischargeData WHERE DischargeTime >= ?)";
        return jdbcTemplate.query(sql, new Object[]{tableStatusInformation.lastAccessAt}, new ResultSetExtractor<List<Long>>() {
            @Override
            public List<Long> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                List<Long> ids = new ArrayList<>();
                while (resultSet.next()) {
                    ids.add(resultSet.getLong(1));
                }
                return ids;
            }
        });
    }

    public List<SchemaGenerationInfo> getColumnNames(final String table) {
        String sql = "select top 1 * from " + table;
        return jdbcTemplate.query(sql, new ColumnRowMapper(table));
    }

    public List<String> findArchiveIds(Timestamp lastAccessAt) {
        String sql = "select archiveid from ... ";
        return null;
    }


    private static class RowInfoMapper implements ResultSetExtractor<List<Column>> {

        private String tableName;
        private String keyColumn;
        private String integration;

        public RowInfoMapper(String tableName, String keyColumn, String integration) {
            this.tableName = tableName;
            this.keyColumn = keyColumn;
            this.integration = integration;
        }

        @Override
        public List<Column> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<Column> columns = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                Column column = new Column(this.integration, convertTableName(metaData.getColumnName(i)), metaData.getColumnName(i));
                column.setPrimaryKey(metaData.getColumnName(i).equalsIgnoreCase(keyColumn));
                column.setTableName(convertTableName(tableName));
                column.setOrigType(metaData.getColumnTypeName(i) + "(" + metaData.getPrecision(i) + ")");
                column.setOrigColumnName(metaData.getColumnName(i));
                column.setName(convertTableName(metaData.getColumnName(i)));
                columns.add(column);
            }
            return columns;
        }
    }


    private static class TableNameRowMapper implements RowMapper<String> {

        @Override
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString("name");
        }
    }

}
