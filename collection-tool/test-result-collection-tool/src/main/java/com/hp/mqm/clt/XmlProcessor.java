package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.xml.JunitXmlIterator;
import com.hp.mqm.clt.xml.TestResultXmlWriter;

import javax.xml.bind.ValidationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class XmlProcessor {

    public List<TestResult> processJunitTestReport(File junitTestReport, Long started) {
        if (junitTestReport == null || !junitTestReport.canRead()) {
            String filePathInfo = (junitTestReport == null) ? "" : ": " + junitTestReport.getAbsolutePath();
            System.out.println("Can not read the JUnit XML file" + filePathInfo);
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        List<TestResult> testResults = new LinkedList<TestResult>();
        try {
            JunitXmlIterator iterator = new JunitXmlIterator(junitTestReport, started);
            while (iterator.hasNext()) {
                testResults.add(iterator.next());
            }
        } catch (IOException e) {
            System.out.println("Unable to process JUnit XML file '" + junitTestReport.getAbsolutePath() + "': " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (XMLStreamException e) {
            System.out.println("Unable to process JUnit XML file '" + junitTestReport.getAbsolutePath() + "', XML stream exception has occurred: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (InterruptedException e) {
            System.out.println("Unable to process JUnit XML file '" + junitTestReport.getAbsolutePath() + "', thread was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (ValidationException e) {
            System.out.println("Unable to process JUnit XML file '" + junitTestReport.getAbsolutePath() + "', XSD validation was not successful: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (RuntimeException e) {
            System.out.println("Unable to process JUnit XML file '" + junitTestReport.getAbsolutePath() + "', XSD validation was not successful: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        if (testResults.isEmpty()) {
            System.out.println("No valid test results to push in JUnit XML file '" + junitTestReport.getAbsolutePath() + "'");
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        return testResults;
    }

    public void writeTestResults(List<TestResult> testResults, Settings settings, File targetPath) {
        if (targetPath == null || !targetPath.canWrite()) {
            String filePathInfo = (targetPath == null) ? "" : ": " + targetPath.getAbsolutePath();
            System.out.println("Can not write test results to file" + filePathInfo);
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        TestResultXmlWriter testResultXmlWriter = new TestResultXmlWriter(targetPath);
        try {
            testResultXmlWriter.add(testResults, settings);
        } catch (InterruptedException e) {
            System.out.println("Unable to process test results to file '" + targetPath.getAbsolutePath() + "', thread was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (XMLStreamException e) {
            System.out.println("Unable to process test results to file '" + targetPath.getAbsolutePath() + "', XML stream exception has occurred: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (IOException e) {
            System.out.println("Unable to process test results to file '" + targetPath.getAbsolutePath() + "': " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } finally {
            try {
                testResultXmlWriter.close();
            } catch (XMLStreamException e) {
                System.out.println("Can not close the XML file'" + targetPath.getAbsolutePath() + "'" + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
        }
    }
}
