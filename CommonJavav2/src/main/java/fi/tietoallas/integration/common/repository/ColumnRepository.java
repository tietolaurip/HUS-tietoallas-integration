package fi.tietoallas.integration.common.repository;

/*-
 * #%L
 * jpa-lib
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

import fi.tietoallas.integration.common.domain.Column;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository  extends JpaRepository<Column,Long> {
    List<Column> findByName(String name);

    List<Column> findByDataSetName(String dataSetName);

    List<Column> findByDataSetNameAndTableName(String dataSetName, String tableName);

    @Query("SELECT c FROM Column c WHERE data_set_name = ?1 AND data_table_name = ?2 AND " +
            "pseudonymization_function = ?3")
    List<Column> findByPseudonymizationFunction(String dataSetName, String tableName, String function);
    List<Column> findByDataSetNameAndTableNameAndName(String dataSetName, String tableName, String name);

}
