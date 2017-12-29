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

package fi.tietoallas.incremental.common.commonincremental.resultsetextrator;

import static fi.tietoallas.incremental.common.commonincremental.util.CommonConversionUtils.convertTableName;
import static java.util.Collections.EMPTY_LIST;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;

/**
 * Implementation org.springframework.jdbc.core.ResultSetExtractor used for converting
 * JDBC types to Avro types using information in java.sql.ResultSet
 * @author Antti Kalliokoski
 * @author Tuukka Arola
 */
public class ColumnRowMapper implements ResultSetExtractor<List<SchemaGenerationInfo>> {

	public static final String AVRO_LOGICAL_TYPE = "logicalType";
	
	private static Logger logger = LoggerFactory.getLogger(ColumnRowMapper.class);
	private final String table;

	public ColumnRowMapper(String table) {
		this.table = table;
	}

    /**
     *
     * @param resultSet information of table only metadata is used
     * @return List<SchemaGenerationInfo> where one element contains information for one column
     * @throws SQLException
     * @throws DataAccessException
     */

	@Override
	public List<SchemaGenerationInfo> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		if (metaData == null) {
			logger.info("no metadata");
			return EMPTY_LIST;
		} else if (resultSet.getMetaData().getColumnCount() <= 1) {
			logger.info("no columns");
			return EMPTY_LIST;
		}
		List<SchemaGenerationInfo> schemaGenerationInfos = new ArrayList<>();

			List<String> columns = new ArrayList<>();
			List<String> types = new ArrayList<>();
			List<Map<String, String>> params = new ArrayList<>();
			for (int j = 1; j <= metaData.getColumnCount(); j++) {
				columns.add(convertTableName(metaData.getColumnName(j)));
				types.add(convertToAvroType(metaData.getColumnType(j)));
				params.add(createAvroSchemaParams(metaData, j));
			}
			schemaGenerationInfos.add(new SchemaGenerationInfo(table, columns, types, params));
		return schemaGenerationInfos;
	}

    /**
     * Method witch convers java.sql.JDBCType to Avro type in string format unless it's logicalType then byte is returned
     * @param columnType value of JDBCType as int
     * @return avro type as string
     */
	static String convertToAvroType(int columnType) {
		JDBCType jdbcType = JDBCType.valueOf(columnType);
		if (jdbcType == JDBCType.BOOLEAN || jdbcType == JDBCType.BIT) {
			return "boolean";
		} else if (jdbcType == JDBCType.SMALLINT || jdbcType == JDBCType.TINYINT || jdbcType == JDBCType.INTEGER) {
			return "int";
		} else if (jdbcType == JDBCType.VARCHAR || jdbcType == JDBCType.LONGNVARCHAR || jdbcType == JDBCType.CHAR || jdbcType == JDBCType.NCHAR
				|| jdbcType == JDBCType.NVARCHAR) {
			return "string";
		} else if (jdbcType == JDBCType.DOUBLE || jdbcType == JDBCType.FLOAT) {
			return "double";
		} else if (jdbcType == JDBCType.BIGINT) {
			return "long";
		} else if (jdbcType == JDBCType.REAL) {
			return "float";
		} else if (jdbcType == JDBCType.NUMERIC || jdbcType == JDBCType.DECIMAL) {
			return "bytes";
		} else if (jdbcType == JDBCType.TIMESTAMP || jdbcType == JDBCType.TIMESTAMP_WITH_TIMEZONE) {
			return "long";
		} else if (jdbcType == JDBCType.DATE) {
			return "int";
		}
		return "string";
	}

    /**
     *
     * @param metaData java.sql.ResultSetMetaData for full table
     * @param index Column which is converted
     * @return map that have empty values unless there is there is logicalType avro type
     * @throws SQLException
     */

	static Map<String, String> createAvroSchemaParams(ResultSetMetaData metaData, int index) throws SQLException {
		JDBCType jdbcType = JDBCType.valueOf(metaData.getColumnType(index));
		Map<String, String> params = new HashMap<>();
		if (jdbcType == JDBCType.TIMESTAMP || jdbcType == JDBCType.TIMESTAMP_WITH_TIMEZONE) {
			params.put(AVRO_LOGICAL_TYPE, "timestamp-millis");
		} else if (jdbcType == JDBCType.DATE) {
			params.put(AVRO_LOGICAL_TYPE, "date");
		} else if (jdbcType == JDBCType.NUMERIC || jdbcType == JDBCType.DECIMAL) {
			params.put(AVRO_LOGICAL_TYPE, "decimal");
			int precision = metaData.getPrecision(index);
			int scale = metaData.getScale(index);
			// Atleast Oracle JDBC driver can return invalid values if not defined in create statement
			if (precision < 0) {
				precision = 5;
			}
			if (scale < 0) {
				scale = 0;
			}
			params.put("precision", String.valueOf(precision));
			params.put("scale", String.valueOf(scale));
		} 
		
		return params;
	}

}
