package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class XmlProcessorTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testXmlProcessor_minimalAcceptedJUnitFormat() throws URISyntaxException {
        // Public API requires at least testName, duration, started and status fields to be filled for every test
        XmlProcessor xmlProcessor = new XmlProcessor();
        long beforeProcessing = System.currentTimeMillis();
        List<TestResult> testResults = xmlProcessor.processSurefireTestReport(new File(getClass().getResource("JUnit-minimalAccepted.xml").toURI()));
        long afterProcessing = System.currentTimeMillis();
        Assert.assertNotNull(testResults);
        Assert.assertEquals(4, testResults.size());
        assertTestResult(testResults.get(0), "", "", "testName", TestResultStatus.PASSED,
                1, beforeProcessing, afterProcessing);
        assertTestResult(testResults.get(1), "", "", "testNameSkipped", TestResultStatus.SKIPPED,
                2, beforeProcessing, afterProcessing);
        assertTestResult(testResults.get(2), "", "", "testNameFailed", TestResultStatus.FAILED,
                3, beforeProcessing, afterProcessing);
        assertTestResult(testResults.get(3), "", "", "testNameWithError", TestResultStatus.FAILED,
                4, beforeProcessing, afterProcessing);
    }

    @Test
    public void testXmlProcessor_testMissingTestName() throws URISyntaxException, IOException, XMLStreamException, InterruptedException {
        systemOutRule.enableLog();
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Assert.assertTrue(systemOutRule.getLog().contains("Unable to process Surefire XML file"));
            }
        });
        XmlProcessor xmlProcessor = new XmlProcessor();
        xmlProcessor.processSurefireTestReport(new File(getClass().getResource("JUnit-missingTestName.xml").toURI()));
    }

    @Test
    public void testXmlProcessor_unclosedElement() throws URISyntaxException {
        systemOutRule.enableLog();
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Assert.assertTrue(systemOutRule.getLog().contains("Unable to process Surefire XML file"));
            }
        });
        XmlProcessor xmlProcessor = new XmlProcessor();
        xmlProcessor.processSurefireTestReport(new File(getClass().getResource("JUnit-unclosedElement.xml").toURI()));
    }

    @Test
    public void testXmlProcessor_junitFileDoesNotExist() throws URISyntaxException {
        systemOutRule.enableLog();
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Assert.assertTrue(systemOutRule.getLog().contains("Can not read the Surefire XML file: fileDoesNotExist.xml"));
            }
        });
        XmlProcessor xmlProcessor = new XmlProcessor();
        xmlProcessor.processSurefireTestReport(new File("fileDoesNotExist.xml"));
    }

    @Test
    public void testXmlProcessor_writeXml() throws URISyntaxException, IOException, XMLStreamException {
        File targetFile = temporaryFolder.newFile();
        long currentTime = System.currentTimeMillis();
        XmlProcessor xmlProcessor = new XmlProcessor();
        List<TestResult> testResults = new LinkedList<TestResult>();
        testResults.add(new TestResult("com.examples.example", "SampleClass", "testOne", TestResultStatus.PASSED, 2, currentTime));
        testResults.add(new TestResult("com.examples.example", "SampleClass", "testTwo", TestResultStatus.SKIPPED, 5, currentTime));
        testResults.add(new TestResult("com.examples.example", "SampleClass", "testThree", TestResultStatus.SKIPPED, 5, currentTime));
        List<String> tags = new LinkedList<String>();
        tags.add("OS:Linux");
        tags.add("DB:Oracle");
        List<String> fields = new LinkedList<String>();
        fields.add("Framework:TestNG");
        fields.add("Test_Level:Unit Test");
        Settings settings = new Settings();
        settings.setTags(tags);
        settings.setFields(fields);
        settings.setProductArea(1001);
        settings.setRelease(1010);
        settings.setRequirement(1020);

        xmlProcessor.writeTestResults(testResults, settings, targetFile);

        Set<XmlElement> xmlElements = new HashSet<XmlElement>();
        xmlElements.add(new XmlElement("tag", "OS", "Linux"));
        xmlElements.add(new XmlElement("tag", "DB", "Oracle"));
        xmlElements.add(new XmlElement("field", "Framework", "TestNG"));
        xmlElements.add(new XmlElement("field", "Test_Level", "Unit Test"));
        xmlElements.add(new XmlElement("productAreaRef", "1001"));
        xmlElements.add(new XmlElement("backlogItemRef", "1020"));
        xmlElements.add(new XmlElement("releaseRef", "1010"));
        assertXml(new LinkedList<TestResult>(testResults), xmlElements, targetFile);
    }

    private void assertTestResult(TestResult testResult, String packageName, String className, String testName,
                                  TestResultStatus result, long duration, long startedLowerBound, long startedUpperBound) {
        Assert.assertEquals(packageName, testResult.getPackageName());
        Assert.assertEquals(className, testResult.getClassName());
        Assert.assertEquals(testName, testResult.getTestName());
        Assert.assertEquals(result, testResult.getResult());
        Assert.assertEquals(duration, testResult.getDuration());
        Assert.assertTrue(testResult.getStarted() >= startedLowerBound);
        Assert.assertTrue(testResult.getStarted() <= startedUpperBound);
    }

    private void assertXml(List<TestResult> expectedTestResults, Set<XmlElement> expectedElements, File xmlFile) throws FileNotFoundException, XMLStreamException {
        FileInputStream fis = new FileInputStream(xmlFile);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(fis);

        boolean isFirstEvent = true;
        while(xmlStreamReader.hasNext()){
            if (!isFirstEvent) {
                xmlStreamReader.next();
            } else {
                isFirstEvent = false;
            }

            if (xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
                String localName = xmlStreamReader.getLocalName();
                if ("tag".equals(localName)) {
                    assertElement(localName, false, xmlStreamReader, expectedElements);
                } else if ("field".equals(localName)) {
                    assertElement(localName, false, xmlStreamReader, expectedElements);
                } else if ("productAreaRef".equals(localName)) {
                    assertElement(localName, true, xmlStreamReader, expectedElements);
                } else if ("backlogItemRef".equals(localName)) {
                    assertElement(localName, true, xmlStreamReader, expectedElements);
                } else if ("releaseRef".equals(localName)) {
                    assertElement(localName, true, xmlStreamReader, expectedElements);
                } else if ("test".equals(localName)) {
                    assertXmlTest(xmlStreamReader, expectedTestResults);
                }
            }
        }
        xmlStreamReader.close();
        IOUtils.closeQuietly(fis);
        Assert.assertTrue(expectedElements.isEmpty());
        Assert.assertTrue(expectedTestResults.isEmpty());
    }

    private void assertXmlTest(XMLStreamReader xmlStreamReader, List<TestResult> testResults) {
        String testName = xmlStreamReader.getAttributeValue(null, "name");
        String statusName = xmlStreamReader.getAttributeValue(null, "status");
        String duration = xmlStreamReader.getAttributeValue(null, "duration");
        String started = xmlStreamReader.getAttributeValue(null, "started");
        Assert.assertNotNull(testName);
        Assert.assertNotNull(statusName);
        Assert.assertNotNull(duration);
        Assert.assertNotNull(started);

        TestResult testToFind = new TestResult(
                xmlStreamReader.getAttributeValue(null, "package"),
                xmlStreamReader.getAttributeValue(null, "class"),
                testName, TestResultStatus.fromPrettyName(statusName),
                Long.valueOf(duration), Long.valueOf(started));

        for (TestResult testResult : testResults) {
            if (areTestResultsEqual(testResult, testToFind)) {
                testResults.remove(testResult);
                return;
            }
        }
        Assert.fail("Can not find the expected test result");
    }

    private boolean areTestResultsEqual(TestResult first, TestResult second) {
        return StringUtils.equals(first.getPackageName(), second.getPackageName()) &&
                StringUtils.equals(first.getClassName(), second.getClassName()) &&
                StringUtils.equals(first.getTestName(), second.getTestName()) &&
                first.getResult() == second.getResult() &&
                first.getDuration() == second.getDuration() &&
                first.getStarted() == second.getStarted();
    }

    private void assertElement(String elemName, boolean isReference, XMLStreamReader xmlStreamReader, Set<XmlElement> expectedElements) {
        String type = null;
        String value;
        if (isReference) {
            value = xmlStreamReader.getAttributeValue(null, "id");
            Assert.assertNotNull(value);
        } else {
            type = xmlStreamReader.getAttributeValue(null, "type");
            value = xmlStreamReader.getAttributeValue(null, "value");
            Assert.assertNotNull(type);
            Assert.assertNotNull(value);
        }
        XmlElement element = new XmlElement(elemName, type, value);
        Assert.assertTrue(expectedElements.contains(element));
        expectedElements.remove(element);
    }

    private class XmlElement {

        private String elemName;
        private String type;
        private String value;

        private XmlElement(String elemName, String type, String value) {
            this.elemName = elemName;
            this.type = type;
            this.value = value;
        }

        private XmlElement(String elemName, String value) {
            this(elemName, null, value);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof XmlElement))
                return false;
            if (obj == this)
                return true;
            return (StringUtils.equals (this.elemName, ((XmlElement) obj).elemName) &&
                    StringUtils.equals (this.type, ((XmlElement) obj).type) &&
                    StringUtils.equals (this.value, ((XmlElement) obj).value));
        }

        public int hashCode(){
            int prime = 31;
            int result = (elemName != null) ? elemName.hashCode() : prime;
            result = prime * result + ((type != null) ? type.hashCode() : prime);
            result = prime * result + ((value != null) ? value.hashCode() : prime);
            return result;
        }
    }
}
