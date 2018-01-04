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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@RunWith(JUnit4.class)
public class LineInfoTest {

    private static Boolean dataChangeColumn = Boolean.TRUE;
    private static Boolean isPrimary = Boolean.FALSE;

    @Test
    public void testBuilder(){
        LineInfo lineInfo = new LineInfo.Builder()
                .withDataChangeColumn(dataChangeColumn)
                .withIsPrimary(isPrimary)
                .withDataType("dataType")
                .withDescription("description")
                .withRowName("rowName")
                .build();
        assertThat(lineInfo.isPrimary,is(isPrimary));
        assertThat(lineInfo.isDataChangeColumn,is(dataChangeColumn));
        assertThat(lineInfo.rowName,is("rowName"));
        assertThat(lineInfo.dataType,is("dataType"));
        assertThat(lineInfo.description,is("description"));
    }

}
