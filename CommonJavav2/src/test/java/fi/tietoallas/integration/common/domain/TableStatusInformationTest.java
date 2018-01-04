package fi.tietoallas.integration.common.domain;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@RunWith(JUnit4.class)
public class TableStatusInformationTest {

    private static final Timestamp lastRunAt = Timestamp.valueOf(LocalDateTime.now());
    private static final BigDecimal lastUsedValue = new BigDecimal(10);

    @Test
    public void testBuilder(){
        TableStatusInformation tableStatusInformation = new TableStatusInformation.Builder()
                .withTableName("tableName")
                .withColumnQuery("columnQuery")
                .withLassAccessAt(lastRunAt)
                .withKeyColumn("keyColumn")
                .withLastUsedValue(lastUsedValue)
                .withOriginalDatabase("originalDatabase")
                .withParameterType("parameterType")
                .withQuery("query")
                .withSearchColumn("searchColumn")
                .withTimeColumn("timeColumn")
                .build();
        assertThat(tableStatusInformation.tableName,is("tableName"));
        assertThat(tableStatusInformation.columnQuery,is("columnQuery"));
        assertThat(tableStatusInformation.lastAccessAt,is(lastRunAt));
        assertThat(tableStatusInformation.keyColumn,is("keyColumn"));
        assertThat(tableStatusInformation.lastUsedValue,is(lastUsedValue));
        assertThat(tableStatusInformation.originalDatabase,is("originalDatabase"));
        assertThat(tableStatusInformation.parameterType,is("parameterType"));
        assertThat(tableStatusInformation.query,is("query"));
        assertThat(tableStatusInformation.searchColumn,is("searchColumn"));
        assertThat(tableStatusInformation.timeColumn,is("timeColumn"));
    }
}
