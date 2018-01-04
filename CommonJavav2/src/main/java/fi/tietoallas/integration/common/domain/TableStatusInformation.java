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

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TableStatusInformation {
    public final String tableName;
    public final String keyColumn;
    public final String timeColumn;
    public final BigDecimal lastUsedValue;
    public final Timestamp lastAccessAt;
    public final String parameterType;
    public final String searchColumn;
    public final String query;
    public final String originalDatabase;
    public final String columnQuery;

    private TableStatusInformation(String tableName, String keyColumn, BigDecimal lastUsedValue, Timestamp lastAccessAt, String timeColumn,String parameterType,final String searchColumn,
                                  final String query,final String originalDatabase,String columnQuery) {
        this.tableName = tableName;
        this.keyColumn = keyColumn;
        this.lastUsedValue = lastUsedValue;
        this.lastAccessAt=lastAccessAt;
        this.timeColumn=timeColumn;
        this.parameterType=parameterType;
        this.searchColumn=searchColumn;
        this.query=query;
        this.originalDatabase=originalDatabase;
        this.columnQuery=columnQuery;
    }
    public String getTableName(){
        return tableName;
    }

    public static class Builder {
        private String tableName;
        private String keyColumn;
        private String timeColumn;
        private BigDecimal lastUsedValue;
        private Timestamp lastAccessAt;
        private String parameterType;
        private String searchColumn;
        private String query;
        private String originalDatabase;
        private String columnQuery;
        public Builder(){

        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withKeyColumn(final String keyColumn){
            this.keyColumn=keyColumn;
            return this;
        }
        public Builder withTimeColumn(final String timeColumn){
            this.timeColumn=timeColumn;
            return this;
        }
        public Builder withLastUsedValue(final BigDecimal lastUsedValue){
            this.lastUsedValue=lastUsedValue;
            return this;
        }
        public Builder withLassAccessAt(final Timestamp lassAccessAt){
            this.lastAccessAt=lassAccessAt;
            return this;
        }
        public Builder withParameterType(final String parameterType){
            this.parameterType=parameterType;
            return this;
        }
        public Builder withSearchColumn(final String searchColumn){
            this.searchColumn=searchColumn;
            return this;
        }
        public Builder withQuery(final String query){
            this.query=query;
            return this;
        }
        public Builder withOriginalDatabase(final String originalDatabase){
            this.originalDatabase=originalDatabase;
            return this;
        }
        public Builder withColumnQuery(final String columnQuery){
            this.columnQuery=columnQuery;
            return this;
        }
        public TableStatusInformation build(){
            return new TableStatusInformation(tableName,keyColumn,lastUsedValue,
                    lastAccessAt,timeColumn,parameterType,searchColumn,query,originalDatabase,columnQuery);
        }
    }
}
