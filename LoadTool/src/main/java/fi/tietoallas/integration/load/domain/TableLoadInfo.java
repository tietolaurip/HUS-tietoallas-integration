package fi.tietoallas.integration.load.domain;

/*-
 * #%L
 * copytool
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
import java.util.List;

/**
 * Domain class for status database information for initial load
 *
 * @author Antti Kalliokoski
 */
public class TableLoadInfo {

    public final String tableName;
    public final String lowerBound;
    public final String upperBound;
    public final String partitionColumn;
    public final String customSql;
    public final String originalDatabase;
    public final String numPartitions;
    public final List<String> columnNames;

    private TableLoadInfo(String tableName, String lowerBound, String upperBound, String partitionColumn,String customSql
            ,String originalDatabase,String numPartitions,List<String> columnNames) {
        this.tableName = tableName;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.partitionColumn = partitionColumn;
        this.customSql=customSql;
        this.originalDatabase=originalDatabase;
        this.numPartitions=numPartitions;
        this.columnNames=columnNames;
    }

    public static class Builder {

        private String tableName;
        private String lowerBound;
        private String upperBound;
        private String partitionColumn;
        private String customSql;
        private String originalDatabase;
        private String numPartitions;
        private List<String> columnNames;

        public Builder(){

        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withLowerBound(final String lowerBound){
            this.lowerBound=lowerBound;
            return this;
        }
        public Builder withUpperBound(final String upperBound){
            this.upperBound=upperBound;
            return this;
        }
        public Builder withPartitionColumn(final String partitionColumn){
            this.partitionColumn=partitionColumn;
            return this;
        }
        public Builder withCustomSql(final String customSql){
            this.customSql=customSql;
            return this;
        }
        public Builder withOriginalDatabase(final String originalDatabase){
            this.originalDatabase=originalDatabase;
            return this;
        }
        public Builder withNumPartitions(final String numPartitions){
            this.numPartitions=numPartitions;
            return this;
        }
        public Builder withColumnNames(final List<String> columnNames){
            this.columnNames=columnNames;
            return this;

        }
        public TableLoadInfo build(){
            return new TableLoadInfo(tableName,lowerBound,upperBound,partitionColumn,customSql,originalDatabase,numPartitions,columnNames);
        }

    }

}
