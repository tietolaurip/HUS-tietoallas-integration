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

import java.util.List;

public class ParseTableInfo {

    public enum TableType {
        FULL_TABLE,
        TIME_COMPARISATION,
        HISTORY_TABLE_LOOKUP,
        EXLUDE

        }
    public final String tableName;
    public final TableType tableType;
    public final List<LineInfo> lines;
    public final String timeColunm;
    public final String integrationName;
    public final String description;
    public final String originalDatabase;
    public final String columnQuery;

    private ParseTableInfo(String tableName, TableType tableType, List<LineInfo> lines, String timeColunm,String integrationName,String description,String originalDatabase,String columnQuery) {
        this.tableName = tableName;
        this.tableType = tableType;
        this.lines = lines;
        this.timeColunm=timeColunm;
        this.integrationName=integrationName;
        this.description=description;
        this.originalDatabase=originalDatabase;
        this.columnQuery=columnQuery;
    }
    public static class Builder {
        private String tableName;
        private TableType tableType;
        private List<LineInfo> lines;
        private String timeColumn;
        private String integrationName;
        private String description;
        private String originalDatabase;
        private String columnQuery;

        public Builder(){

        }
        public Builder(final ParseTableInfo info){
            this.tableName=info.tableName;
            this.tableType=info.tableType;
            this.lines=info.lines;
            this.timeColumn =info.timeColunm;
            this.integrationName=info.integrationName;
            this.description=info.description;
        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withTableType(final TableType tableType){
            this.tableType=tableType;
            return this;
        }
        public Builder withLines(final List<LineInfo> lines){
            this.lines=lines;
            return this;
        }
        public Builder withTimeColumn(final String timeColumn){
            this.timeColumn=timeColumn;
            return this;
        }
        public Builder withIntegrationName(final String integrationName){
            this.integrationName=integrationName;
            return this;
        }
        public Builder withDescription(final String description){
            this.description=description;
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
        public ParseTableInfo build(){
            return new ParseTableInfo(tableName,tableType,lines,timeColumn,integrationName,description,originalDatabase,columnQuery);
        }
    }
}
