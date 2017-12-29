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
package fi.tietoallas.integration.caresuiteincremental.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class TableSqlQueryPairTest {

    private static final BigDecimal TEST_BOID=new BigDecimal(1000);
    private static final Timestamp TEST_TIME_STAMP=Timestamp.valueOf(LocalDateTime.now());

    @Test
    public void testBuilder(){

        TableSqlQueryPair tableSqlQueryPair = new TableSqlQueryPair.Builder()
                .withColumnName("columnName")
                .withDbIOD(TEST_BOID)
                .withLastRunAt(TEST_TIME_STAMP)
                .withSql("sql")
                .withTableName("tableName")
                .withtColumnQuery("columnQuery")
                .build();

        assertThat(tableSqlQueryPair.tableName,is("tableName"));
        assertThat(tableSqlQueryPair.columnQuery,is("columnQuery"));
        assertThat(tableSqlQueryPair.columnName,is("columnName"));
        assertThat(tableSqlQueryPair.tableName,is("tableName"));
        assertThat(tableSqlQueryPair.sql,is("sql"));
        assertThat(tableSqlQueryPair.dboid,is(TEST_BOID));
        assertThat(tableSqlQueryPair.lastUpdatedAt,is(TEST_TIME_STAMP));
    }
}
