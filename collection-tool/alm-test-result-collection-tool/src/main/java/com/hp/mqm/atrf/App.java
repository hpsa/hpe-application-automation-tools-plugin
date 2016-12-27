package com.hp.mqm.atrf;

import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.alm.services.AlmQueryBuilder;
import com.hp.mqm.atrf.alm.services.AlmWrapperService;
import com.hp.mqm.atrf.core.configuration.ConfigurationUtilities;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import com.hp.mqm.atrf.core.configuration.ReturnCode;
import com.hp.mqm.atrf.core.rest.RestStatusException;
import com.hp.mqm.atrf.octane.core.OctaneTestResultOutput;
import com.hp.mqm.atrf.octane.entities.TestRunResultEntity;
import com.hp.mqm.atrf.octane.services.OctaneWrapperService;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by berkovir on 08/12/2016.
 */
public class App {
    static final Logger logger = LogManager.getLogger();
    private FetchConfiguration configuration;
    private AlmWrapperService almWrapper;
    private OctaneWrapperService octaneWrapper;

    private Map<String, String> alm2OctaneTestingToolMapper = new HashMap<>();
    private DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2016-03-22 11:34:23
    private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");//2016-03-22 11:34:23

    private final String OCTANE_RUN_PASSED_STATUS = "Passed";
    private final String OCTANE_RUN_FAILED_STATUS = "Failed";
    private final String OCTANE_RUN_SKIPPED_STATUS = "Skipped";
    private Set<String> OCTANE_RUN_VALID_STATUS = new HashSet<>(Arrays.asList(OCTANE_RUN_PASSED_STATUS, OCTANE_RUN_FAILED_STATUS));

    public App(FetchConfiguration configuration) {
        this.configuration = configuration;

        alm2OctaneTestingToolMapper.put("MANUAL", "Manual");
        alm2OctaneTestingToolMapper.put("LEANFT-TEST", "LeanFT");
        alm2OctaneTestingToolMapper.put("QUICKTEST_TEST", "UFT");
        alm2OctaneTestingToolMapper.put("BUSINESS-PROCESS", "BPT");
    }

    public void start() {

        //VALIDATE LOGIN TO ALM
        loginToAlm();

        //VALIDATE LOGIN TO OCTANE
        if (!isOutput()) {
            loginToOctane();
        }


        logger.info("***************************************************************************************************");
        //SEND/OUTPUT DATA
        List<OctaneTestResultOutput> resultOutputs = outputToOctane();


        logger.info("***************************************************************************************************");

        //GET PERSISTENCE STATUS
        getCreationStatus(resultOutputs);
    }

    private void getCreationStatus(List<OctaneTestResultOutput> resultOutputs) {
        logger.info("Get creation statuses");
        for (int i = 0; i < resultOutputs.size(); i++) {
            OctaneTestResultOutput current = resultOutputs.get(i);
            getCreationStatusInternal(configuration, i + 1, current);
        }
    }

    private void getCreationStatusInternal(FetchConfiguration configuration, int bulkId, OctaneTestResultOutput output) {

        int failsCount = 0;
        boolean finished = false;
        int sleepSize = Integer.parseInt(configuration.getSyncSleepBetweenPosts()) * 1000;
        while (!finished) {
            if (!output.getStatus().equals("success") && !output.getStatus().equals(OctaneTestResultOutput.FAILED_SEND_STATUS)) {
                try {
                    output = octaneWrapper.getTestResultStatus(output);
                } catch (Exception e) {
                    failsCount++;
                    if (failsCount > 3) {
                        logger.info(String.format("Bulk #%s : failed to get creation status ", bulkId));
                        break;
                    } else {
                        logger.info(String.format("Bulk #%s : failed to get creation status, trial %s", bulkId, failsCount));
                        sleep(sleepSize);
                        continue;
                    }
                }
            }

            logger.info(String.format("Bulk #%s : creation status is %s", bulkId, output.getStatus().toUpperCase()));
            if (!(output.getStatus().equals("running") || output.getStatus().equals("queued"))) {
                finished = true;
            } else {
                sleep(sleepSize);
            }
        }
    }

    private boolean isOutput() {
        boolean isOutput = StringUtils.isNotEmpty(configuration.getOutputFile());
        return isOutput;
    }

    private List<OctaneTestResultOutput> outputToOctane() {
        if (!isOutput()) {
            logger.info("Starting send of data to Octane");
        }

        int bulkSize = Integer.parseInt(configuration.getSyncBulkSize());
        int fetchLimit = Integer.parseInt(configuration.getRunFilterFetchLimit());

        int pageSize = Math.min(bulkSize, fetchLimit);
        AlmQueryBuilder queryBuilder = almWrapper.buildRunFilter(configuration);
        queryBuilder.addPageSize(pageSize);


        //PRINT EXPECTED RUN COUNT
        int expectedRunsCount = 0;
        try {
            expectedRunsCount = almWrapper.getExpectedRuns(queryBuilder);
        } catch (RestStatusException e) {
            logger.error(String.format("Failed to execute Rest query in ALM. Validate Run Filter section in configuration. The received exception from ALM is %s", e.getMessage()));
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }


        expectedRunsCount = Math.min(expectedRunsCount, fetchLimit);
        logger.info(String.format("Expected runs : %d", expectedRunsCount));
        int expectedBulks = expectedRunsCount / bulkSize;
        if (expectedRunsCount % bulkSize > 0) {
            expectedBulks++;
        }
        logger.info(String.format("Expected bulks : %d", expectedBulks));

        //LOOP OF SEND
        long lastSentTime = 0;
        int runStartIndex = 0;
        int sleepBetweenPosts = Integer.parseInt(configuration.getSyncSleepBetweenPosts()) * 1000;


        long start = System.currentTimeMillis();
        List<OctaneTestResultOutput> resultOutputs = new ArrayList<>();
        for (int bulkId = 1; bulkId <= expectedBulks; bulkId++) {

            logger.info(String.format("Bulk #%s : preparing", bulkId));

            //4.1 GET DATA FROM ALM
            queryBuilder.addStartIndex(runStartIndex + 1);
            List<Run> runs = almWrapper.fetchRuns(queryBuilder);
            almWrapper.fetchRunRelatedEntities(runs);
            runStartIndex += runs.size();


            //4.2 SLEEP IF REQUIRED
            long fromLastSent = System.currentTimeMillis() - lastSentTime;
            long toSleep = sleepBetweenPosts - fromLastSent;
            if (toSleep > 0) {
                sleep(toSleep);
            }

            //4.3SEND/OUTPUT
            List<TestRunResultEntity> ngaRuns = prepareRunsForInjection(bulkId, runs);
            if (isOutput()) {
                File file = saveResults(configuration, ngaRuns);
                String note = "";
                if (runStartIndex < expectedRunsCount) {
                    note = String.format("(first %s runs)", bulkSize);
                }

                logger.info(String.format("The results are saved to  %s: %s", note, file.getAbsolutePath()));
                System.exit(0);
            } else {

                String firstRunId = ngaRuns.get(0).getRunId();
                String lastRunId = ngaRuns.get(ngaRuns.size() - 1).getRunId();
                OctaneTestResultOutput currentOutput = null;
                try {
                    currentOutput = sendResults(bulkId, ngaRuns);
                    lastSentTime = System.currentTimeMillis();
                    ConfigurationUtilities.saveLastSentRunId(lastRunId);
                    logger.info(String.format("Bulk #%s : sending %s runs , run ids from %s to %s , job id=%s, %s",
                            bulkId, ngaRuns.size(), firstRunId, lastRunId, currentOutput.getId(), currentOutput.getStatus().toUpperCase()));

                } catch (Exception e) {
                    String msg = e.getMessage();
                    int msgLength = 350;
                    if (msg.length() > msgLength) {
                        msg = msg.substring(0, msgLength);
                    }
                    logger.info(String.format("Bulk #%s : failed to send run ids from %s to %s: %s", bulkId, firstRunId, lastRunId, msg));

                    currentOutput = new OctaneTestResultOutput();
                    currentOutput.put(OctaneTestResultOutput.FIELD_STATUS, OctaneTestResultOutput.FAILED_SEND_STATUS);
                }
                resultOutputs.add(currentOutput);
            }
        }

        long end = System.currentTimeMillis();
        logger.info(String.format("Finish send of data to Octane in %d sec ", (end - start) / 1000));
        return resultOutputs;
    }

    private File saveResults(FetchConfiguration configuration, List<TestRunResultEntity> runResults) {

        List<TestRunResultEntity> myRunResults = runResults;
        File file = new File(configuration.getOutputFile());
        StreamResult result = new StreamResult(file);
        convertToXml(myRunResults, result, true);
        return file;
    }

    private OctaneTestResultOutput sendResults(int bulkId, List<TestRunResultEntity> runResults) {

        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        convertToXml(runResults, result, false);
        String xmlData = writer.toString();

        int trial = 0;
        OctaneTestResultOutput output = null;
        boolean finished = false;
        while (!finished) {
            try {
                trial++;
                output = octaneWrapper.postTestResults(xmlData);
                finished = true;
            } catch (Exception ex) {
                if (trial == 3) {
                    throw ex;
                }
                logger.warn(String.format("Bulk #%s : failed to send, trial %s", bulkId, trial));
                //sleep before next send
                sleep(5000);

            }
        }

        return output;

    }

    private void sleep(long sleepSize) {
        try {
            Thread.sleep(sleepSize);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loginToAlm() {
        try {
            logger.info("ALM : Validating login configuration ...");
            almWrapper = new AlmWrapperService(configuration.getAlmServerUrl(), configuration.getAlmDomain(), configuration.getAlmProject());
            boolean loggedIn = false;
            try {
                loggedIn = almWrapper.login(configuration.getAlmUser(), configuration.getAlmPassword());
            } catch (RestStatusException e) {
                //validate credentials
                if (e.getResponse().getStatusCode() == 401) {
                    String msg = String.format("Failed to login as '%s', validate credentials.", configuration.getAlmUser());
                    throw new RuntimeException(msg);
                } else {
                    throw e;
                }
            } catch (RuntimeException e) {
                //validate host
                if (e.getCause() instanceof UnknownHostException) {
                    String msg = "Failed to connect to host : " + configuration.getAlmServerUrl();
                    throw new RuntimeException(msg);
                } else {
                    throw e;
                }
            }

            if (loggedIn) {
                logger.info("ALM : Login successful");

                if (almWrapper.validateConnectionToDomain()) {
                    logger.info("ALM : Connected to ALM domain successfully");
                } else {
                    throw new RuntimeException("Failed to connect to ALM domain " + configuration.getAlmDomain());
                }

                if (almWrapper.validateConnectionToProject()) {
                    logger.info("ALM : Connected to ALM project successfully");
                } else {
                    throw new RuntimeException("Failed to connect to ALM project " + configuration.getAlmProject());
                }
            } else {
                throw new RuntimeException("Failed to login");
            }
        } catch (Exception e) {
            logger.error("ALM : " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
    }

    private void loginToOctane() {
        try {
            logger.info("Octane : Validating login configuration ...");
            long sharedSpaceId = Long.parseLong(configuration.getOctaneSharedSpaceId());
            long workspaceId = Long.parseLong(configuration.getOctaneWorkspaceId());

            octaneWrapper = new OctaneWrapperService(configuration.getOctaneServerUrl(), sharedSpaceId, workspaceId);
            if (octaneWrapper.login(configuration.getOctaneUser(), configuration.getOctanePassword())) {

                logger.info("Octane : Login successful");
                if (octaneWrapper.validateConnectionToSharedspace()) {
                    logger.info("Octane : Connected to Octane sharedspace successfully");
                } else {
                    throw new RuntimeException("Failed to connect to Octane sharedspace " + sharedSpaceId);
                }
                if (octaneWrapper.validateConnectionToWorkspace()) {
                    logger.info("Octane : Connected to Octane workspace successfully");
                } else {
                    throw new RuntimeException("Failed to connect to Octane workspace " + workspaceId);
                }
            } else {
                throw new RuntimeException("Failed to login");
            }
        } catch (Exception e) {
            logger.error("Octane : " + e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
    }

    private List<TestRunResultEntity> prepareRunsForInjection(int bulkId, List<Run> runs) {
        List<TestRunResultEntity> list = new ArrayList<>();

        List<String> skippedRunIds = new ArrayList<>();

        for (Run run : runs) {

            //preparation
            Test test = almWrapper.getTest(run.getTestId());
            TestFolder testFolder = almWrapper.getTestFolder(test.getTestFolderId());
            TestSet testSet = almWrapper.getTestSet(run.getTestSetId());
            TestConfiguration testConfiguration = almWrapper.getTestConfiguration(run.getTestConfigId());

            if (testSet == null) {
                //testSet was deleted
                skippedRunIds.add(run.getId());
                continue;
            }

            TestRunResultEntity injectionEntity = new TestRunResultEntity();
            injectionEntity.setRunId(run.getId());

            //TEST FIELDS
            //test name + test configuration, if Test name =Test configuration, just keep test name
            String testName = String.format("AlmTestId #%s : %s", test.getId(), sanitizeForXml(test.getName()));
            if (!testConfiguration.getName().equals(test.getName())) {
                testName = String.format("AlmTestId #%s, ConfId #%s : %s - %s", test.getId(), testConfiguration.getId(), sanitizeForXml(test.getName()), sanitizeForXml(testConfiguration.getName()));
            }
            injectionEntity.setTestName(restrictTo255(testName));

            injectionEntity.setTestingToolType(alm2OctaneTestingToolMapper.get(test.getSubType()));
            injectionEntity.setPackageValue(almWrapper.getProject());
            injectionEntity.setModule(almWrapper.getDomain());
            injectionEntity.setClassValue(restrictTo255(sanitizeForXml(testFolder.getName())));


            //RUN FIELDS
            injectionEntity.setDuration(run.getDuration());
            injectionEntity.setRunName(restrictTo255(String.format("AlmTestSet #%s : %s", testSet.getId(), sanitizeForXml(testSet.getName()))));
            injectionEntity.setExternalReportUrl(almWrapper.generateALMReferenceURL(run));


            Date startedDate = null;
            try {
                startedDate = DATE_TIME_FORMAT.parse(run.getExecutionDate() + " " + run.getExecutionTime());
            } catch (ParseException e) {
                try {
                    startedDate = DATE_FORMAT.parse(run.getExecutionDate());
                } catch (ParseException e1) {
                    throw new RuntimeException(String.format("Failed to convert run execution date '%s' to Java Date : %s", run.getExecutionDate(), e1.getMessage()));
                }
            }
            injectionEntity.setStartedTime(Long.toString(startedDate.getTime()));

            String status = OCTANE_RUN_VALID_STATUS.contains(run.getStatus()) ? run.getStatus() : OCTANE_RUN_SKIPPED_STATUS;
            injectionEntity.setStatus(status);

            injectionEntity.validateEntity();
            list.add(injectionEntity);
        }

        if (!skippedRunIds.isEmpty()) {
            List subList = skippedRunIds;
            int showCount = 20;
            String firstNMessage = "";
            if (skippedRunIds.size() > showCount) {
                subList = skippedRunIds.subList(0, showCount);
                firstNMessage = String.format(", first %s runs are", showCount);
            }

            logger.info(String.format("Bulk #%s : %s runs are skipped as their testsets are deleted %s : %s", bulkId, skippedRunIds.size(), firstNMessage, StringUtils.join(subList, ",")));
        }

        return list;
    }

    private String sanitizeForXml(String str) {
        if (hasInvalidCharacter(str)) {
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (!XMLChar.isInvalid(c)) {
                    strBuilder.append(c);
                } else {
                    strBuilder.append("_");
                }
            }

            return strBuilder.toString();
        }
        String newStr = null;
        try {
            newStr = new String(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            newStr = str;
        }
        return newStr;
    }

    private boolean hasInvalidCharacter(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (XMLChar.isInvalid(c)) {
                return true;
            }
        }
        return false;
    }

    private String restrictTo255(String value) {
        if (value == null || value.length() <= 255) {
            return value;
        }

        return value.substring(0, 255);
    }

    private void convertToXml(List<TestRunResultEntity> runResults, StreamResult result, boolean formatXml) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("test_result");
            doc.appendChild(rootElement);


            Element testRuns = doc.createElement("test_runs");
            rootElement.appendChild(testRuns);

            for (TestRunResultEntity runResult : runResults) {
                Element testRun = doc.createElement("test_run");
                testRuns.appendChild(testRun);

                testRun.setAttribute("module", runResult.getModule());
                testRun.setAttribute("package", runResult.getPackageValue());
                testRun.setAttribute("class", runResult.getClassValue());
                testRun.setAttribute("name", runResult.getTestName());

                testRun.setAttribute("duration", runResult.getDuration());
                testRun.setAttribute("status", runResult.getStatus());
                testRun.setAttribute("started", runResult.getStartedTime());
                testRun.setAttribute("external_report_url", runResult.getExternalReportUrl());
                testRun.setAttribute("run_name", runResult.getRunName());

                Element testFields = doc.createElement("test_fields");
                testRun.appendChild(testFields);

                if (StringUtils.isNotEmpty(runResult.getTestingToolType())) {
                    Element testField = doc.createElement("test_field");
                    testFields.appendChild(testField);
                    testField.setAttribute("type", "Testing_Tool_Type");
                    testField.setAttribute("value", runResult.getTestingToolType());
                }

                if (OCTANE_RUN_FAILED_STATUS.equals(runResult.getStatus())) {
                    Element error = doc.createElement("error");
                    testRun.appendChild(error);

                    error.setAttribute("type", "Error");
                    error.setAttribute("message", "For more details , goto ALM run : " + runResult.getExternalReportUrl());
                }
            }


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            if (formatXml) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            DOMSource source = new DOMSource(doc);

            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
