/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests.gherkin;

import com.hpe.application.automation.tools.octane.tests.junit.TestResultStatus;
import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by franksha on 20/03/2016.
 */
public class GherkinTestResult implements TestResult {
    private Map<String, String> attributes;
    private Element contentElement;

    public GherkinTestResult(String name, Element xmlElement, long duration, TestResultStatus status) {
        this.attributes = new HashMap<>();
        this.attributes.put("name", name);
        this.attributes.put("duration", String.valueOf(duration));
        this.attributes.put("status", status.toPrettyName());
        this.contentElement = xmlElement;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Element getXmlElement() {
        return contentElement;
    }

    @Override
    public void writeXmlElement(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("gherkin_test_run");
        if (attributes != null) {
            for (String attrName : attributes.keySet()) {
                writer.writeAttribute(attrName, attributes.get(attrName));
            }
        }
        writeXmlElement(writer, contentElement);
        writer.writeEndElement();
    }

    private void writeXmlElement(XMLStreamWriter writer, Element rootElement) throws XMLStreamException {
        if (rootElement != null) {
            writer.writeStartElement(rootElement.getTagName());
            for (int a = 0; a < rootElement.getAttributes().getLength(); a++) {
                String attrName = rootElement.getAttributes().item(a).getNodeName();
                writer.writeAttribute(attrName, rootElement.getAttribute(attrName));
            }
            NodeList childNodes = rootElement.getChildNodes();
            for (int c = 0; c < childNodes.getLength(); c++) {
                Node child = childNodes.item(c);
                if (child instanceof Element) {
                    writeXmlElement(writer, (Element) child);
                } else if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                    writer.writeCharacters(child.getNodeValue());
                }
            }
            writer.writeEndElement();
        }
    }
}
