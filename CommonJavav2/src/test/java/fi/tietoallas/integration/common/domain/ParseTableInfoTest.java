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

import java.util.ArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@RunWith(JUnit4.class)
public class ParseTableInfoTest {

    @Test
    public void testBuilder(){
        ParseTableInfo parseTableInfo = new ParseTableInfo.Builder()
                .withDescription("description")
                .withIntegrationName("integrationName")
                .withLines(new ArrayList<>())
                .withTableName("tableName")
                .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
                .withTimeColumn("timeColumn")
                .withColumnQuery("columnQuery")
                .withOriginalDatabase("originalDatabase")
                .build();

        assertThat(parseTableInfo.integrationName,is("integrationName"));
        assertThat(parseTableInfo.tableName,is("tableName"));
        assertThat(parseTableInfo.columnQuery,is("columnQuery"));
        assertThat(parseTableInfo.tableType,is(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP));
        assertThat(parseTableInfo.timeColunm,is("timeColumn"));
        assertThat(parseTableInfo.lines,hasSize(0));
        assertThat(parseTableInfo.description,is("description"));

    }
}
