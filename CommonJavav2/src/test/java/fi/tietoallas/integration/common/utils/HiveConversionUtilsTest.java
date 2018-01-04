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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Types;

import static fi.tietoallas.integration.common.utils.HiveConversionUtils.toHiveType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
@RunWith(JUnit4.class)
public class HiveConversionUtilsTest {

    private static final String HIVE_TIME_TYPE="TIMESTAMP";
    private static final String HIVE_INT_TYPE="int";
    private static final String HIVE_SMALL_INT_TYPE="SMALLINT";
    private static final String HIVE_TINY_INT_TYPE="TINYINT";
    private static final String HIVE_BOOLEAN_TYPE="BOOLEAN";
    private static final String HIVE_FLOAT_TYPE="FLOAT";
    private static final String HIVE_BINARY_TYPE="BINARY";
    private static final String HIVE_STRING_TYPE="STRING";
    private static final String HIVE_DOUBLE_TYPE="DOUBLE";


    @Test
    public void testTimeTypes(){
        assertThat(HIVE_TIME_TYPE,is(toHiveType(Types.DATE,0,0)));
        assertThat(HIVE_TIME_TYPE,is(toHiveType(Types.TIME,0,0)));
        assertThat(HIVE_TIME_TYPE,is(toHiveType(Types.TIME_WITH_TIMEZONE,0,0)));
        assertThat(HIVE_TIME_TYPE,is(toHiveType(Types.TIMESTAMP,0,0)));
        assertThat(HIVE_TIME_TYPE,is(toHiveType(Types.TIMESTAMP_WITH_TIMEZONE,0,0)));

    }
    @Test
    public void testIntType(){
        assertThat(HIVE_INT_TYPE,is(toHiveType(Types.INTEGER,0,0)));
    }
    @Test
    public void testSmallInt(){
        assertThat(HIVE_SMALL_INT_TYPE,is(toHiveType(Types.SMALLINT,0,0)));

    }
    @Test
    public void testTinyInt(){
        assertThat(HIVE_TINY_INT_TYPE,is(toHiveType(Types.TINYINT,0,0)));
    }
    @Test
    public void testBoolean(){
        assertThat(HIVE_BOOLEAN_TYPE,is(toHiveType(Types.BIT,0,0)));
        assertThat(HIVE_BOOLEAN_TYPE,is(toHiveType(Types.BOOLEAN,0,0)));
    }
    @Test
    public void testFloat(){
        assertThat(HIVE_FLOAT_TYPE,is(toHiveType(Types.FLOAT,0,0)));
    }
    @Test
    public void testBinary(){
        assertThat(HIVE_BINARY_TYPE,is(toHiveType(Types.BLOB,0,0)));
        assertThat(HIVE_BINARY_TYPE,is(toHiveType(Types.CLOB,0,0)));
        assertThat(HIVE_BINARY_TYPE,is(toHiveType(Types.BINARY,0,0)));
        assertThat(HIVE_BINARY_TYPE,is(toHiveType(Types.LONGVARBINARY,0,0)));
        assertThat(HIVE_BINARY_TYPE,is(toHiveType(Types.NCLOB,0,0)));
        assertThat(HIVE_BINARY_TYPE,is(toHiveType(Types.VARBINARY,0,0)));;
    }
    @Test
    public void testString(){
        assertThat(HIVE_STRING_TYPE,is(toHiveType(Types.VARCHAR,0,0)));
        assertThat(HIVE_STRING_TYPE,is(toHiveType(Types.NVARCHAR,0,0)));
        assertThat(HIVE_STRING_TYPE,is(toHiveType(Types.CHAR,0,0)));
        assertThat(HIVE_STRING_TYPE,is(toHiveType(Types.NCHAR,0,0)));
        assertThat(HIVE_STRING_TYPE,is(toHiveType(Types.SQLXML,0,0)));
    }
    @Test
    public void testDouble(){
        assertThat(HIVE_DOUBLE_TYPE,is(toHiveType(Types.DOUBLE,0,0)));
    }
}
