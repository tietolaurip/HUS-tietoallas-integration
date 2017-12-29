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

package fi.tietoallas.integration.caresuiteincremental.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain class for XON_INT_DATALAKE incremental change table
 * @author xxkallia
 */
public class TableIdColumnInformation {

    public final String table;
    public final BigDecimal id;
    public final String columnName;
    public final Timestamp created;

    public TableIdColumnInformation(String table, BigDecimal id, String columnName, Timestamp created) {
        this.table = table;
        this.id = id;
        this.columnName=columnName;
        this.created=created;
    }

    /**
     *
     * @return Database BOID for specific table
     */
    public BigDecimal getId(){
        return id;
    }

    /**
     *
     * @return Table name in Caresuite database
     */
    public String getTable(){
        return table;
    }
}
