package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultStatus;
import com.hp.mqm.clt.xml.AbstractXmlIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class XmlProcessor {

    public List<TestResult> processJUnitXmlFile(File junitXmlFile) {
        if (junitXmlFile == null || !junitXmlFile.canRead()) {
            System.out.println("Can not read the JUnit file: " + junitXmlFile.getName());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        List<TestResult> testResults = new LinkedList<TestResult>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(junitXmlFile);
            JUnitXmlIterator iterator = new JUnitXmlIterator(fis);
            while (iterator.hasNext()) {
                testResults.add(iterator.next());
            }
        } catch (IOException e) {
            System.out.println("Unable to process JUnit XML file: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (XMLStreamException e) {
            System.out.println("Unable to process JUnit XML file, XML stream exception has occurred: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (InterruptedException e) {
            System.out.println("Unable to process JUnit XML file, process was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } finally {
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
        }

        if (testResults.isEmpty()) {
            System.out.println("No valid test results to push");
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        return testResults;
    }

    private class JUnitXmlIterator extends AbstractXmlIterator<TestResult> {

        private String packageName;
        private String className;
        private String testName;
        private long duration;
        private TestResultStatus status;

        public JUnitXmlIterator(InputStream read) throws XMLStreamException {
            super(read);
        }

        @Override
        protected void onEvent(XMLEvent event) throws IOException {
            if (event instanceof StartElement) {
                StartElement element = (StartElement) event;
                String localName = element.getName().getLocalPart();
                if ("testcase".equals(localName)) { // NON-NLS
                    packageName = "";
                    className = "";
                    testName = "";
                    duration = 0;
                    status = TestResultStatus.PASSED;

                    Iterator iterator = element.getAttributes();
                    while (iterator.hasNext()) {
                        Attribute attribute = (Attribute) iterator.next();
                        if ("classname".equals(attribute.getName().toString())) {
                            parseClassname(attribute.getValue());
                        } else if ("name".equals(attribute.getName().toString())) {
                            testName = attribute.getValue();
                        } else if ("time".equals(attribute.getName().toString())) {
                            duration = parseTime(attribute.getValue());
                        }
                    }
                } else if ("skipped".equals(localName)) { // NON-NLS
                    status = TestResultStatus.SKIPPED;
                } else if ("failure".equals(localName)) { // NON-NLS
                    status = TestResultStatus.FAILED;
                } else if ("error".equals(localName)) { // NON-NLS
                    status = TestResultStatus.FAILED;
                }
            } else if (event instanceof EndElement) {
                EndElement element = (EndElement) event;
                String localName = element.getName().getLocalPart();

                if ("testcase".equals(localName) && StringUtils.isNotEmpty(testName)) { // NON-NLS
                    addItem(new TestResult(packageName, className, testName, status, duration));
                }
            }
        }

        private long parseTime(String timeString) {
            String time = timeString.replace(",","");
            try {
                float seconds = Float.parseFloat(time);
                return (long) (seconds * 1000);
            } catch (NumberFormatException e) {
                try {
                    return new DecimalFormat().parse(time).longValue();
                } catch (ParseException ex) {
                    System.out.println("Unable to parse test duration: " + timeString);
                }
            }
            return 0;
        }

        private void parseClassname(String fqn) {
            int p = fqn.lastIndexOf(".");
            className = fqn.substring(p + 1);
            if (p > 0) {
                packageName = fqn.substring(0, p);
            } else {
                packageName = "";
            }
        }
    }
}
