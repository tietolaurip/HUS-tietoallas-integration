package fi.tietoallas.integration.healthweb;

/*-
 * #%L
 * healthweb-integration
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

import fi.tietoallas.integration.mq.MessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HealthwebServiceTest {

    @Autowired
    private HealthwebService messageService;

    private MessageProducerStub messageProducer = new MessageProducerStub();

    @Before
    public void setUp() {
        messageService.setMessageProducer(messageProducer);
    }

    @Test
    public void testReceive() throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(getClass().getResource("/RCMR_IN100016FI01.xml").toURI()));
        String message = new String(encoded, "UTF-8");
        messageService.receive(message);

        assertTrue(messageProducer.latestKeys.get("healthweb-orig").startsWith("healthweb;1.2.246.537.10.15675350.91007.3135.2;"));

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        org.w3c.dom.Document result = builder.parse(
                new ByteArrayInputStream(
                        messageProducer.latestValues.get("healthweb-orig").getBytes(StandardCharsets.UTF_8.name())));

        assertEquals("1.2.246.537.10.15675350.91007.3135.2", xpath.evaluate(createXpath("ClinicalDocument/id/@root"), result));
        assertEquals("070472-8967", xpath.evaluate(createXpath("ClinicalDocument/recordTarget/patientRole/id/@extension"), result));
        assertEquals("TESTI", xpath.evaluate(createXpath("ClinicalDocument/recordTarget/patientRole/patient/name/given"), result));
    }

    /**
     * Creates a namespace agnostic version from the given xpath.
     *
     * @param xpath the xpath
     * @return a namespace agnostic version of the xpath
     */
    private String createXpath(String xpath) {
        return String.join("/", Arrays.asList(xpath.split("/")).stream().map(t -> {
            if (t.startsWith("@")) {
                return t;
            } else {
                return "*[local-name()='" + t + "']";
            }
        }).collect(Collectors.toList()));
    }

    public class MessageProducerStub implements MessageProducer {
        Map<String, String> latestKeys = new HashMap<>();
        Map<String, String> latestValues = new HashMap<>();

        @Override
        public void produce(String topic, String key, String value) {
            this.latestKeys.put(topic, key);
            this.latestValues.put(topic, value);
        }
    }
}
