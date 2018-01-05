package fi.tietoallas.integration.qpati.qpati.repository;

/*-
 * #%L
 * qpati
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
import fi.tietoallas.integration.qpati.qpati.domain.HiveTypeAndDataColumnName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MetadataRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<HiveTypeAndDataColumnName> getHiveTypes(final String integration, final String table) {
		return jdbcTemplate.query(
				"select hive_type,data_column_name from data_column where data_set_name=? and data_table_name=?",
				new Object[] { integration, table }, new HiveTypeMapper());
	}

	private static class HiveTypeMapper implements RowMapper<HiveTypeAndDataColumnName> {
		
		@Override
		public HiveTypeAndDataColumnName mapRow(ResultSet resultSet, int i) throws SQLException {
			return new HiveTypeAndDataColumnName(convertToAvroType(resultSet.getString("hive_type")),
					resultSet.getString("data_column_name"));
		}
	}

	private static String convertToAvroType(final String hiveType) {
		if (hiveType.trim().equalsIgnoreCase("int")) {
			return "int";
		} else {
			return "string";
		}
	}
}
