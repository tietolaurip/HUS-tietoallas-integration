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
import net.sf.saxon.lib.NamespaceConstant;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A pseudonymizer for XML documents. The implementation is not thread-safe.
 */
public class XmlDocumentPseudonymizer extends AbstractPseudonymizer<String, org.w3c.dom.Document> {

    /** An XML document builder factory. */
    DocumentBuilderFactory builderFactory;

    /** An XML document builder. */
    DocumentBuilder builder;

    /** An XPath factory. */
    private XPathFactory xpathFactory;

    /** An XPath evaluation environment. */
    private XPath xpath;

    /** The XML schemas to use. */
    private Schema schema;

    /** Mapping from an XPath string to a pre-compiled XPathExpression */
    private Map<String, XPathExpression> xpathCache;

    /** A flag indicating the match mode (by name or by type). */
    private boolean matchByName = true;

    /**
     * Creates a new instance. The instance matches elements by name.
     *
     * @param ruleStream the rules to use
     */
    public XmlDocumentPseudonymizer(IdentityManager identityManager, InputStream ruleStream) {

        super(identityManager, ruleStream);

        try {
            builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(false);
            builderFactory.setValidating(false);
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON,
                "net.sf.saxon.xpath.XPathFactoryImpl");
        try {
            xpathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
            xpath = xpathFactory.newXPath();
        } catch (XPathFactoryConfigurationException e) {
            throw new RuntimeException(e);
        }

        // Pre-compile XPath expressions. Actual performance gains may be limited but this has the
        // added benefit of validating the expressions.
        xpathCache = new HashMap<>();
        List<PseudonymizerRule> all = rules.values().stream().flatMap(c -> c.stream()).collect(Collectors.toList());
        for (PseudonymizerRule rule : all) {
            cache(rule.getHashedFields());
            cache(rule.getClearedFields());
            cache(rule.getRemovedFields());
            cache(rule.getConditions());
        }
    }

    private void cache(List<String> expressions) {
        if (expressions != null) {
            expressions.forEach(field -> {
                try {
                    xpathCache.put(field, xpath.compile(field));
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Creates a new instance. The instance matches elements by type.
     *
     * @param ruleStream the rules to use
     * @param schema the XML Schema to use
     */
    public XmlDocumentPseudonymizer(IdentityManager identityManager, Schema schema, InputStream ruleStream) {

        this(identityManager, ruleStream);
        this.schema = schema;
        try {
            // Override previously set values
            builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            builderFactory.setSchema(schema);
            builder = builderFactory.newDocumentBuilder();
        }  catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        matchByName = false;
    }

    @Override
    public Document pseudonymize(String source) {
        try {
            org.w3c.dom.Document document = builder.parse(
                    new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8.name())));

            // Instead of hashing identities on the fly, first collect them all in to a set and then
            // retrieve the matching pseudo-identities in one call to minimize calls to the pseudo-proxy.
            Set<Node> hashedNodes = new HashSet<>();
            collectAndAnonymize(document.getDocumentElement(), hashedNodes);
            pseudonymize(hashedNodes);

            return document;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void collectAndAnonymize(Element element, Set<Node> hashedNodes) throws XPathExpressionException {
        NodeList nodes = element.getChildNodes();
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            Node childNode = nodes.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String childElementIdentifier =
                        matchByName ?
                                childElement.getNodeName() :
                                childElement.getSchemaTypeInfo().getTypeName();
                if (childElementIdentifier.startsWith("#AnonType_")) {
                    childElementIdentifier = childElementIdentifier.replace("#AnonType_", "");
                }
                if (rules.containsKey(childElementIdentifier)) {
                    for (PseudonymizerRule rule : rules.get(childElementIdentifier)) {
                        if (!rule.hasConditions() || conditionsMatch(rule, childElement)) {
                            hashedNodes.addAll(collectHashedNodes(childElement, rule));
                            anonymize(childElement, rule);
                        }
                    }
                }
                collectAndAnonymize(childElement, hashedNodes);
            }
        }
    }

    private boolean conditionsMatch(PseudonymizerRule rule, Element childElement) throws XPathExpressionException {
        for (String condition : rule.getConditions()) {
            if (xpathCache.get(condition).evaluate(childElement, XPathConstants.NODE) != null) {
                return true;
            }
        }
        return false;
    }

    private Set<Node> collectHashedNodes(Element element, PseudonymizerRule rule) throws XPathExpressionException {
       Set<Node> result = new HashSet<>();
        if (rule.getHashedFields() != null && !rule.getHashedFields().isEmpty()) {
            for (String expression : rule.getHashedFields()) {
                NodeList matches = (NodeList) xpathCache.get(expression).evaluate(element, XPathConstants.NODESET);
                for (int i = 0, n = matches.getLength(); i < n; i++) {
                    result.add(matches.item(0));
                }
            }
        }
        return result;
    }

    private void pseudonymize(Set<Node> nodes) {
        // Collect identities
        Set<String> identities = new HashSet<>();
        for (Node node : nodes) {
            if (node instanceof Attr) {
                identities.add(((Attr) node).getValue());
            } else if (node instanceof Element) {
                NodeList content = node.getChildNodes();
                for (int j = 0, m = content.getLength(); j < m; j++) {
                    if (content.item(j) instanceof Text) {
                        identities.add(((Text) content.item(j)).getData());
                    }
                }
            }
        }

        // Pseudonymize identities
        Map<String, String> pseudoIdentities = identityManager.pseudonymize(new ArrayList<>(identities));
        for (Node node : nodes) {
            if (node instanceof Attr) {
                String value = ((Attr) node).getValue();
                ((Attr) node).setValue(pseudoIdentities.get(value));
            } else if (node instanceof Element) {
                NodeList content = node.getChildNodes();
                for (int j = 0, m = content.getLength(); j < m; j++) {
                    if (content.item(j) instanceof Text) {
                        String value = ((Text) content.item(j)).getData();
                        ((Text) content.item(j)).setData(pseudoIdentities.get(value));
                    }
                }
            }
        }
    }

    private void anonymize(Element element, PseudonymizerRule rule) throws XPathExpressionException {
        if (rule.getClearedFields() != null && !rule.getClearedFields().isEmpty()) {
            for (String expression : rule.getClearedFields()) {
                NodeList matches = (NodeList) xpathCache.get(expression).evaluate(element, XPathConstants.NODESET);
                for (int i = 0, n = matches.getLength(); i < n; i++) {
                    Node match = matches.item(i);
                    if (match instanceof Attr) {
                        ((Attr) match).setValue("");
                    } else if (match instanceof Element) {
                        NodeList content = match.getChildNodes();
                        for (int j = 0, m = content.getLength(); j < m; j++) {
                            if (content.item(j) instanceof Text) {
                                ((Text) content.item(j)).setData("");
                            }
                        }
                    }
                }
            }
        }
        if (rule.getRemovedFields() != null && !rule.getRemovedFields().isEmpty()) {
            for (String expression : rule.getRemovedFields()) {
                NodeList matches = (NodeList) xpathCache.get(expression).evaluate(element, XPathConstants.NODESET);
                for (int i = 0, n = matches.getLength(); i < n; i++) {
                    Node match = matches.item(i);
                    match.getParentNode().removeChild(match);
                }
            }
        }
    }


}
