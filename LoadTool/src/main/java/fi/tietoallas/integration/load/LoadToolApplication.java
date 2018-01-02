package fi.tietoallas.integration.load;

/*-
 * #%L
 * copytool
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
import com.zaxxer.hikari.HikariDataSource;
import fi.tietoallas.integration.load.domain.TableLoadInfo;
import fi.tietoallas.integration.load.service.CopyToHDFS;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fi.tietoallas.integration.load.service.CopyToHDFS.convertTableName;
import static org.apache.commons.lang3.StringUtils.containsNone;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Class where parameters are read from startup arguments and status database
 *
 * @author xxkallia
 */


public class LoadToolApplication {

    public static void main(String[] args) {
        if (args != null && args.length > 10) {
            String integration = convertTableName(trim(args[0]));
            String dbName = trim(args[1]);
            String sourceDbUser = trim(args[2]);
            String sourceDbPassword = trim(args[3]);
            final String sourceDbUrl = trim(args[4]);
            String sourceDbDriver = trim(args[5]);
            String statusDbUser = trim(args[6]);
            String statusDbPwd = trim(args[7]);
            String statusDbUrl = trim(args[8]);
            String path = args.length==10 ? trim(args[9]):null;
            System.out.println(LocalDateTime.now().toString() + " [INFO ] BEGIN: Load data: " + integration);
            if (StringUtils.isNotBlank(dbName) && StringUtils.contains(dbName, ",")) {
                String[] dbNames = StringUtils.split(dbName, ",");
                for (int i = 0; i < dbNames.length; i++) {
                    String realDbName = dbNames[i];
                    String modifiedJdbcUrl = addDatabaseToJDBCURL(sourceDbUrl, realDbName);
                    CopyToHDFS copyToHDFS = new CopyToHDFS();
                    List<TableLoadInfo> tables = getTables(integration, statusDbUser, statusDbUrl, statusDbPwd, realDbName);
                    for (TableLoadInfo table : tables) {
                        System.out.println(LocalDateTime.now().toString() + " [INFO ]    Starting to process(" + integration + "): " + realDbName + "." + table.tableName);

                        copyToHDFS.copyTable(table,  integration, sourceDbDriver, modifiedJdbcUrl, sourceDbUser, sourceDbPassword,
                                getLocalDateTimeInUTC().toString(), table.columnNames,path);
                        System.out.println(LocalDateTime.now().toString() + " [INFO ]    Completed processing(" + integration + "): " + realDbName + "." + table.tableName);
                    }

                }

            } else {

                System.out.println(LocalDateTime.now().toString() + " [INFO ]    Starting to load data for default db(" + integration + ") ");
                CopyToHDFS copyToHDFS = new CopyToHDFS();
                List<TableLoadInfo> tables = getTables(integration, statusDbUser, statusDbUrl, statusDbPwd, null);
                for (TableLoadInfo table : tables) {
                    System.out.println(LocalDateTime.now().toString() + " [INFO ]    Starting to process(" + integration + "): <defaultDb>." + table.tableName);


                    copyToHDFS.copyTable(table,  integration, sourceDbDriver, sourceDbUrl, sourceDbUser, sourceDbPassword,  getLocalDateTimeInUTC().toString(),table.columnNames,path);
                    System.out.println(LocalDateTime.now().toString() + " [INFO ]    Completed processing(" + integration + "): <defaultDb>." + table.tableName);
                }
            } // If DB not defined
            System.out.println(LocalDateTime.now().toString() + " [INFO ] END: Load data: " + integration);
        } // If args ok
    } // Main

    private static void updateTimeStamp(final String table, final String integration, final String password, final String url, final String username) {
        String sql = "UPDATE integration_status SET last_run_at=? WHERE integration_name=? AND table_name=?";
        HikariDataSource basicDataSource = new HikariDataSource();
        basicDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        basicDataSource.setConnectionTimeout(200000);
        basicDataSource.setPassword(password);
        basicDataSource.setJdbcUrl(url);
        basicDataSource.setUsername(username);
        try {
            Connection connection = basicDataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setTimestamp(1, Timestamp.valueOf(getLocalDateTimeInUTC()));
            statement.setString(2, integration);
            statement.setString(3, table);
            statement.execute();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.print("Error in timestamp update " + e.getMessage());
        }

    }


    private static List<TableLoadInfo> getTables(final String integration, final String username, final String url, final String password, final String originalDatabase) {
        try {
            List<TableLoadInfo> tables = new ArrayList<>();
            HikariDataSource basicDataSource = new HikariDataSource();
            basicDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            basicDataSource.setPassword(password);
            basicDataSource.setJdbcUrl(url);
            basicDataSource.setUsername(username);
            Connection connection = basicDataSource.getConnection();
            PreparedStatement statement = null;
            if (originalDatabase == null) {
                String sql = "SELECT table_name,lowerBound,upperBound,partitionColumn,custom_sql,original_database,numPartitions, column_query FROM integration_status WHERE integration_name=? AND parameter_type <> 'EXCLUDE'";
                statement = connection.prepareStatement(sql);
                statement.setString(1, integration);
            } else {
                String sql = "SELECT table_name,lowerBound,upperBound,partitionColumn,custom_sql,original_database,numPartitions,column_query FROM integration_status WHERE integration_name=? AND original_database = ? AND parameter_type <> 'EXCLUDE'";
                statement = connection.prepareStatement(sql);
                statement.setString(1, integration);
                statement.setString(2, originalDatabase);
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tables.add(new TableLoadInfo.Builder()
                        .withTableName(resultSet.getString("table_name"))
                        .withLowerBound(resultSet.getString("lowerBound"))
                        .withUpperBound(resultSet.getString("upperBound"))
                        .withPartitionColumn(resultSet.getString("partitionColumn"))
                        .withCustomSql(resultSet.getString("custom_sql"))
                        .withOriginalDatabase(resultSet.getString("original_database"))
                        .withNumPartitions(resultSet.getString("numPartitions"))
                        .withColumnNames(Arrays.asList(StringUtils.split(resultSet.getString("column_query"),",")))
                        .build());
            }
            resultSet.close();
            statement.close();
            connection.close();
            return tables;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String addDatabaseToJDBCURL(final String jdbcUrl, final String database) {
        if (jdbcUrl.contains("sybase")) {
            String[] jdbcParts = StringUtils.split(jdbcUrl, "?");
            if (jdbcParts.length > 1) {
                return jdbcParts[0] + "/" + database + "?" + jdbcParts[1];
            } else if (containsNone("?")) {
                return jdbcUrl + "/" + database;
            } else {
                return jdbcParts[0] + "/" + database;
            }
        }

        //support for only for SAP/sybase ASE
        return null;
    }

    public static LocalDateTime getLocalDateTimeInUTC() {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);

        return nowUTC.toLocalDateTime();
    }


}
