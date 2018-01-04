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

public class LineInfo {

    public final String dataType;
    public final Boolean isPrimary;
    public final String rowName;
    public final Boolean isDataChangeColumn;
    public final String description;

    private LineInfo(String dataType, Boolean isPrimary, String rowName, Boolean isDataChangeColumn,String description) {
        this.dataType = dataType;
        this.isPrimary = isPrimary;
        this.rowName = rowName;
        this.isDataChangeColumn = isDataChangeColumn;
        this.description=description;
    }
    public static class Builder {
        private String dataType;
        private Boolean isPrimary;
        private String rowName;
        private Boolean isDataChangeColumn;
        private  String description;

        public Builder(){

        }
        public Builder (final LineInfo lineInfo){
            this.description=lineInfo.description;
            this.dataType=lineInfo.dataType;
            this.isDataChangeColumn=lineInfo.isDataChangeColumn;
            this.rowName=lineInfo.rowName;
            this.isPrimary=lineInfo.isPrimary;
        }
        public Builder withDataType(final String dataType){
            this.dataType=dataType;
            return this;
        }
        public Builder withIsPrimary(final Boolean isPrimary){
            this.isPrimary=isPrimary;
            return this;
        }
        public Builder withRowName(final String rowName){
            this.rowName=rowName;
            return this;
        }
        public Builder withDataChangeColumn(final Boolean isDataChangeColumn){
            this.isDataChangeColumn=isDataChangeColumn;
            return this;
        }
        public Builder withDescription(final String description){
            this.description=description;
            return this;
        }
        public LineInfo build(){
            return new LineInfo(dataType,isPrimary,rowName,isDataChangeColumn,description);
        }
    }
}
