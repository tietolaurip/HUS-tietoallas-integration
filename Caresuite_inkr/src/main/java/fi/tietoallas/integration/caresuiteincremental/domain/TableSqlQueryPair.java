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
 * Domain class with combines everything needed fetch new data from
 * database and send it to kafka
 *
 * @author xxkallia
 */
public class TableSqlQueryPair {

    public final String tableName;
    public final String sql;
    public final BigDecimal dboid;
    public final String columnName;
    public final Timestamp lastUpdatedAt;
    public final String columnQuery;

    private TableSqlQueryPair(String tableName, String columnName, String sql, BigDecimal dboid,
                             Timestamp lastUpdatedAt,String columnQuery) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.sql = sql;
        this.dboid=dboid;
        this.lastUpdatedAt=lastUpdatedAt;
        this.columnQuery=columnQuery;
    }
    public static class Builder{
        private String tableName;
        private String sql;
        private BigDecimal dboid;
        private String columnName;
        private Timestamp lastUpdatedAt;
        private String columnQuery;

        public Builder(){

        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withColumnName(final String columnName){
            this.columnName=columnName;
            return this;
        }
        public Builder withSql(final String sql){
            this.sql=sql;
            return this;
        }
        public Builder withDbIOD(final BigDecimal dboid){
            this.dboid=dboid;
            return this;
        }
        public Builder withLastRunAt(final Timestamp lastUpdatedAt){
            this.lastUpdatedAt=lastUpdatedAt;
            return this;
        }
        public Builder withtColumnQuery(final String columnQuery){
            this.columnQuery=columnQuery;
            return this;
        }
        public TableSqlQueryPair build(){
            return new TableSqlQueryPair(tableName,columnName,sql,dboid,lastUpdatedAt,columnQuery);
        }
    }
}
