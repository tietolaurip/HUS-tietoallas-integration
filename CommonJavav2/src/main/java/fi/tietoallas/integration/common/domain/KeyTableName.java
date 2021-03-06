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


public class KeyTableName {

    public final String key;
    public final String table;

    private KeyTableName(String key, String table) {
        this.key = key;
        this.table = table;
    }
    public static class Builder {
        private String key;
        private String table;
        public Builder(){

        }
        public Builder withKey(final String key){
            this.key=key;
            return this;
        }
        public Builder withTable(final String table){
            this.table=table;
            return this;
        }
        public KeyTableName build(){
            return new KeyTableName(key,table);
        }
    }
}
