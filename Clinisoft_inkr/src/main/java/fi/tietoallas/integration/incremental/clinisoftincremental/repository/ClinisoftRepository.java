package fi.tietoallas.integration.incremental.clinisoftincremental.repository;

/*-
 * #%L
 * clinisoft-incremental
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
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.ColumnRowMapper;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.GenericRecordMapper;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clinisoft specific repository methods
 * @author xxkallia
 */

@Repository
public class ClinisoftRepository {

    private JdbcTemplate jdbcTemplate;

    public ClinisoftRepository(@Autowired JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    /**
     *
     * @param table table where columns are
     * @param columns Comma separated list of column names
     * @return Schema information of each column
     */

    public List<SchemaGenerationInfo> getColumnInfo(final String table, final String columns) {
        String sql = "select top 1 "+columns+" from " + table;
        return jdbcTemplate.query(sql, new ColumnRowMapper(table));
    }

    /**
     *
     * @param sql SQL statement for fetching new data
     * @param table name of table
     * @return list of Generic records with new data or empty list if there is none
     */
    public List<GenericRecord> getNewData(final String sql, final String table) {
        return jdbcTemplate.query(sql, new GenericRecordMapper(table));
    }

    /**
     *
     * @param tableStatusInformation information about table
     * @return list of patient ids that have been released after last integration
     */

    public List<Long> findPatientIds(final TableStatusInformation tableStatusInformation) {
        String sql = "SELECT patientid FROM p_generaldata WHERE status = 8 AND patientId IN (SELECT patientId FROM P_DischargeData WHERE DischargeTime > ?)";
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

    /**
     *
     * @param database name of selected database
     */

    public void changeDatabase(String database) {
        jdbcTemplate.execute("use "+database);
    }
}
