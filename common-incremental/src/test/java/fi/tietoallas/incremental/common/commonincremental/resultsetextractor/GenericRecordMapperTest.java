package fi.tietoallas.incremental.common.commonincremental.resultsetextractor;

/*-
 * #%L
 * common-incremental
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
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.GenericRecordMapper;
import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GenericRecordMapperTest {

    private static final String TEST_STRING_COLUMN="string_column";
    private static final String TEST_INT_COLUMN="int_column";

    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSetMetaData metaData;

    @Before
    public void beforeTest() throws Exception {
        when (metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn(TEST_STRING_COLUMN);
        when(metaData.getColumnName(2)).thenReturn(TEST_INT_COLUMN);
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(2)).thenReturn(Types.INTEGER);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString(1)).thenReturn("test_string");
        when(resultSet.getString(TEST_STRING_COLUMN)).thenReturn("test_string");
        when(resultSet.getInt(2)).thenReturn(2);
    }
    @Test
    public void testSelect() throws Exception{
        GenericRecordMapper mapper = new GenericRecordMapper("test");
        List<GenericRecord> records = mapper.extractData(resultSet);
        assertThat(records,hasSize(1));
        assertThat(records.get(0).get(TEST_STRING_COLUMN),is("test_string"));
        assertThat(records.get(0).get(TEST_INT_COLUMN),is(2));

    }
}
