package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultPushStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.FileEntity;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestResultCollectionTool {

    private Settings settings;
    private RestClient client;

    public TestResultCollectionTool(Settings settings) {
        this.settings = settings;
    }

    public void collectAndPushTestResults() {
        Map<File, String> publicApiXMLs = new LinkedHashMap<File, String>();
        if (settings.isInternal()) {
            for (String fileName : settings.getInputXmlFileNames()) {
                publicApiXMLs.put(new File(fileName), fileName);
            }
        } else if (settings.getOutputFile() != null) {
            processJunitReport(new File(settings.getInputXmlFileNames().get(0)), new File(settings.getOutputFile()));
            System.out.println("JUnit report was saved to the output file");
            System.exit(ReturnCode.SUCCESS.getReturnCode());
        } else {
            for (String fileName : settings.getInputXmlFileNames()) {
                File publicApiTempXML = null;
                try {
                    publicApiTempXML = File.createTempFile("testResult.xml", null);
                    publicApiTempXML.deleteOnExit();
                } catch (IOException e) {
                    System.out.println("Can not create temp file for test result");
                    System.exit(ReturnCode.FAILURE.getReturnCode());
                }
                processJunitReport(new File(fileName), publicApiTempXML);
                publicApiXMLs.put(publicApiTempXML, fileName);
            }
        }

        client = new RestClient(settings);
        try {
            for (Map.Entry<File, String> publicApiXML : publicApiXMLs.entrySet()) {
                long testResultId;
                try {
                    testResultId = client.postTestResult(new FileEntity(publicApiXML.getKey()));
                } catch (ValidationException e) {
                    // One invalid public API XML should not stop the whole process when supplied externally
                    System.out.println("Test result from file '" + publicApiXML.getValue() + "' was not pushed"); // TODO message
                    System.out.println(e.getMessage());
                    continue;
                }
                if (settings.isCheckResult()) {
                    if (validatePublishResult(testResultId, publicApiXML.getValue())) {
                    }
                } else {
                    System.out.println("Test result from file '" + publicApiXML.getValue() + "' was pushed to the server with ID " + testResultId);
                }
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

    private boolean validatePublishResult(long testResultId, String fileName) {
        String publishResult = null;
        try {
            publishResult = getPublishResult(testResultId);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted: " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        if (StringUtils.isEmpty(publishResult)) {
            System.out.println("Unable to verify publish result of the last push from file '" + fileName + "' with ID: " + testResultId);
            return false;
        }

        Set<String> allowedPublishResults = new HashSet<String>();
        allowedPublishResults.add("success");
        if (settings.isSkipErrors()) {
            allowedPublishResults.add("warning");
        }
        if (!allowedPublishResults.contains(publishResult)) {
            System.out.println("Test result from file '" + fileName + "' with ID " + testResultId + " was not pushed - " +
                    "please check if all references (e.g. release id) are correct or try to set skip-errors option");
            return false;
        } else {
            System.out.println("Test result from file '" + fileName + "' with ID " + testResultId + " was successfully pushed");
            return true;
        }
    }

    private void processJunitReport(File junitReport, File outputFile) {
        XmlProcessor xmlProcessor = new XmlProcessor();
        List<TestResult> testResults = new LinkedList<TestResult>();
        testResults.addAll(xmlProcessor.processJunitTestReport(junitReport, settings.getStarted()));
        xmlProcessor.writeTestResults(testResults, settings, outputFile);
    }

    private String getPublishResult(long id) throws InterruptedException {
        String status = null;
        for (int i = 0; i < settings.getCheckResultTimeout() * 10; i++) {
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
