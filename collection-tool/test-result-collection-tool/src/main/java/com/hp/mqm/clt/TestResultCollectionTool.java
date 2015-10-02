package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultPushStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.FileEntity;

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

    public long getLastPushedTestResultId() {
        return lastPushedTestResultId;
    }

    public void collectAndPushTestResults() {
        List<File> publicApiXMLs = new LinkedList<File>();
        File tempOutputFile = null; // TODO rename
        if (settings.isInternal()) {
            for (String fileName : settings.getFileNames()) {
                publicApiXMLs.add(new File(fileName));
            }
        } else if (settings.getOutputFile() != null) {
            processSurefireReports(new File(settings.getOutputFile()));
            System.out.println("JUnit report(s) were saved to the output file");
            System.exit(ReturnCode.SUCCESS.getReturnCode());
        } else {
            try {
                tempOutputFile = File.createTempFile("testResult.xml", null);
                tempOutputFile.deleteOnExit();
            } catch (IOException e) {
                System.out.println("Can not create temp file for test result");
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
            processSurefireReports(tempOutputFile);
            publicApiXMLs.add(tempOutputFile);
        }

        client = new RestClient(settings);
        try {
            for (File publicApiXML : publicApiXMLs) {
                lastPushedTestResultId = client.postTestResult(new FileEntity(publicApiXML));
                validatePublishResult();
            }
        } catch (IOException e) {
            releaseClient();
            System.out.println("Unable to push test result: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } finally {
            releaseClient();
        }

        System.out.println("Test result(s) were successfully pushed");
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
            System.out.println("Unable to verify publish result of the last push with ID: " + lastPushedTestResultId);
        }

        Set<String> allowedPublishResults = new HashSet<String>();
        allowedPublishResults.add("success");
        if (settings.isSkipErrors()) {
            allowedPublishResults.add("warning");
        }
        if (!allowedPublishResults.contains(publishResult)) {
            System.out.println("Unsuccessful test result push with status: " + publishResult);
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
    }

    private void processSurefireReports(File outputFile) {
        List<TestResult> testResults = new LinkedList<TestResult>();
        XmlProcessor xmlProcessor = new XmlProcessor();
        for (String fileName : settings.getFileNames()) {
            testResults.addAll(xmlProcessor.processSurefireTestReport(new File(fileName)));
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
