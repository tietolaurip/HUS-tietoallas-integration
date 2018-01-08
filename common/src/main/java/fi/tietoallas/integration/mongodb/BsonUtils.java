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

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.BasicBSONList;

import java.util.Arrays;

/**
 * Utility methods for working with BSON (for testing purposes).
 */
public class BsonUtils {

    /**
     * Finds a string property from the given path.
     *
     * @param object the BSON object
     * @param path the path (e.g. 'clinicalDocument/id/root')
     * @return the string property matching the given path
     */
    public static String find(Document object, String path) {
        String[] tokens = path.split("/");
        return find((BSONObject) object.get(tokens[0]), Arrays.copyOfRange(tokens, 1, tokens.length));
    }

    /**
     * Finds a string property from the given path.
     *
     * @param object the BSON object
     * @param path the path (e.g. 'clinicalDocument/id/root')
     * @return the string property matching the given path
     */
    public static String find(BSONObject object, String path) {
        return find(object, path.split("/"));
    }

    private static String find(BSONObject object, String[] path) {
        if (path.length == 1) {
            if (object instanceof BasicBSONObject) {
                return ((BasicBSONObject) object).getString(path[0]);
            } else if (object instanceof BasicBSONList){
                return (String) ((BasicBSONList) object).get(Integer.parseInt(path[0]));
            } else {
                throw new IllegalStateException("BsonUtils::findFromObject in illegal state.");
            }
        } else {
            BSONObject child;
            if (object instanceof BasicBSONObject) {
                child = (BSONObject) object.get(path[0]);
            } else if (object instanceof BasicBSONList){
                child = (BSONObject) ((BasicBSONList) object).get(Integer.parseInt(path[0]));
            } else {
                throw new IllegalStateException("BsonUtils::findFromObject in illegal state.");
            }
            return find(child, Arrays.copyOfRange(path, 1, path.length));
        }
    }
}
