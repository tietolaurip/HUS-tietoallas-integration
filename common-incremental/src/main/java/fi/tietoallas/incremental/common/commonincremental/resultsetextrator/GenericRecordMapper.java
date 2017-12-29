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


import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.util.CommonConversionUtils;
import org.apache.avro.Conversions.DecimalConversion;
import org.apache.avro.LogicalTypes;
import org.apache.avro.LogicalTypes.Decimal;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools.toAvroSchema;

/**
 * Implementation of ResultSetExtractor which converts java.sql.RecordSet to List of Avro GenericRecord
 *
 * @author Antti Kalliokoski
 * @author Tuukka Arola
 */
public class GenericRecordMapper implements ResultSetExtractor<List<GenericRecord>> {

	public static final long MILLIS_IN_DAY = 1000L * 60 * 60 * 24;

	private final String table;

	public GenericRecordMapper(String table) {
		this.table = table;
	}

	@Override
	public List<GenericRecord> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
		int columnCount = resultSet.getMetaData().getColumnCount();
		ResultSetMetaData metaData = resultSet.getMetaData();
		List<GenericRecord> records = new ArrayList<>();
		List<String> columnNames = null;
		Schema schema = null;
		while (resultSet.next()) {
			// The schema is available at this location and is extracted only once for the whole dataset.
			if (columnNames == null) {
				columnNames = new ArrayList<>();
				List<String> columnTypes = new ArrayList<>();
				List<Map<String, String>> params = new ArrayList<>();
				for (int u = 1; u <= columnCount; u++) {
					columnNames.add(CommonConversionUtils.convertTableName(metaData.getColumnName(u)));
					columnTypes.add(ColumnRowMapper.convertToAvroType(metaData.getColumnType(u)));
					params.add(ColumnRowMapper.createAvroSchemaParams(metaData, u));
				}
				schema = new Schema.Parser().parse(toAvroSchema(new SchemaGenerationInfo(table, columnNames, columnTypes, params)));
			}

			GenericRecord record = new GenericData.Record(schema);
			for (int j = 1; j <= columnCount; j++) {
				record.put(columnNames.get(j - 1), getTypedValue(resultSet, metaData, j));
			}
			records.add(record);
		}

		return records;
	}

	private Object getTypedValue(ResultSet rs, ResultSetMetaData metaData, int column) throws SQLException {
		try {
			JDBCType jdbcType = JDBCType.valueOf(metaData.getColumnType(column));
			if (jdbcType == JDBCType.BOOLEAN || jdbcType == JDBCType.BIT) {
				return rs.getBoolean(column);
			} else if (jdbcType == JDBCType.SMALLINT || jdbcType == JDBCType.TINYINT || jdbcType == JDBCType.INTEGER) {
				return rs.getInt(column);
			} else if (jdbcType == JDBCType.VARCHAR || jdbcType == JDBCType.LONGNVARCHAR || jdbcType == JDBCType.CHAR || jdbcType == JDBCType.NCHAR
					|| jdbcType == JDBCType.NVARCHAR) {
				return rs.getString(column);
			} else if (jdbcType == JDBCType.DOUBLE || jdbcType == JDBCType.FLOAT) {
				return rs.getDouble(column);
			} else if (jdbcType == JDBCType.BIGINT) {
				return rs.getLong(column);
			} else if (jdbcType == JDBCType.NUMERIC || jdbcType == JDBCType.DECIMAL) {
				int precision = metaData.getPrecision(column);
				int scale = metaData.getScale(column);
				// Atleast Oracle JDBC driver can return invalid values if not defined in create statement
				if (precision < 0) {
					precision = 5;
				}
				if (scale < 0) {
					scale = 0;
				}
				Decimal decimal = LogicalTypes.decimal(precision, scale);
				return new DecimalConversion().toBytes(rs.getBigDecimal(column), null, decimal);
			} else if (jdbcType == JDBCType.REAL) {
				return rs.getFloat(column);
			} else if (jdbcType == JDBCType.TIMESTAMP) {
				return (rs.getTimestamp(column) != null ? rs.getTimestamp(column).getTime() : null);
			} else if (jdbcType == JDBCType.DATE) {
				return (rs.getDate(column) != null ? (int) (rs.getDate(column).getTime() / MILLIS_IN_DAY) : null);
			}
		} catch (Exception e) {
			// normally not error, but if null is returned type can't be resolved
		}

		return rs.getString(column);

	}
}
