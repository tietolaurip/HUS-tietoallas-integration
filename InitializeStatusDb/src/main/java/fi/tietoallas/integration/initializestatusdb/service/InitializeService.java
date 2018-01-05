package fi.tietoallas.integration.initializestatusdb.service;

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
import fi.tietoallas.integration.initializestatusdb.config.DbConfig;
import fi.tietoallas.integration.initializestatusdb.domain.TableNameInfo;
import fi.tietoallas.integration.initializestatusdb.domain.TableStatusInfo;
import fi.tietoallas.integration.initializestatusdb.repository.StatusDbRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

@Service
public class InitializeService {

    @Qualifier(DbConfig.HIVE_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate hiveTemplate;
    @Qualifier(DbConfig.STATUS_DB_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate metadataJdbcTemplate;

    @Autowired
    private StatusDbRepository statusDbRepository;

    private Logger logger = LoggerFactory.getLogger(InitializeService.class);

    public void initializeData(final String integration, final String originalDatabase,final boolean generateColumnQuery) {
        logger.info("=============Starting initializeData========");
        logger.info("=============Using integration " + integration);
        logger.info("=============Using originalDatabase" + originalDatabase);
        List<TableStatusInfo> allTables = statusDbRepository.getAllTables(integration, originalDatabase);
        logger.info("=============table size " + allTables.size());
        for (TableStatusInfo table : allTables) {
            logger.info("updating integer types");
            String hiveTableName = "staging_" + integration + "." + convertTableName(table.tableName);
            updateIntegerType(hiveTableName, integration);
            logger.info("processing table " + hiveTableName);
            if (table.parameterType.equalsIgnoreCase("TIME_COMPARISATION")) {
                String hql = createTimeBasedHql(hiveTableName, convertTableName(table.timeColumn));
                Timestamp lastUpdateAt = hiveTemplate.query(hql, new TimeStampRowExactor());
                if (generateColumnQuery){
                    List<String> orderedOriginalNames = getColumQuery(integration, table, hiveTableName);
                    statusDbRepository.updateWithLastUsedAndColumnQuery(integration, originalDatabase, table.tableName, lastUpdateAt, join(orderedOriginalNames,","));

                }else {
                    statusDbRepository.updateLastRunAt(integration, originalDatabase, table.tableName, lastUpdateAt);
                }
            } else if (!table.parameterType.equalsIgnoreCase("FULL_TABLE")) {
                String hql = createValueBasedHql(hiveTableName, convertTableName(table.keyColumn));
                BigDecimal  lastUsedValue = hiveTemplate.query(hql, new ValueRowSetExtracotr());
                String timeHql = createDistinctHql(hiveTableName,"allas__pvm_partitio");
                List<String> tempLastRunAt = hiveTemplate.query(timeHql,new DistantHiveStampRowMapper());
                //This because there is bug in HIVE JDBC driver max(allas__pvmpartitio) don't return rows_
                Optional<LocalDateTime> lastRunAt = tempLastRunAt.stream()
                        .map(p -> LocalDateTime.parse(p, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))
                        .max(LocalDateTime::compareTo);
                if (generateColumnQuery){
                    List<String> columQuery = getColumQuery(integration, table, hiveTableName);
                    if (lastRunAt.isPresent()) {
                        statusDbRepository.updateLastUsedValueAndColumQuery(integration,originalDatabase,table.tableName,lastUsedValue,Timestamp.valueOf(lastRunAt.get()),join(columQuery,","));
                    } else {
                        statusDbRepository.updateLastUsedValueAndColumQuery(integration,originalDatabase,table.tableName, new BigDecimal(0),Timestamp.valueOf(lastRunAt.get()),join(columQuery,","));
                    }
                }else {
                    if (lastRunAt.isPresent()) {
                        statusDbRepository.updateLastUsedValue(integration, originalDatabase, table.tableName, lastUsedValue, Timestamp.valueOf(lastRunAt.get()));
                    } else {
                        statusDbRepository.updateLastUsedValue(integration, originalDatabase, table.tableName, new BigDecimal(0), Timestamp.valueOf(LocalDateTime.now()));
                    }
                }
            }
        }
    }

    private List<String> getColumQuery(String integration, TableStatusInfo table, String hiveTableName) {
        List<String> hiveColumns = hiveTemplate.query("select * from " + hiveTableName + " where 1=0", new ColumnQueryMapper());
        List<TableNameInfo> tableNameInfos = metadataJdbcTemplate.query("select data_column_name,orig_column_name from data_column where data_set_name=? and data_table_name=?",new Object[]{integration,convertTableName(table.tableName)},new TableNameInfoMapper());
        return hiveColumns.stream()
                .map(c -> tableNameInfos.stream()
                        .filter(i -> i.hiveName.equalsIgnoreCase(c))
                        .map(i -> i.orignalName)
                        .findFirst()
                        .get()
                )
                .collect(Collectors.toList());
    }

    private void updateIntegerType(String tableName, String integration) {

        List<String> updateSqls = hiveTemplate.query("select * from `" + tableName + "` limit 1", new RefactorRowExactor(tableName));
        if (updateSqls != null) {
            logger.info("starting hive update for " + tableName);
            for (String sql : updateSqls) {
                logger.info("Update sql is "+sql);
                hiveTemplate.execute(sql);
            }
            logger.info("hive update done for " + tableName);
        }
    }

    String createValueBasedHql(String table, String column) {
        return "select max(`" + column + "`) from `" + table+"`";
    }

    String createTimeBasedHql(final String tableName, final String timeField) {
        return "select max(`" + timeField + "`) from `" + tableName+"`";
    }
    String createDistinctHql( final String tableName, final String field){
        return "select distinct("+field+") from "+tableName;
    }


    private class TimeStampRowExactor implements ResultSetExtractor<Timestamp> {
        @Override
        public Timestamp extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return resultSet.getTimestamp(1);
            }
            return null;
        }
    }

    private class ValueRowSetExtracotr implements ResultSetExtractor<BigDecimal> {
        @Override
        public BigDecimal extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            if (resultSet.next()) {
                if (metaData.getColumnType(1)==Types.INTEGER){
                    return BigDecimal.valueOf(resultSet.getInt(1));
                }
                if (metaData.getColumnType(1)==Types.DOUBLE){
                    return BigDecimal.valueOf(resultSet.getDouble(1));
                }
                if (metaData.getColumnType(1)==Types.FLOAT){
                    return  BigDecimal.valueOf(resultSet.getFloat(1));
                }
                return resultSet.getBigDecimal(1);
            }
            return null;
        }
    }

    private class RefactorRowExactor implements ResultSetExtractor<List<String>> {

        private String table;

        public RefactorRowExactor(final String table) {
            this.table = table;
        }

        @Override
        public List<String> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> sqls = new ArrayList<>();
            boolean needsModification = false;
            for (int i = 1; i <= columnCount; i++) {
                int type = metaData.getColumnType(i);
                if (type == Types.TINYINT || type == Types.SMALLINT) {
                    String columnName = metaData.getColumnName(i);
                    logger.info("column name = "+columnName);
                    String[] parts = StringUtils.split(columnName, ".");
                    sqls.add("alter table `"+table +"` change `" + parts[2] + "` `" + parts[2] + "` int");
                    needsModification = true;
                }
            }
            if (needsModification) {
                return sqls;
            }
            return null;
        }
    }

    private static String convertTableName(final String originalTableName) {
        if (StringUtils.isNotBlank(originalTableName)) {
            return originalTableName.
                    replace(" ", "_").
                    replace("-", "_").
                    replace("Ä", "a").
                    replace("ä", "a").
                    replace("Å", "a").
                    replace("å", "a").
                    replace("Ö", "o").
                    replace("ö", "o").toLowerCase();
        }
        return null;
    }


    private class DistantHiveStampRowMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(1);
        }
    }

    private class ColumnQueryMapper implements ResultSetExtractor<List<String>> {
        @Override
        public List<String> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int i=1; i<=metaData.getColumnCount();i++){
                columns.add(metaData.getColumnName(i));
            }
            return columns;
        }
    }

    private class TableNameInfoMapper implements RowMapper<TableNameInfo> {
        @Override
        public TableNameInfo mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TableNameInfo.Builder()
                    .withHiveName(resultSet.getString("data_column_name"))
                    .withOriginalName(resultSet.getString("orig_column_name"))
                    .build();
        }
    }
}
