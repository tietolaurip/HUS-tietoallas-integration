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

import fi.tietoallas.integration.exception.ParseException;
import fi.tietoallas.integration.mq.MessageProducer;
import fi.tietoallas.monitoring.MetricService;
import org.hl7.v3.ClinicalDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Properties;

/**
 * Service for receiving CDA R2 documents from Healthweb.
 */
@Service
public class HealthwebService {

    /** The name of the integration. */
    private final static String INTEGRATION_NAME = "healthweb";

    /** The key separator. */
    private final static String KEY_SEPARATOR = ";";

    /** An XPath indicating the CDA location. */
    private static final String CDA_XPATH = "//controlActProcess/subject/clinicalDocument/text";

    /** Our message producer */
    private MessageProducer messageProducer;

    /** The JAXB context */
    private JAXBContext jaxbContext;

    /** The metric service */
    private MetricService metricService;

    @Autowired
    public void setMessageProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Autowired
    public void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * Creates a new instance.
     */
    public HealthwebService() {
        try {
            this.jaxbContext = JAXBContext.newInstance(ClinicalDocument.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives a CDA R2 document from Healthweb and passes it to the integration specific message queue.
     *
     * @param message the CDA R2 XML as a string
     */
    public void receive(final String message) {

        String rawData = extractClinicalDocument(message);
        ClinicalDocument clinicalDocument = parseClinicalDocument(rawData);

        Instant timestamp = Instant.now();
        String key = INTEGRATION_NAME + KEY_SEPARATOR +
                clinicalDocument.getId().getRoot() + KEY_SEPARATOR +
                timestamp.toString();

        messageProducer.produce(INTEGRATION_NAME + "-orig", key, rawData);
        metricService.reportSendBytes(INTEGRATION_NAME, rawData.getBytes().length);
    }

    /**
     * Extracts the clinical document from the wrapper.
     *
     * @param message the message
     * @return the clinical document
     */
    public String extractClinicalDocument(String message) {

        org.w3c.dom.Document document;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8.name())));
        } catch (Exception e) {
            throw new ParseException("Unable to parse message: " + e.getMessage(), e);
        }

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate(CDA_XPATH, document, XPathConstants.NODE);
            if (node == null) {
                throw new ParseException("The document does not contain the expected payload: " + CDA_XPATH);
            }

            Session s = Session.getDefaultInstance(new Properties());
            InputStream is = new ByteArrayInputStream(node.getTextContent().getBytes(StandardCharsets.UTF_8.name()));
            MimeMessage mimeMessage = new MimeMessage(s, is);
            if (mimeMessage.getContent() instanceof String) {
                throw new ParseException("The payload is not a valid MIME multipart message.");
            }

            Multipart multipart = (Multipart) mimeMessage.getContent();
            if (multipart.getCount() == 0) {
                throw new ParseException("The payload is not a valid MIME multipart message (parts=0).");
            }
            return (String) multipart.getBodyPart(0).getContent();

        } catch (Exception e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /**
     * Parses the given clinical document.
     *
     * @param document a textual representation of a clinical document
     * @return the clinical document
     * @throws Exception thrown if the parsing fails
     */
    private ClinicalDocument parseClinicalDocument(String document) {
        try {
            Reader reader = new StringReader(document);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ClinicalDocument) JAXBIntrospector.getValue(unmarshaller.unmarshal(reader));
        } catch (Exception e) {
            System.out.println(document);
            throw new ParseException("Unable to parse clinical document: " + e.getMessage(), e);
        }
    }

}
