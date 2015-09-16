package com.hp.octane.plugins.jenkins.tests.detection;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

public class ResultFieldsXmlReader {

    private Reader input;
    private XMLEventReader eventReader;
    private ResultFields resultFields;
    private List<TestAttributes> testAttributes;

    public ResultFieldsXmlReader(Reader input) throws XMLStreamException {
        this.input = input;
        eventReader = XMLInputFactory.newInstance().createXMLEventReader(input);
        resultFields = new ResultFields();
        testAttributes = new LinkedList<TestAttributes>();
    }

    public TestResultContainer readXml() {
        boolean fieldsElement = false;
        try {
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event instanceof StartElement) {
                    StartElement element = (StartElement) event;
                    String localName = element.getName().getLocalPart();
                    if ("fields".equals(localName)) {
                        fieldsElement = true;
                    }
                    if ("field".equals(localName)) {
                        if (!fieldsElement) {
                            Assert.fail("<Field> element found, but surrounding element '<fields>' is missing in the XML file");
                        }
                        String type = element.getAttributeByName(new QName("type")).getValue();
                        String value = element.getAttributeByName(new QName("value")).getValue();
                        if (type.equals("Framework")) {
                            resultFields.setFramework(value);
                        } else if (type.equals("Testing_Tool_Type")) {
                            resultFields.setTestingTool(value);
                        } else if (type.equals("Test_Level")) {
                            resultFields.setTestLevel(value);
                        }
                    }
                    if ("test".equals(localName)) {
                        String moduleName = element.getAttributeByName(new QName("module")).getValue();
                        String packageName = element.getAttributeByName(new QName("package")).getValue();
                        String className = element.getAttributeByName(new QName("class")).getValue();
                        String testName = element.getAttributeByName(new QName("name")).getValue();
                        testAttributes.add(new TestAttributes(moduleName, packageName, className, testName));
                    }
                }
            }
            IOUtils.closeQuietly(input);
            eventReader.close();
        } catch (XMLStreamException e){
            throw new RuntimeException(e);
        }
        return new TestResultContainer(testAttributes, resultFields);
    }

    public class TestResultContainer {

        private List<TestAttributes> testAttributes;
        private ResultFields resultFields;

        public TestResultContainer(List<TestAttributes> testAttributes, ResultFields resultFields) {
            this.testAttributes = testAttributes;
            this.resultFields = resultFields;
        }

        public List<TestAttributes> getTestAttributes() {
            return testAttributes;
        }

        public ResultFields getResultFields() {
            return resultFields;
        }
    }

    public class TestAttributes {
        private final String moduleName;
        private final String packageName;
        private final String className;
        private final String testName;

        public TestAttributes(final String moduleName, final String packageName, final String className, final String testName) {
            this.moduleName = moduleName;
            this.packageName = packageName;
            this.className = className;
            this.testName = testName;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getClassName() {
            return className;
        }

        public String getTestName() {
            return testName;
        }
    }
}
