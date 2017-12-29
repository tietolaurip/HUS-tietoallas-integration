package fi.tietoallas.integration.caresuiteincremental.repository;

/*-
 * #%L
 * caresuite-incremental
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
import fi.tietoallas.integration.caresuiteincremental.domain.TableIdColumnInformation;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Repository class for Caresuite repository
 * Methods are used to fetch new data from caresuite, because
 * integration is read only there is no methods for update or delete
 *
 * @author xxkallia
 */

@Repository
public class CareSuiteRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Logger logger = LoggerFactory.getLogger(CareSuiteRepository.class);

    private static final String SELECT_MODIFIED_IDS_FROM_XON_INT = "SELECT SOURCETABLE,SOURCETABLE_PK_DBOID,SOURCETABLE_PK_COLUMNNAME,CREATED  FROM XON_INT_DATALAKE WHERE SOURCETABLE =? AND  CREATED >=?";

    public CareSuiteRepository(@Autowired JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    /**
     *
     * @param table name of table
     * @param columns comma seperated list of columns
     * @return List of schemaGenerationInfo which is used to generate Acro schmema
     */

    public List<SchemaGenerationInfo> getColumnNames(final String table,final String columns) {
        String sql = "select top 1 "+columns+" from " + table;
        return jdbcTemplate.query(sql, new ColumnRowMapper(table));
    }

    /**
     *
     * @param sql SQL statement to fetch new rows
     * @param table table where data is fetched
     * @return List of Avro Generic records ready to send to kafka
     */
    public List<GenericRecord> getNewData(final String sql, final String table) {
        logger.info("sql is " + sql);
        return jdbcTemplate.query(sql, new GenericRecordMapper(table));
    }

    /**
     * Information about updated rows  using XON_INT table and new rows based on lastusedvalue
     * @param table name of table
     * @param lastUsedValue Biggest value which is send to kafka. Bigger are included in resultset
     * @return Base information of new and updated rows
     */
    public List<TableIdColumnInformation> getUpdatedBDOID(final String table, final Timestamp lastUsedValue) {
        return jdbcTemplate.query(SELECT_MODIFIED_IDS_FROM_XON_INT, new Object[]{table, lastUsedValue}, new OnIntDatalakeMapper());
    }

    protected static final class OnIntDatalakeMapper implements RowMapper<TableIdColumnInformation> {

        @Override
        public TableIdColumnInformation mapRow(ResultSet resultSet, int i) throws SQLException {
            return new TableIdColumnInformation(resultSet.getString("SOURCETABLE"), resultSet.getBigDecimal("SOURCETABLE_PK_DBOID"), resultSet.getString("SOURCETABLE_PK_COLUMNNAME"), resultSet.getTimestamp("CREATED"));
        }
    }
}
