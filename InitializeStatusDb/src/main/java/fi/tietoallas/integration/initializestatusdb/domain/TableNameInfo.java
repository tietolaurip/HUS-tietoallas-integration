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
public class TableNameInfo {
    public final String orignalName;
    public final String hiveName;

    private TableNameInfo(String orignalName, String hiveName) {
        this.orignalName = orignalName;
        this.hiveName = hiveName;
    }
    public static class Builder {
        private String orignalName;
        private String hiveName;

        public Builder(){ }

        public Builder withOriginalName(final String originalName){
            this.orignalName=originalName;
            return this;
        }
        public Builder withHiveName(final String hiveName){
            this.hiveName=hiveName;
            return this;
        }
        public TableNameInfo build(){
            return new TableNameInfo(orignalName,hiveName);
        }

    }
}
