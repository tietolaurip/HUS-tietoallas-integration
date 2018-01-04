package fi.tietoallas.integration.common.utils;

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

import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;

public class CommonConversionUtils {

    public static String convertTableName(final String originalTableName){
        if (StringUtils.isNotBlank(originalTableName)){
          return  originalTableName.
                    replace(" ","_").
                    replace("-","_").
                    replace("Ä","a").
                    replace("ä","a").
                    replace("Å","a").
                    replace("å","a").
                    replace("Ö","o").
                    replace("ö","o").toLowerCase();
        }
        return null;
    }
    public static String convertTableNameAndConvertComma(final String originalTableName){
        if (StringUtils.isNoneBlank(originalTableName)){
          return   convertTableName(originalTableName)
                    .replace(".","_");
        }
        return null;
    }
    public static String generateKey(){
        return new SimpleDateFormat("yyyy-MM-dd_HH").format(new Date());
    }

}
