package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultStatus;
import com.hp.mqm.clt.xml.TestResultXmlWriter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class XmlProcessorTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule();

    @Test
    public void testXmlProcessor_minimalAcceptedJUnitFormat() throws URISyntaxException {
        // Public API requires at least testName, duration and status fields to be filled for every test
        XmlProcessor xmlProcessor = new XmlProcessor();
        List<TestResult> testResults = xmlProcessor.processJUnitXmlFile(new File(getClass().getResource("JUnit-minimalAccepted.xml").toURI()));
        Assert.assertNotNull(testResults);
        Assert.assertEquals(testResults.size(), 4);
        AssertTestResult(testResults.get(0), "", "", "testName", TestResultStatus.PASSED, 0);
        AssertTestResult(testResults.get(1), "", "", "testNameSkipped", TestResultStatus.SKIPPED, 0);
        AssertTestResult(testResults.get(2), "", "", "testNameFailed", TestResultStatus.FAILED, 0);
        AssertTestResult(testResults.get(3), "", "", "testNameWithError", TestResultStatus.FAILED, 0);
    }

    @Test
    public void testXmlProcessor_testMissingTestName() throws URISyntaxException, IOException, XMLStreamException, InterruptedException {
        XmlProcessor xmlProcessor = new XmlProcessor();
        List<TestResult> testResults = xmlProcessor.processJUnitXmlFile(new File(getClass().getResource("JUnit-missingTestName.xml").toURI()));
        Assert.assertNotNull(testResults);
        Assert.assertEquals(testResults.size(), 3);
        AssertTestResult(testResults.get(0), "com.examples.example", "SampleClass", "testOne", TestResultStatus.PASSED, 2);
        AssertTestResult(testResults.get(1), "com.examples.example", "SampleClass", "testTwo", TestResultStatus.SKIPPED, 5);
        AssertTestResult(testResults.get(2), "com.examples.example", "SampleClass", "testThree", TestResultStatus.SKIPPED, 5);
    }

    @Test
    public void testXmlProcessor_unclosedElement() throws URISyntaxException {
        systemOutRule.enableLog();
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Assert.assertTrue(systemOutRule.getLog().contains("Unable to parse JUnit XML file"));
            }
        });
        XmlProcessor xmlProcessor = new XmlProcessor();
        xmlProcessor.processJUnitXmlFile(new File(getClass().getResource("JUnit-unclosedElement.xml").toURI()));
    }

    @Test
    public void testXmlProcessor_junitFileDoesNotExist() throws URISyntaxException {
        systemOutRule.enableLog();
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Assert.assertTrue(systemOutRule.getLog().contains("Can not read the JUnit file: fileDoesNotExist.xml"));
            }
        });
        XmlProcessor xmlProcessor = new XmlProcessor();
        xmlProcessor.processJUnitXmlFile(new File("fileDoesNotExist.xml"));
    }

    private void AssertTestResult(TestResult testResult, String packageName, String className,
                                 String testName, TestResultStatus result, long duration) {
        Assert.assertEquals(testResult.getPackageName(), packageName);
        Assert.assertEquals(testResult.getClassName(), className);
        Assert.assertEquals(testResult.getTestName(), testName);
        Assert.assertEquals(testResult.getResult(), result);
        Assert.assertEquals(testResult.getDuration(), duration);
    }
}
