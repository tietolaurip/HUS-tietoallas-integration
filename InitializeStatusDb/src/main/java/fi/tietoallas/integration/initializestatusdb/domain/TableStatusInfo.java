package fi.tietoallas.integration.initializestatusdb.domain;

/*-
 * #%L
 * initialize-status-db
 * %%
 * Copyright (C) 2017 - 2018 Pivotal Software, Inc.
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
public class TableStatusInfo {

    public final String tableName;
    public final String parameterType;
    public final String keyColumn;
    public final String timeColumn;

    private TableStatusInfo(String tableName,String parameterType,String keyColumn,String timeColumn){
        this.tableName=tableName;
        this.parameterType=parameterType;
        this.keyColumn=keyColumn;
        this.timeColumn=timeColumn;
    }
    public static class Builder {

        private String tableName;
        private String parameterType;
        private String keyColumn;
        private String timeColumn;

        public Builder(){

        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withParameterType(final String parameterType){
            this.parameterType=parameterType;
            return this;
        }
        public Builder withKeyColoumn(final String keyColoumn){
            this.keyColumn=keyColoumn;
            return this;
        }
        public Builder withTimeColumn(final String timeColumn){
            this.timeColumn=timeColumn;
            return this;
        }
        public TableStatusInfo build(){
            return new TableStatusInfo(tableName,parameterType,keyColumn,timeColumn);
        }
    }
}
