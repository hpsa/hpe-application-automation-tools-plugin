/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.tests.detection;

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
                    if ("test_fields".equals(localName)) {
                        fieldsElement = true;
                    }
                    if ("test_field".equals(localName)) {
                        if (!fieldsElement) {
                            Assert.fail("<test_field> element found, but surrounding element '<test_fields>' is missing in the XML file");
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
                    if ("test_run".equals(localName)) {
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
