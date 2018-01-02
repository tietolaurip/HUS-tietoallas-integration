package fi.tietoallas.integration.metadata.jpalib.domain;

/*-
 * #%L
 * jpa-lib
 * %%
 * Copyright (C) 2017 - 2018 Tieto Finland Oyj
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
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

public class ColumnCsv {
    // symbols for referring to columns in the standard format metadata CSV for columns
    public final static int ID=0;
    public final static int DATA_COLUMN_NAME=1;
    public final static int ORIG_COLUMN_NAME=2;
    public final static int DATA_TABLE_NAME=3;
    public final static int DATA_SET_NAME=4;
    public final static int ORIG_TYPE=5;
    public final static int DESCRIPTION=6;
    public final static int CODE_SYSTEM=7;
    public final static int SOURCES=8;
    public final static int FORMATION_RULE=9;
    public final static int IS_PRIMARY_KEY=10;
    public final static int FOREIGN_KEY=11;
    public final static int FORMAT=12;
    public final static int UNIT_OF_MEASURE=13;
    public final static int PSEUDONYMIZATION_FUNCTION=14;
    public final static int STATUS=15;
    public final static int COMMENT=16;
    public final static int SOURCE_DOCUMENT=17;
    public final static int METADATA_LAST_UPDATED=18;
    public final static int COLUMN_COUNT=19;
    /* Create a Column from its array of Strings as a standard parsing of a CSV line
     * NOTE: id fields is never set
     */
    public static Column createFrom(String[] line) throws ParseException {
        if (line.length!=COLUMN_COUNT)
            throw new ParseException("Invalid line in columns.csv: "+ Arrays.toString(line),0);
        Column col = new Column(line[DATA_SET_NAME],line[DATA_TABLE_NAME],line[DATA_COLUMN_NAME]);
        col.setOrigColumnName(getIfNotEmpty(line[ORIG_COLUMN_NAME]));
        col.setOrigType(getIfNotEmpty(line[ORIG_TYPE]));
        col.setDescription(getIfNotEmpty(line[DESCRIPTION]));
        col.setCodeSystem(getIfNotEmpty(line[CODE_SYSTEM]));
        col.setSources(getIfNotEmpty(line[SOURCES]));
        col.setCodeSystem(getIfNotEmpty(line[FORMATION_RULE]));
        col.setPrimaryKey(line[IS_PRIMARY_KEY].equals("1") || line[IS_PRIMARY_KEY].equalsIgnoreCase("true"));
        col.setForeignKey(getIfNotEmpty(line[FOREIGN_KEY]));
        col.setFormat(getIfNotEmpty(line[FORMAT]));
        col.setUnitOfMeasure(getIfNotEmpty(line[UNIT_OF_MEASURE]));
        col.setPseudonymizationFunction(getIfNotEmpty(line[PSEUDONYMIZATION_FUNCTION]));
        col.setStatus(getIfNotEmpty(line[STATUS]));
        col.setComment(getIfNotEmpty(line[COMMENT]));
        col.setSourceDocument(getIfNotEmpty(line[SOURCE_DOCUMENT]));
        col.setMetadataLastUpdated(new Date());
        return col;
    }
    private static String getIfNotEmpty(String value) {
        if (value.equals(""))
            return null;
        else
            return value;
    }
}
