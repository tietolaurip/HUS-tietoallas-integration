/*-
 * #%L
 * common-incremental
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

package fi.tietoallas.incremental.common.commonincremental.domain;

import java.util.List;
import java.util.Map;

/**
 * Domain class to store information needed to generate acro schema
 * name of table, names of fields, avro type of fields and additional info
 * for avro types when it's not basic type e.g. Timestamp
 *
 * @author Antti Kalliokoski
 */
public class SchemaGenerationInfo {
   public final String table;
   public final List<String> names;
   public final List<String> types;
   public final List<Map<String, String>> params;

    public SchemaGenerationInfo(String table, List<String> names, List<String> types, List<Map<String, String>> params) {
        this.table = table;
        this.names = names;
        this.types = types;
        this.params = params;
    }
}
