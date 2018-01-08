package fi.tietoallas.integration.pseudonymization;

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

import fi.tietoallas.integration.pseudonymization.identity.IdentityManager;
import fi.tietoallas.integration.pseudonymization.identity.IdentityManagerShaImpl;
import fi.tietoallas.integration.xsd.SchemaLoader;
import net.sf.saxon.lib.NamespaceConstant;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.*;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class XmlDocumentPseudonymizerTest {

    @Test
    public void testMatchByType() throws Exception {
        IdentityManager identityManager = new IdentityManagerShaImpl();
        InputStream schemaStream = getClass().getResourceAsStream("/schemas/CDA_Fi.xsd");
        InputStream ruleStream = getClass().getResourceAsStream("/cda_pseudonymizer_rules_by_type.json");

        Pseudonymizer<String,Document> pseudonymizer =
                new XmlDocumentPseudonymizer(
                        identityManager,
                        SchemaLoader.fromStream(schemaStream, "schemas"),
                        ruleStream);

        URL documentUrl = getClass().getResource("/KIE_Kielto_20170127.xml");
        byte[] documentBytes = Files.readAllBytes(Paths.get(documentUrl.toURI()));
        String cda = new String(documentBytes, "UTF-8");

        long timestamp = System.currentTimeMillis();
        Document result = pseudonymizer.pseudonymize(cda);
        System.out.println("By type duration: " + (System.currentTimeMillis() - timestamp));
        checkResults(result);
    }

    @Test
    public void testMatchByName() throws Exception {

        IdentityManager identityManager = new IdentityManagerShaImpl();
        InputStream ruleStream = getClass().getResourceAsStream("/cda_pseudonymizer_rules_by_name.json");

        Pseudonymizer<String,Document> pseudonymizer =
                new XmlDocumentPseudonymizer(identityManager, ruleStream);

        URL documentUrl = getClass().getResource("/KIE_Kielto_20170127.xml");
        byte[] documentBytes = Files.readAllBytes(Paths.get(documentUrl.toURI()));
        String cda = new String(documentBytes, "UTF-8");

        long timestamp = System.currentTimeMillis();
        Document result = pseudonymizer.pseudonymize(cda);
        System.out.println("By name duration: " + (System.currentTimeMillis() - timestamp));
        checkResults(result);
    }

    private void checkResults(Document result) throws XPathFactoryConfigurationException, XPathExpressionException {

        System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
        XPathFactory xpathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        XPath xpath = xpathFactory.newXPath();

        Element root = result.getDocumentElement();

        String expectedHash = "j1O8tnLW2BFB8u6/ysNOXgAIQuF6ItFDQ+49pj8Bnfs="; // SHA-256 of 150245-987R
        assertEquals(expectedHash, xpath.evaluate("//*:recordTarget/*:patientRole/*:id/@extension", root, XPathConstants.STRING));
        assertEquals("", xpath.evaluate("//*:recordTarget/*:patientRole/*:patient/*:name/*:given", root, XPathConstants.STRING));
        assertEquals("", xpath.evaluate("//*:recordTarget/*:patientRole/*:patient/*:name/*:family", root, XPathConstants.STRING));

        Element sectionWithSSN = (Element) xpath.evaluate("//*:section[./*:code/@code='18' and ./*:code/@codeSystem='1.2.246.537.6.12.2002.331']", root, XPathConstants.NODE);
        assertEquals(expectedHash, xpath.evaluate("*:text/*:content", sectionWithSSN, XPathConstants.STRING));

        assertNull(xpath.evaluate("//*:localHeader/*:signatureCollection", sectionWithSSN, XPathConstants.NODE));
        assertNotNull(xpath.evaluate("//*:localHeader/*:recordStatus", sectionWithSSN, XPathConstants.NODE));

        // Also check that the pseudonymizer is not too eager
        assertEquals("Ei", xpath.evaluate("//*:section[./*:code/@code='5']/*:text/*:content", root, XPathConstants.STRING));
    }
}
