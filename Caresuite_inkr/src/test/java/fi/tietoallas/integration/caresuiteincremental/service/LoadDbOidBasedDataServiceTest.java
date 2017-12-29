package fi.tietoallas.integration.caresuiteincremental.service;

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

import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.caresuiteincremental.domain.TableIdColumnInformation;
import fi.tietoallas.integration.caresuiteincremental.domain.TableKeyUpdatableIds;
import fi.tietoallas.integration.caresuiteincremental.domain.TableSqlQueryPair;
import fi.tietoallas.integration.caresuiteincremental.repository.CareSuiteRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class LoadDbOidBasedDataServiceTest {

    private static final String TEST_TABLE="test_table";
    private static final BigDecimal LAST_USED_VALUE= new BigDecimal(1000);
    private static final String KEY_COLUMN="key_column";
    private static final Timestamp DATA_CREATED = Timestamp.valueOf(LocalDateTime.now().minusDays(1L));
    private static final String COLUMN_QUERY="key_column,data_column, info_column";
    private static final String INTEGRATION_NAME="caresuite";
    private LoadDbOidBasedDataService loadDbOidBasedDataService;
    @Mock
    private CareSuiteRepository careSuiteRepository;
    @Mock
    private StatusDatabaseRepository statusDatabaseRepository;

    @Before
    public void setUp(){
        when(statusDatabaseRepository.selectUsingXON(eq(INTEGRATION_NAME))).thenReturn(asList(getTestStatusInformation()));
        when(careSuiteRepository.getUpdatedBDOID(eq(TEST_TABLE), any(Timestamp.class)))
                .thenReturn(asList(testTableIdColumnInformation()));
        loadDbOidBasedDataService=new LoadDbOidBasedDataService(statusDatabaseRepository,careSuiteRepository);

    }

    @Test
    public void testFirstTimeQuery(){
        String query = loadDbOidBasedDataService.createQuery(TEST_TABLE, KEY_COLUMN, new ArrayList<>(), null, COLUMN_QUERY);
        assertThat(query,is(("select "+COLUMN_QUERY+" from "+TEST_TABLE)));
    }
    @Test
    public void testFetchNewData(){
        String query = loadDbOidBasedDataService.createQuery(TEST_TABLE,KEY_COLUMN,new ArrayList<>(),new BigDecimal(1000),COLUMN_QUERY);
        asList(query,is(("select "+COLUMN_QUERY+ " from "+ TEST_TABLE+" where "+KEY_COLUMN+" > 1000")));
    }
    @Test
    public void fetchNewUpdatedData(){
        String query = loadDbOidBasedDataService.createQuery(TEST_TABLE,KEY_COLUMN,asList(new BigDecimal(200),new BigDecimal(400)),new BigDecimal(200),COLUMN_QUERY);
        assertThat(query,is("select "+COLUMN_QUERY+ " from "+TEST_TABLE+ " where "+KEY_COLUMN+" in (200,400) OR key_column > 200"));
    }
    @Test
    public void testGetUpdatedTables(){
        List<TableKeyUpdatableIds> updatedTables = loadDbOidBasedDataService.getUpdatedTables(INTEGRATION_NAME);
        assertThat(updatedTables,hasSize(1));
        TableKeyUpdatableIds tableKeyUpdatableIds = updatedTables.get(0);
        assertThat(tableKeyUpdatableIds.biggetValue,is(LAST_USED_VALUE));
        assertThat(tableKeyUpdatableIds.caresuiteTableName,is(TEST_TABLE));
        assertThat(tableKeyUpdatableIds.columnQuery,is(COLUMN_QUERY));
        assertThat(tableKeyUpdatableIds.keyColumn,is(KEY_COLUMN));
        assertThat(tableKeyUpdatableIds.lastAccessAt,is(DATA_CREATED));
        assertThat(tableKeyUpdatableIds.lastUsedValue,notNullValue());
    }
    @Test
    public void testGatherData(){
        List<TableSqlQueryPair> tableSqlQueryPairs = loadDbOidBasedDataService.gatherData(INTEGRATION_NAME);
        assertThat(tableSqlQueryPairs,hasSize(1));
        TableSqlQueryPair pair = tableSqlQueryPairs.get(0);
        assertThat(pair.tableName,is(TEST_TABLE));
        assertThat(pair.columnQuery,is(COLUMN_QUERY));
        assertThat(pair.columnName,is(KEY_COLUMN));
        assertThat(pair.lastUpdatedAt,is(DATA_CREATED));
        assertThat(pair.dboid,is(LAST_USED_VALUE));
        assertThat(pair.sql,is("select "+COLUMN_QUERY+ " from "+TEST_TABLE+ " where "+KEY_COLUMN+" in (1000) OR key_column > 1000"));
    }


    private TableStatusInformation getTestStatusInformation() {
        return new TableStatusInformation.Builder()
                .withParameterType("HISTORY_TABLE_LOOKUP")
                .withLastUsedValue(LAST_USED_VALUE)
                .withKeyColumn(KEY_COLUMN)
                .withTableName(TEST_TABLE)
                .withLassAccessAt(DATA_CREATED)
                .withColumnQuery(COLUMN_QUERY)
                .build();
    }

    private TableIdColumnInformation testTableIdColumnInformation(){
        return new TableIdColumnInformation(TEST_TABLE,LAST_USED_VALUE,KEY_COLUMN,DATA_CREATED);
    }

}
