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
import fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools;
import fi.tietoallas.integration.caresuiteincremental.domain.TableIdColumnInformation;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CareSuiteRepositoryTest {


    @Mock
    private JdbcTemplate jdbcTemplate;

    private CareSuiteRepository careSuiteRepository;

    private static final String TEST_TABLE="A";

    private static final List<String> columnNames = asList("AOID","AESC","ATYPEOID");
    @Before
    public void setUp(){
        SchemaGenerationInfo info = getSchemaGenerationInfo();
        Schema schema = new Schema.Parser().parse(DynamicAvroTools.toIntegrationAvroSchema("testi",info));

        careSuiteRepository  = new CareSuiteRepository(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), Mockito.any(ColumnRowMapper.class))).thenReturn(asList(new SchemaGenerationInfo(TEST_TABLE,columnNames,null,null)));
        when(jdbcTemplate.query(anyString(),Mockito.any(GenericRecordMapper.class))).thenReturn(asList(new GenericData.Record(schema)));
        when(jdbcTemplate.query(anyString(),Mockito.any(CareSuiteRepository.OnIntDatalakeMapper.class))).thenReturn(Arrays.asList(new TableIdColumnInformation(TEST_TABLE,new BigDecimal(200),"test",Timestamp.valueOf(LocalDateTime.now().minusYears(1)))));
    }

    @Test
    public void testGetColumnName(){

        List<SchemaGenerationInfo> allergies = careSuiteRepository.getColumnNames(TEST_TABLE, join(columnNames, ","));
        assertThat(allergies,notNullValue());
        assertThat(allergies,hasSize(1));
    }
    @Test
    public void testGetNewData(){
        String sql = "select "+ join(columnNames,",")+ " from "+TEST_TABLE;
        List<GenericRecord> newData = careSuiteRepository.getNewData(sql, TEST_TABLE);
        assertThat(newData,notNullValue());
        assertThat(newData,not(hasSize(0)));
        GenericRecord genericRecord = newData.get(0);
        assertThat(genericRecord.getSchema(),notNullValue());
    }
    @Test
    public void testGetUpdatedBDOID(){
        List<TableIdColumnInformation> updatedBDOID = careSuiteRepository.getUpdatedBDOID(TEST_TABLE, Timestamp.valueOf(LocalDateTime.now().minusYears(50)));
        assertThat(updatedBDOID,notNullValue());
        assertThat(updatedBDOID,hasSize(0));
    }



    private SchemaGenerationInfo getSchemaGenerationInfo(){
        List<String> names = asList("a","b","c","d");
        List<String> types = asList("string","string","string","string");
        return new SchemaGenerationInfo("testa",names,types, asList(null,null,null,null));

    }

}
