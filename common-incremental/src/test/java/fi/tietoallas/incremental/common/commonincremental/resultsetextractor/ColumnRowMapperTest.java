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
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.ColumnRowMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static fi.tietoallas.incremental.common.commonincremental.resultsetextrator.ColumnRowMapper.AVRO_LOGICAL_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ColumnRowMapperTest {

    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSetMetaData resultSetMetadata;

    @Before
    public void beforeTest() throws Exception {
        when(resultSetMetadata.getColumnCount()).thenReturn(9);
        when(resultSetMetadata.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSetMetadata.getColumnType(2)).thenReturn(Types.DECIMAL);
        when(resultSetMetadata.getColumnType(3)).thenReturn(Types.BIT);
        when(resultSetMetadata.getColumnType(4)).thenReturn(Types.BOOLEAN);
        when(resultSetMetadata.getColumnType(5)).thenReturn(Types.BIGINT);
        when(resultSetMetadata.getColumnType(6)).thenReturn(Types.DATE);
        when(resultSetMetadata.getColumnType(7)).thenReturn(Types.TIMESTAMP);
        when(resultSetMetadata.getColumnType(8)).thenReturn(Types.DOUBLE);
        when(resultSetMetadata.getColumnType(9)).thenReturn(Types.VARCHAR);
        when(resultSetMetadata.getColumnName(1)).thenReturn("Int_column");
        when(resultSetMetadata.getColumnName(2)).thenReturn("decimaL_column");
        when(resultSetMetadata.getColumnName(3)).thenReturn("biT_column");
        when(resultSetMetadata.getColumnName(4)).thenReturn("boolean_Column");
        when(resultSetMetadata.getColumnName(5)).thenReturn("bigint_Column");
        when(resultSetMetadata.getColumnName(6)).thenReturn("date_Column");
        when(resultSetMetadata.getColumnName(7)).thenReturn("timestamp_column");
        when(resultSetMetadata.getColumnName(8)).thenReturn("double_column");
        when(resultSetMetadata.getColumnName(9)).thenReturn("varchar_column");
        when(resultSet.getMetaData()).thenReturn(resultSetMetadata);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
    }
    @Test
    public void test() throws Exception{
        ColumnRowMapper mapper = new ColumnRowMapper("test");
        List<SchemaGenerationInfo> infos = mapper.extractData(resultSet);
        assertThat(infos,hasSize(1));
        SchemaGenerationInfo schemaGenerationInfo = infos.get(0);
        List<String> names = schemaGenerationInfo.names;
        assertThat(names.get(0),is("int_column"));
        assertThat(names.get(1),is("decimal_column"));
        assertThat(names.get(2),is("bit_column"));
        List<String> types = schemaGenerationInfo.types;
        assertThat(types.get(0),is("int"));
        assertThat(types.get(1),is("bytes"));
        assertThat(types.get(2),is("boolean"));
        assertThat(types.get(3),is("boolean"));
        assertThat(types.get(4),is("long"));
        assertThat(types.get(5),is("int"));
        assertThat(types.get(6),is("long"));
        assertThat(types.get(7),is("double"));
        assertThat(types.get(8),is("string"));
        List<Map<String, String>> params = schemaGenerationInfo.params;
        assertThat(schemaGenerationInfo.params.get(0).size(),is(0));
        assertThat(((String)schemaGenerationInfo.params.get(1).get(AVRO_LOGICAL_TYPE)),is("decimal"));
        assertThat(schemaGenerationInfo.params.get(5).get(AVRO_LOGICAL_TYPE),is("date"));
        assertThat(schemaGenerationInfo.params.get(6).get(AVRO_LOGICAL_TYPE),is("timestamp-millis"));
        assertThat(schemaGenerationInfo.table,is("test"));
    }
}
