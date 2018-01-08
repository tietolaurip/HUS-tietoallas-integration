package fi.tietoallas.integration.xsd;

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

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SchemaLoader {

    /**
     * Utility method for loading an XSD schema from a stream.
     *
     * @param schemaStream the stream
     * @param prefix the directory prefix of the schema(s)
     * @return the schema
     */
    public static Schema fromStream(InputStream schemaStream, String prefix) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        schemaFactory.setResourceResolver(new ResourceResolver(prefix));
        try {
            return schemaFactory.newSchema(new StreamSource(schemaStream));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility method for loading and XSD schema from a file.
     *
     * @param file the file
     * @return the schema
     */
    public static Schema fromFile(File file) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        try {
            return schemaFactory.newSchema(file);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A resource resolver to use when loading a schema.
     */
    public static class ResourceResolver implements LSResourceResolver {

        private String prefix;
        public ResourceResolver(String prefix) {
            this.prefix = prefix;
        }

        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            LSInput input = new DOMInputImpl();
            input.setBaseURI(baseURI);
            input.setSystemId(systemId);
            input.setPublicId(publicId);
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(
                    this.prefix + File.separator + systemId);
            input.setCharacterStream(new InputStreamReader(resourceAsStream));
            return input;
        }
    }
}
