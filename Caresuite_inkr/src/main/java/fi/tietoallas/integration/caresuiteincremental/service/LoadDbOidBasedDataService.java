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
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

;

/**
 * Class which gathers incremental changes that needs to be fetched
 * using XON_INIT table or biggest value
 *
 * @author xxkallia
 */

public class LoadDbOidBasedDataService {

    private StatusDatabaseRepository statusDatabaseRepository;
    private CareSuiteRepository careSuiteRepository;

    public LoadDbOidBasedDataService(StatusDatabaseRepository statusDatabaseRepository,CareSuiteRepository careSuiteRepository){
        this.statusDatabaseRepository=statusDatabaseRepository;
        this.careSuiteRepository=careSuiteRepository;
    }

    /**
     *
     * @return List of all tables that have new or updated rows
     */

    public List<TableSqlQueryPair> gatherData(final String integration) {
        List<TableKeyUpdatableIds> updatedTables = getUpdatedTables(integration);
        List<TableSqlQueryPair> tableSqlQueryPairs = new ArrayList<>();
        for (TableKeyUpdatableIds tableKeyLastId : updatedTables) {
            String sql = createQuery(tableKeyLastId.caresuiteTableName,tableKeyLastId.keyColumn,tableKeyLastId.lastUsedValue,tableKeyLastId.biggetValue,tableKeyLastId.columnQuery );
            tableSqlQueryPairs.add(new TableSqlQueryPair.Builder()
                    .withColumnName(tableKeyLastId.keyColumn)
                    .withSql(sql)
                    .withDbIOD(tableKeyLastId.biggetValue)
                    .withTableName(tableKeyLastId.caresuiteTableName)
                    .withLastRunAt(tableKeyLastId.lastAccessAt)
                    .withtColumnQuery(tableKeyLastId.columnQuery)
                    .build());
        }
        return tableSqlQueryPairs;
    }

    protected List<TableKeyUpdatableIds> getUpdatedTables(String integration) {

        List<TableStatusInformation> compareToXONinformation = statusDatabaseRepository.selectUsingXON(integration);
        List<TableKeyUpdatableIds> returnList = new ArrayList<>();
        for (TableStatusInformation tableStatusInformation : compareToXONinformation) {
            List<TableIdColumnInformation> updatedBDOID = careSuiteRepository.getUpdatedBDOID(tableStatusInformation.tableName, tableStatusInformation.lastAccessAt);
            List<BigDecimal> bigDecimals = updatedBDOID.stream()
                    .map(t -> t.id)
                    .collect(Collectors.toList());
            returnList.add(new TableKeyUpdatableIds(tableStatusInformation.tableName, tableStatusInformation.keyColumn,bigDecimals, tableStatusInformation.lastAccessAt,tableStatusInformation.lastUsedValue,tableStatusInformation.columnQuery));
        }
        return returnList;

    }

    protected String createQuery(String table,String keyColumn, List<BigDecimal> currentValues,BigDecimal largestValue,String columsn) {
        if (currentValues == null || currentValues.isEmpty()){
            if (largestValue == null){
                return "select "+columsn +" from "+table;
            }
            return "select "+ columsn +" from "+table+ " where "+ keyColumn+ " > "+largestValue.toPlainString();
        }
        else {
            return "select "+columsn+ " from " + table + " where " + keyColumn + " in (" + StringUtils.join(currentValues, ",") + ") OR " + keyColumn + " > " + largestValue.toPlainString();
        }
    }
}
