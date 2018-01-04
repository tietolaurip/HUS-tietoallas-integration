package fi.tietoallas.integration.common.mapper;

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
import fi.tietoallas.integration.common.utils.HiveConversionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableCreateScriptMapper implements ResultSetExtractor<String> {
    private String table;
    private String integration;
    private boolean staging;
    private String storageLocation;

    public TableCreateScriptMapper(String table,String integration,boolean staging,String storageLocation){
        this.table=table;
        this.integration=integration;
        this.staging=staging;
        this.storageLocation=storageLocation;
    }

    @Override
    public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        return HiveConversionUtils.generateCreate(resultSet.getMetaData(),this.table,this.integration,this.staging,this.storageLocation);
    }
}
