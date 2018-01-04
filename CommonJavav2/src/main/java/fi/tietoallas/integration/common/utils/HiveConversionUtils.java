package fi.tietoallas.integration.common.utils;

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
import java.sql.JDBCType;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.*;

public class HiveConversionUtils {
    public static String generateCreate(ResultSetMetaData resultSetMetaData, String table, String integration, boolean staging,String  storageLocation) throws SQLException {
        StringBuffer sql = new StringBuffer();
        if (staging) {
            sql.append("create table staging_");
        } else {
            sql.append("create table varasto_");
        }
        sql.append(integration);
        if (!staging) {
            sql.append("_historia_log");
        }
        sql.append(".");
        sql.append(convertTableNameAndConvertComma(table));
        sql.append(" (");
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            sql.append("`");
            sql.append(convertTableName(resultSetMetaData.getColumnName(i)));
            sql.append("`");
            sql.append(" ");
            sql.append(toHiveType(resultSetMetaData.getColumnType(i), resultSetMetaData.getPrecision(i), resultSetMetaData.getScale(i)));
            sql.append(",");
        }
        sql.append("allas__pvm_partitio STRING");
        sql.append(") stored as orc LOCATION '");
        if (staging) {
            sql.append("adl:///staging/");
        } else {
                sql.append(storageLocation);
        }
        sql.append(integration);
        sql.append("/");
        sql.append(convertTableNameAndConvertComma(table));
        sql.append("'");

        return sql.toString();
    }

    public static String toHiveType(int columnType, int precision, int scale) {
        JDBCType jdbcType = JDBCType.valueOf(columnType);
        if (jdbcType == JDBCType.BOOLEAN) {
            return "BOOLEAN";
        } else if (jdbcType == JDBCType.BIT) {
            return "BOOLEAN";
        } else if (jdbcType == JDBCType.BINARY || jdbcType == JDBCType.BLOB || jdbcType == JDBCType.CLOB ||
                jdbcType == JDBCType.LONGVARBINARY || jdbcType == JDBCType.VARBINARY || jdbcType == JDBCType.NCLOB) {
            return "BINARY";
        } else if (jdbcType == JDBCType.TINYINT) {
            return "TINYINT";
        } else if (jdbcType == JDBCType.SMALLINT) {
            return "SMALLINT";
        } else if (jdbcType == JDBCType.INTEGER) {
            return "int";
        } else if (jdbcType == JDBCType.VARCHAR || jdbcType == JDBCType.LONGNVARCHAR ||
                jdbcType == JDBCType.CHAR || jdbcType == JDBCType.NCHAR ||
                jdbcType == JDBCType.NVARCHAR) {
            return "STRING";
        } else if (jdbcType == JDBCType.DOUBLE) {
            return "DOUBLE";
        } else if ( jdbcType == JDBCType.FLOAT){
            return "FLOAT";
        }
        else if (jdbcType == JDBCType.BIGINT) {
            return "BIGINT";
        } else if (jdbcType == JDBCType.NUMERIC || jdbcType == JDBCType.DECIMAL) {
            //Atleast Oracle JDBC driver can return invalid values if not defined in create statement
            if (precision <= 0) {
                precision = 5;
            }
            if (scale < 0) {
                scale = 0;
            }
            if (scale>precision){
                scale = 3;
                precision = 15;
            }
            if (scale > 38 ){
                scale = 38;
            }
            if (precision > 38){
                precision = 38;
            }
            return "DECIMAL (" + precision + "," + scale + ")";
        } else if (jdbcType == JDBCType.REAL) {
            return "STRING";
        } else if (jdbcType == JDBCType.DATE || jdbcType == JDBCType.TIMESTAMP || jdbcType == JDBCType.TIME || jdbcType == JDBCType.TIME_WITH_TIMEZONE || jdbcType == JDBCType.TIMESTAMP_WITH_TIMEZONE) {
            return "TIMESTAMP";
        }
        return "STRING";
    }
}
