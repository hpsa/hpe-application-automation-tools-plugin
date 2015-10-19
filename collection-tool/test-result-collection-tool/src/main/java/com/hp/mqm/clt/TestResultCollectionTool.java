package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultPushStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.FileEntity;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TestResultCollectionTool {

    private Settings settings;
    private RestClient client;

    private long lastPushedTestResultId;

    public TestResultCollectionTool(Settings settings) {
        this.settings = settings;
    }

    // CODE REVIEW, Johnny, 19Oct2015 - unused method, remove
    public long getLastPushedTestResultId() {
        return lastPushedTestResultId;
    }

    public void collectAndPushTestResults() {
        List<File> publicApiXMLs = new LinkedList<File>();
        if (settings.isInternal()) {
            for (String fileName : settings.getFileNames()) {
                publicApiXMLs.add(new File(fileName));
            }
        } else if (settings.getOutputFile() != null) {
            processSurefireReports(new File(settings.getOutputFile()));
            System.out.println("JUnit report(s) were saved to the output file");
            System.exit(ReturnCode.SUCCESS.getReturnCode());
        } else {
            File publicApiTempXML = null;
            try {
                publicApiTempXML = File.createTempFile("testResult.xml", null);
                publicApiTempXML.deleteOnExit();
            } catch (IOException e) {
                System.out.println("Can not create temp file for test result");
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
            processSurefireReports(publicApiTempXML);
            publicApiXMLs.add(publicApiTempXML);
        }

        client = new RestClient(settings);
        try {
            for (File publicApiXML : publicApiXMLs) {
                try {
                    lastPushedTestResultId = client.postTestResult(new FileEntity(publicApiXML));
                } catch (ValidationException e) {
                    // CODE REVIEW, Johnny, 19Oct2015 - consider giving the full path to file as for example the temp
                    // file needed for non-internal reports will not be easy to found
                    System.out.println("Test result was not pushed - please check if the supplied file '" +
                            publicApiXML.getName() + "' is in a valid public API format (internal option was set)");
                    // CODE REVIEW, Johnny, 19Oct2015 - above - misleading message, publicApiXMLs also contains converted
                    // JUnit reports - so this validation exception is not thrown only in case when internal option is set
                    continue;
                }
                validatePublishResult();
            }
        } catch (IOException e) {
            releaseClient();
            System.out.println("Unable to push test result: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (RuntimeException e) {
            releaseClient();
            System.out.println("Unable to push test result: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } finally {
            releaseClient();
        }
    }

    private void releaseClient() {
        if (client != null) {
            try {
                client.release();
            } catch (IOException e) {
                System.out.println("Unable to release client session: " + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
        }
    }

    private void validatePublishResult() {
        String publishResult = null;
        try {
            publishResult = getPublishResult(lastPushedTestResultId);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        if (StringUtils.isEmpty(publishResult)) {
            // CODE REVIEW, Johnny, 19Oct2015 - id does not tell much, user must know which file was successfully pushed
            // and which failed; also applies to messages below
            System.out.println("Unable to verify publish result of the last push with ID: " + lastPushedTestResultId);
        }

        Set<String> allowedPublishResults = new HashSet<String>();
        allowedPublishResults.add("success");
        if (settings.isSkipErrors()) {
            allowedPublishResults.add("warning");
        }
        if (!allowedPublishResults.contains(publishResult)) {
            System.out.println("Test result with ID " + lastPushedTestResultId + " was not pushed - " +
                    "please check if all references (e.g. release id) are correct or try to set skip-errors option");
        } else {
            System.out.println("Test result with ID " + lastPushedTestResultId + " was successfully pushed");
        }
    }

    // CODE REVIEW, Johnny, 19Oct2015 - this merging of all JUnit reports in single file is potentially dangerous
    // talk to Rado or Vojta - consider the case when two or more reports contain runs of the same subset of tests
    // with different status; also consider that history table will not contain the full history if duplicates
    // are removed on server-side, user might rely on ordering on input files at cmd line arguments; this behavior
    // should be at least documented
    private void processSurefireReports(File outputFile) {
        List<TestResult> testResults = new LinkedList<TestResult>();
        XmlProcessor xmlProcessor = new XmlProcessor();
        for (String fileName : settings.getFileNames()) {
            testResults.addAll(xmlProcessor.processSurefireTestReport(new File(fileName), settings.getStarted()));
        }
        xmlProcessor.writeTestResults(testResults, settings, outputFile);
    }

    private String getPublishResult(long id) throws InterruptedException {
        String status = null;
        for (int i = 0; i < 100; i++) {
            TestResultPushStatus testResultPushStatus = client.getTestResultStatus(id);
            status = testResultPushStatus.getStatus();
            if (!"running".equals(status) && !"queued".equals(status)) {
                break;
            }
            Thread.sleep(100);
        }
        return status;
    }
}
