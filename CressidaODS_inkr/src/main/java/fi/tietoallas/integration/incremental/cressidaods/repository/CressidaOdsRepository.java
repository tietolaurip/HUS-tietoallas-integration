package fi.tietoallas.integration.incremental.cressidaods.repository;

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

import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.ColumnRowMapper;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.GenericRecordMapper;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * Repository class from CressidaODS specific data
 * @author Antti Kalliokoski
 */

@Repository
public class CressidaOdsRepository {

    private JdbcTemplate jdbcTemplate;

    public CressidaOdsRepository(@Autowired JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    /**
     * 
     * @param sql SQL statement
     * @param table table 
     * @param lastRunAt time which is used in comparison 
     * @return List of new Data or empty list
     */

    public List<GenericRecord> getNewData(final String sql, final String table, final Timestamp lastRunAt){
        return jdbcTemplate.query(sql,new Object[]{lastRunAt}, new GenericRecordMapper(table));
    }

    /**
     * 
     * @param tableName table name
     * @param columns comma separated list of tables columns
     * @return schema information of listed columns
     */
    public List<SchemaGenerationInfo> getColumnInfo(String tableName, String columns) {
        String sql = "select "+columns +" from "+tableName+" where 1=0";
       return jdbcTemplate.query(sql, new ColumnRowMapper(tableName));
    }
}
