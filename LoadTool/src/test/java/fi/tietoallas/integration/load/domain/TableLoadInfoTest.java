package fi.tietoallas.integration.load.domain;

/*-
 * #%L
 * copytool
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class TableLoadInfoTest {

    @Test
    public void builderTest(){

        TableLoadInfo tableLoadInfo = new TableLoadInfo.Builder()
                .withColumnNames(Arrays.asList("column"))
                .withCustomSql("customSql")
                .withLowerBound("lowerBound")
                .withNumPartitions("numberOfPartitions")
                .withOriginalDatabase("originalDatabase")
                .withPartitionColumn("partitionColumn")
                .withTableName("tableName")
                .withUpperBound("upperBound")
                .build();

        assertThat(tableLoadInfo.tableName,is("tableName"));
        assertThat(tableLoadInfo.columnNames.get(0),is("column"));
        assertThat(tableLoadInfo.lowerBound,is("lowerBound"));
        assertThat(tableLoadInfo.numPartitions,is("numberOfPartitions"));
        assertThat(tableLoadInfo.originalDatabase,is("originalDatabase"));
        assertThat(tableLoadInfo.upperBound,is("upperBound"));
        assertThat(tableLoadInfo.customSql,is("customSql"));
    }
}
