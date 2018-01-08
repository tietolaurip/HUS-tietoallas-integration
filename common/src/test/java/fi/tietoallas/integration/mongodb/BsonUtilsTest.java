package fi.tietoallas.integration.mongodb;

/*-
 * #%L
 * common
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
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.junit.Test;

import static org.junit.Assert.*;

public class BsonUtilsTest {

    @Test
    public void testFind() {
        DBObject bson = (DBObject) JSON.parse("{\"a\": \"b\"}");
        assertEquals("b", BsonUtils.find(bson, "a"));

        bson = (DBObject) JSON.parse("{\"a\": {\"b\": \"c\"}}");
        assertEquals("c", BsonUtils.find(bson, "a/b"));

        bson = (DBObject) JSON.parse("{\"a\": {\"b\": [\"c\", \"d\"]}}");
        assertEquals("d", BsonUtils.find(bson, "a/b/1"));

        bson = (DBObject) JSON.parse("{\"a\": {\"b\": [{\"d\": \"e\"}]}}");
        assertEquals("e", BsonUtils.find(bson, "a/b/0/d"));
    }
}
