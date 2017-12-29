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

package fi.tietoallas.incremental.common.commonincremental.util;


import org.apache.commons.lang3.StringUtils;

/**
 * Class for common string conversion static methods
 */

public class CommonConversionUtils {

    /**
     *
     * @param originalTableName string to normalize
     * @return string which scandinavic letters and removes spaces and dash or null if string is null or empty
     */
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
}
