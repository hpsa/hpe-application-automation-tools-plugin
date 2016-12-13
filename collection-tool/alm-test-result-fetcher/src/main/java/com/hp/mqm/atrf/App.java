package com.hp.mqm.atrf;

import com.hp.mqm.atrf.alm.entities.*;
import com.hp.mqm.atrf.alm.services.AlmWrapperService;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import com.hp.mqm.atrf.octane.core.OctaneTestResultOutput;
import com.hp.mqm.atrf.octane.entities.TestRunResultEntity;
import com.hp.mqm.atrf.octane.services.OctaneWrapperService;
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

        loginToAlm();
        loginToOctane();

        almWrapper.fetchRunsAndRelatedEntities(configuration);
        List<TestRunResultEntity> ngaRuns = prepareRunsForInjection();

        if (StringUtils.isNotEmpty(configuration.getOutputFile())) {
            saveResults(configuration, ngaRuns);
        } else {
            List<OctaneTestResultOutput> outputs = sendResults(configuration, ngaRuns);
            getPersistanceStatus(configuration, outputs);
        }
    }

    private void getPersistanceStatus(FetchConfiguration configuration, List<OctaneTestResultOutput> outputs) {

        int sleepSize = Integer.parseInt(configuration.getSyncSleepBetweenPosts());
        logger.info("Sent results are : ");
        for (OctaneTestResultOutput output : outputs) {
            boolean finished = false;
            while (!finished) {
                if (!output.getStatus().equals("success")) {
                    try {
                        output = octaneWrapper.getTestResultStatus(output);
                    } catch (Exception e) {
                        logger.info(String.format("Sent id %s : %s", output.getId(), "Failed to get final result"));
                        finished = true;
                        break;
                    }

                }

                logger.info(String.format("Sent id %s : %s", output.getId(), output.getStatus()));

                if (!(output.getStatus().equals("running") || output.getStatus().equals("queued"))) {
                    finished = true;
                } else {
                    try {
                        int timeToWait = Math.max(10000/*10 sec*/, sleepSize * 1000 * 3);
                        Thread.sleep(timeToWait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void saveResults(FetchConfiguration configuration, List<TestRunResultEntity> runResults) {
        File file = new File(configuration.getOutputFile());
        StreamResult result = new StreamResult(file);
        convertToXml(runResults, result, true);
        logger.info("The results are saved to  : " + file.getAbsolutePath());
    }

    private List<OctaneTestResultOutput> sendResults(FetchConfiguration configuration, List<TestRunResultEntity> runResults) {
        int bulkSize = Integer.parseInt(configuration.getSyncBulkSize());
        int sleepSize = Integer.parseInt(configuration.getSyncSleepBetweenPosts());

        List<OctaneTestResultOutput> outputs = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < runResults.size(); i += bulkSize) {
            int subListStart = i;
            int subListEnd = Math.min(subListStart + bulkSize, runResults.size());
            List<TestRunResultEntity> subList = runResults.subList(subListStart, subListEnd);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            convertToXml(subList, result, false);
            String xmlData = writer.toString();
            OctaneTestResultOutput output = octaneWrapper.postTestResults(xmlData);
            outputs.add(output);
            //output.put("LIST", subList);
            logger.info(String.format("Sending bulk #%s of %s runs , run ids from %s to %s : %s, sent id=%s",
                    i / bulkSize + 1, subList.size(), subList.get(0).getRunId(), subList.get(subList.size() - 1).getRunId(), output.getStatus(), output.getId()));
            try {
                Thread.sleep(sleepSize * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        long endTime = System.currentTimeMillis();
        logger.info(String.format("Sent %s runs , total time %s ms", runResults.size(), endTime - startTime));

        return outputs;
    }

    private void loginToAlm() {
        logger.info("ALM : Validating login configuration ...");
        almWrapper = new AlmWrapperService(configuration.getAlmServerUrl(), configuration.getAlmDomain(), configuration.getAlmProject());
        if (almWrapper.login(configuration.getAlmUser(), configuration.getAlmPassword())) {

            logger.info("ALM : Login successful");
            if (almWrapper.validateConnectionToProject()) {
                logger.info("ALM : Connected to ALM project successfully");
            } else {
                throw new RuntimeException("ALM : Failed to connect to ALM Project.");
            }
        } else {
            throw new RuntimeException("ALM : Failed to login");
        }
    }

    private void loginToOctane() {
        logger.info("Octane : Validating login configuration ...");
        long sharedSpaceId = Long.parseLong(configuration.getOctaneSharedSpaceId());
        long workspaceId = Long.parseLong(configuration.getOctaneWorkspaceId());

        octaneWrapper = new OctaneWrapperService(configuration.getOctaneServerUrl(), sharedSpaceId, workspaceId);
        if (octaneWrapper.login(configuration.getOctaneUser(), configuration.getOctanePassword())) {

            logger.info("Octane : Login successful");
            if (octaneWrapper.validateConnectionToWorkspace()) {
                logger.info("Octane : Connected to Octane project successfully");
            } else {
                throw new RuntimeException("Octane : Failed to connect to Octane Workspace.");
            }
        } else {
            throw new RuntimeException("Octane : Failed to login");
        }
    }

    private List<TestRunResultEntity> prepareRunsForInjection() {
        List<TestRunResultEntity> list = new ArrayList<>();

        for (Run run : almWrapper.getRuns()) {

            //preparation
            Test test = almWrapper.getTest(run.getTestId());
            TestFolder testFolder = almWrapper.getTestFolder(test.getTestFolderId());
            TestSet testSet = almWrapper.getTestSet(run.getTestSetId());
            TestConfiguration testConfiguration = almWrapper.getTestConfiguration(run.getTestConfigId());


            TestRunResultEntity injectionEntity = new TestRunResultEntity();
            injectionEntity.setRunId(run.getId());

            //TEST FIELDS
            //test name + test configuration, if Test name =Test configuration, just keep test name
            String testName = String.format("AlmTestId #%s : %s", test.getId(), test.getName());
            if (!testConfiguration.getName().equals(test.getName())) {
                testName = String.format("AlmTestId %s, ConfId %s : %s - %s", test.getId(), testConfiguration.getId(), test.getName(), testConfiguration.getName());
            }
            testName = restrictTo255(testName);
            injectionEntity.setTestName(testName);

            injectionEntity.setTestingToolType(alm2OctaneTestingToolMapper.get(test.getSubType()));
            injectionEntity.setPackageValue(almWrapper.getProject());
            injectionEntity.setModule(almWrapper.getDomain());
            injectionEntity.setClassValue(testFolder.getName());


            //RUN FIELDS
            injectionEntity.setDuration(run.getDuration());
            injectionEntity.setRunName(restrictTo255(String.format("AlmTestSet %s : %s", testSet.getId(), testSet.getName())));
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

        return list;
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
                //ERROR????

                /*if (OCTANE_RUN_FAILED_STATUS.equals(runResult.getStatus())) {
                    Element error = doc.createElement("error");
                    testRun.appendChild(error);

                    testRun.setAttribute("type", "Error");
                    testRun.setAttribute("message", "For more details , goto ALM run : " + runResult.getExternalReportUrl());
                }*/

                Element testFields = doc.createElement("test_fields");
                testRun.appendChild(testFields);

                if (StringUtils.isNotEmpty(runResult.getTestingToolType())) {
                    Element testField = doc.createElement("test_field");
                    testFields.appendChild(testField);
                    testField.setAttribute("type", "Testing_Tool_Type");
                    testField.setAttribute("value", runResult.getTestingToolType());
                }

            }


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            if (formatXml) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }


            DOMSource source = new DOMSource(doc);

            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
