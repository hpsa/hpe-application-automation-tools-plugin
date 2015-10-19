package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.xml.SurefireXmlIterator;
import com.hp.mqm.clt.xml.TestResultXmlWriter;

import javax.xml.bind.ValidationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class XmlProcessor {

    // CODE REVIEW, Johnny, 19Oct2015 - consult with Mirek - I would limit ourselves to Surefire only, all messages to
    // user should be generic and not mention Surefire, just 'JUnit report XML file'.
    public List<TestResult> processSurefireTestReport(File surefireTestReport, Long started) {
        if (surefireTestReport == null || !surefireTestReport.canRead()) {
            String fileNameInfo = (surefireTestReport == null) ? "" : ": " + surefireTestReport.getName();
            // CODE REVIEW, Johnny, 19Oct2015 - consider writing out the full path to file, not just its name
            System.out.println("Can not read the Surefire XML file" + fileNameInfo);
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        List<TestResult> testResults = new LinkedList<TestResult>();
        try {
            SurefireXmlIterator iterator = new SurefireXmlIterator(surefireTestReport, started);
            while (iterator.hasNext()) {
                testResults.add(iterator.next());
            }
        } catch (IOException e) {
            // CODE REVIEW, Johnny, 19Oct2015 - check whether the e.getMessage() includes the path to the file,
            // user should know processing of which file failed; also applies to System.outs below
            System.out.println("Unable to process Surefire XML file: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (XMLStreamException e) {
            System.out.println("Unable to process Surefire XML file, XML stream exception has occurred: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (InterruptedException e) {
            System.out.println("Unable to process Surefire XML file, thread was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (ValidationException e) {
            System.out.println("Unable to process Surefire XML file, XSD validation was not successful: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (RuntimeException e) {
            System.out.println("Unable to process Surefire XML file, XSD validation was not successful: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }

        if (testResults.isEmpty()) {
            System.out.println("No valid test results to push");
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        return testResults;
    }

    public void writeTestResults(List<TestResult> testResults, Settings settings, File targetPath) {
        if (targetPath == null || !targetPath.canWrite()) {
            String fileNameInfo = (targetPath == null) ? "" : ": " + targetPath.getName();
            // CODE REVIEW, Johnny, 19Oct2015 - consider writing out the full path to file, not just its name
            System.out.println("Can not write test results to file" + fileNameInfo);
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        TestResultXmlWriter testResultXmlWriter = new TestResultXmlWriter(targetPath);
        try {
            testResultXmlWriter.add(testResults, settings);
        } catch (InterruptedException e) {
            // CODE REVIEW, Johnny, 19Oct2015 - check whether the e.getMessage() includes the path to the file,
            // user should know which file writing to failed, also applies to System.outs below
            System.out.println("Unable to process test results, thread was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (XMLStreamException e) {
            System.out.println("Unable to process test results, XML stream exception has occurred: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (IOException e) {
            System.out.println("Unable to process test results: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } finally {
            try {
                testResultXmlWriter.close();
            } catch (XMLStreamException e) {
                System.out.println("Can not close the XML file" + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
        }
    }
}
