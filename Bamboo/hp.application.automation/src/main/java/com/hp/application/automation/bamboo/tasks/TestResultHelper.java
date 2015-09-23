package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.Lists;
import com.hp.application.automation.tools.common.result.ResultSerializer;
import com.hp.application.automation.tools.common.result.model.junit.Testcase;
import com.hp.application.automation.tools.common.result.model.junit.Testsuite;
import com.hp.application.automation.tools.common.result.model.junit.Testsuites;
import com.hp.application.automation.tools.common.sdk.DirectoryZipHelper;
import org.apache.commons.io.FileUtils;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by dsinelnikov on 7/31/2015.
 */
public final class TestResultHelper
{
    private static final String TEST_REPORT_FILE_PATTERNS = "*.xml";
    private static final String TEST_STATUS_PASSED = "pass";
    private static final String TEST_STATUS_FAIL = "fail";
    public static final String HP_UFT_PREFIX = "HP_UFT_Build_";
    public enum ResultTypeFilter {All, SUCCESSFUL, FAILED }
    private static final String RUN_LOG_FILE_NAME = "RunLog";
    private static final String CAN_NOT_SAVE_RUN_LOG_MESSAGE = ResourceManager.getText("Alm.error.canNotSaveTheRunLog");
    private static final String RUN_LOG_HTML_TEXT =
        "<!DOCTYPE html>\n" +
            "<html>\n" +
            "    <head>\n" +
            "        <title>Test</title>\n" +
            "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
            "        <script type=\"text/javascript\">\n" +
            "        function codeAddress() {\n" +
            "            window.location = ALM_RUN_RESULTS_LINK_PARAMETER;\n" +
            "        }\n" +
            "        window.onload = codeAddress;\n" +
            "        </script>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "   \n" +
            "    </body>\n" +
            "</html>";
    private static final String ALM_RUN_RESULTS_LINK_PARAMETER = "ALM_RUN_RESULTS_LINK_PARAMETER";
    private static List<String> savedALMRunLogPaths = new ArrayList<String>();

    private TestResultHelper()
    {

    }

    public static void CollateResults(@NotNull final TestCollationService testCollationService,@NotNull final TaskContext taskContext)
    {
        taskContext.getBuildLogger().addBuildLogEntry("TEST_REPORT_FILE_PATTERNS is :"+TEST_REPORT_FILE_PATTERNS);
        taskContext.getBuildLogger().addBuildLogEntry("new XmlTestResultsReportCollector() is :"+new XmlTestResultsReportCollector());
        testCollationService.collateTestResults(taskContext, TEST_REPORT_FILE_PATTERNS, new XmlTestResultsReportCollector());
    }

    public static Collection<ResultInfoItem> getTestResults(@NotNull File results, ResultTypeFilter filter
            ,@NotNull final String resultArtifactNameFormat, @NotNull final TaskContext taskContext, @NotNull final BuildLogger logger)
    {
        Collection<ResultInfoItem> resultItems = new ArrayList<ResultInfoItem>();

        if(!results.exists())
        {
            logger.addBuildLogEntry("Test results file (" + results.getName() + ") was not found.");
            return resultItems;
        }

        try
        {
            Testsuites testsuites = ResultSerializer.Deserialize(results);

            Map<String, Integer> testNames = new HashMap<String, Integer>();

            for (Testsuite testsuite : testsuites.getTestsuite())
            {
                for (Testcase testcase : testsuite.getTestcase())
                {
                    if(isInFilter(testcase, filter))
                    {
                        String testName = getTestName(new File(testcase.getName()));
                        if(!testNames.containsKey(testName)){
                            testNames.put(testName, 0);
                        }
                        testNames.put(testName, testNames.get(testName) + 1);

                        StringBuilder fileNameSuffix = new StringBuilder();
                        Integer fileNameEntriesAmount = testNames.get(testName);
                        if(fileNameEntriesAmount>1){
                            fileNameSuffix.append("_").append(fileNameEntriesAmount);
                        }
                        File reportDir = new File(testcase.getReport());
                        File zipFileFolder = new File(getOutputFilePath(taskContext));
                        zipFileFolder.mkdirs();
                        File reportZipFile = new File(zipFileFolder, testName + fileNameSuffix + ".zip");
                        String resultArtifactName = String.format(resultArtifactNameFormat, testName);

                        ResultInfoItem resultItem = new ResultInfoItem(testName, reportDir, reportZipFile, resultArtifactName);
                        resultItems.add(resultItem);
                    }
                }
            }
        }
        catch (JAXBException ex)
        {
            logger.addBuildLogEntry("Test results file (" + results.getName() + ") has invalid format.");
        }

        return resultItems;
    }

    private static String getOutputFilePath(TaskContext taskContext)
    {
        StringBuilder fileName = new StringBuilder(taskContext.getWorkingDirectory().toString());
        String taskName = taskContext.getConfigurationMap().get(CommonTaskConfigurationProperties.TASK_NAME);
        fileName.append("\\").append(HP_UFT_PREFIX).append(taskContext.getBuildContext().getBuildNumber())
                .append("\\").append(String.format("%03d", taskContext.getId())).append("_" + taskName + "\\");
        return fileName.toString();
    }

    private static String getTestName(File test)
    {
        return test.getName();
    }

    private static boolean isInFilter(Testcase testcase, ResultTypeFilter filter)
    {
        if(filter == ResultTypeFilter.All)
        {
            return true;
        }

        String status = testcase.getStatus();

        if(filter == ResultTypeFilter.SUCCESSFUL && status.equals(TEST_STATUS_PASSED))
        {
            return true;
        }

        if(filter == ResultTypeFilter.FAILED && status.equals(TEST_STATUS_FAIL))
        {
            return true;
        }

        return false;
    }

    public static void zipResults(Collection<ResultInfoItem> resultInfoItems, BuildLogger logger)
    {
        for(ResultInfoItem resultItem : resultInfoItems)
        {
            try
            {
                DirectoryZipHelper.zipFolder(resultItem.getSourceDir().getPath(), resultItem.getZipFile().getPath());
            }
            catch (IOException ex){
                logger.addBuildLogEntry(ex.getMessage());
            } catch (Exception ex) {
                logger.addBuildLogEntry(ex.getMessage());
            }
        }
    }

    //is used for Run from Alm Lab Management task
    private static String findRequiredStringFromLog(TaskContext taskContext, String searchFilter)
    {
        String taskBuildNumber = new Integer(taskContext.getBuildContext().getBuildNumber()).toString();
        BuildLogger logger = taskContext.getBuildLogger();
        List<LogEntry> buildLog = Lists.reverse(logger.getBuildLog());
        for(LogEntry logEntry: buildLog){
            String log = logEntry.getLog();
            if(log.contains(searchFilter)) {
                int pathBegin = log.indexOf("http");
                if(pathBegin > -1)
                {
                    log=log.substring(pathBegin);
                    if(!savedALMRunLogPaths.contains(taskBuildNumber+log)){
                        return log;
                    }
                }
            }
        }
        return null;
    }

    //is used for Run from Alm task
    private static List<String> findRequiredStringsFromLog(TaskContext taskContext, String searchFilter)
    {
        String taskBuildNumber = new Integer(taskContext.getBuildContext().getBuildNumber()).toString();
        BuildLogger logger = taskContext.getBuildLogger();
        List<LogEntry> buildLog = Lists.reverse(logger.getBuildLog());
        List<String> results = new ArrayList<String>();
        for(LogEntry logEntry: buildLog){
            String log = logEntry.getLog();
            if(log.contains(searchFilter)) {
                int pathBegin = log.indexOf("td:");
                if(pathBegin > -1)
                {
                   String result = log.substring(pathBegin);
                    if(!results.contains(result) && !savedALMRunLogPaths.contains(taskBuildNumber+result)){
                        results.add(result);
                    }
                }
            }
        }
        return results;
    }

    private static void clearSavedALMRunLogPaths(TaskContext taskContext)
    {
        String taskBuildNumber = new Integer(taskContext.getBuildContext().getBuildNumber()).toString();

        if(savedALMRunLogPaths.size()>0&& !savedALMRunLogPaths.get(savedALMRunLogPaths.size()-1).startsWith(taskBuildNumber))
        {
            savedALMRunLogPaths.clear();
        }
    }

    protected static void AddALMArtifacts(final TaskContext taskContext, String linkSearchFilter)
    {
        clearSavedALMRunLogPaths(taskContext);
        String taskName = taskContext.getConfigurationMap().get(CommonTaskConfigurationProperties.TASK_NAME);

        if(taskName.equals(ResourceManager.getText(AlmLabManagementTaskConfigurator.TASK_NAME_VALUE))) {
            String taskRunLogPath = findRequiredStringFromLog(taskContext, linkSearchFilter);
            if (com.hp.application.automation.tools.common.StringUtils.isNullOrEmpty(taskRunLogPath)) {
                taskContext.getBuildLogger().addErrorLogEntry(CAN_NOT_SAVE_RUN_LOG_MESSAGE);
                return;
            }

            createResultFile(taskContext, taskRunLogPath, ".*processRunId=");
        }
        else if(taskName.equals(ResourceManager.getText(RunFromAlmTaskConfigurator.TASK_NAME_VALUE))){
            List<String> links = findRequiredStringsFromLog(taskContext, linkSearchFilter);
            Integer linksAmount = new Integer (links.size());
            if (linksAmount.equals(0)) {
                taskContext.getBuildLogger().addErrorLogEntry(CAN_NOT_SAVE_RUN_LOG_MESSAGE);
                return;
            }

            for (String link : links) {
                createResultFile(taskContext, link, ".*EntityID=");
            }
        }
    }
    private static void createResultFile(TaskContext taskContext, String link, String idFilter){
        Integer taskBuildNumber = taskContext.getBuildContext().getBuildNumber();

        savedALMRunLogPaths.add(taskBuildNumber + link);
        String RunReportFileId = link.replaceAll(idFilter, "");

        if(com.hp.application.automation.tools.common.StringUtils.isNullOrEmpty(RunReportFileId))
        {
            return;
        }
        String RunReportFileName = RUN_LOG_FILE_NAME+RunReportFileId+".html";
        String workingDirectory = getOutputFilePath(taskContext);
        File resultFile = new File(workingDirectory+"/"+RunReportFileName);
        link = "\""+link+"\"";
        String parameterizedResultsHtmlText = RUN_LOG_HTML_TEXT.replaceAll(ALM_RUN_RESULTS_LINK_PARAMETER, link);
        try {
            FileUtils.writeStringToFile(resultFile, parameterizedResultsHtmlText);
        }
        catch(Exception ex){
            taskContext.getBuildLogger().addErrorLogEntry(CAN_NOT_SAVE_RUN_LOG_MESSAGE);
        }
    }
}
