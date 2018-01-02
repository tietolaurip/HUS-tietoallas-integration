/*-
 * #%L
 * ca-incremental-import
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

package fi.tietoallas.integration.ca.caincrementalimport.repository;

import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.ColumnRowMapper;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.GenericRecordMapper;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * Repository class for CA specific querys
 *
 * @author xxkallia
 */
@Repository
public class CaRepository {

    private JdbcTemplate jdbcTemplate;

    public CaRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     *
     * @param tableName name of table which SchemaGenerationInfo is generated
     * @param columns comma separated string for all columns in table or view
     * @return info about all columns
     */
    public List<SchemaGenerationInfo> getColumnInfo(final String tableName, final String columns) {
        String sql = "select top 1 " + columns + " from " + tableName;
        return jdbcTemplate.query(sql, new ColumnRowMapper(tableName));
    }

    /**
     *
     * @param sql SQL statement which is used to fetch new data
     * @param tableName name of table used in query
     * @return list of GenericRecord with new data or empty list if there is no new data
     */
    public List<GenericRecord> getNewData(String sql, String tableName) {
        return jdbcTemplate.query(sql, new GenericRecordMapper(tableName));
    }

}
