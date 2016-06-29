/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hpe.application.automation.bamboo.tasks.TestResultHelper.getOutputFilePath;

/**
 * Created by ybobrik on 9/25/2015.
 */
public class TestResultHelperAlm {
    private static final String CAN_NOT_SAVE_RUN_LOG_MESSAGE = "Alm.error.canNotSaveTheRunLog";
    private static final String RUN_LOG_FILE_NAME = "RunLog";
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
    private static int currentBuildNumber;

    protected static void AddALMArtifacts(final TaskContext taskContext, File resultFile, String linkSearchFilter, I18nBean i18nBean)
    {
        clearSavedALMRunLogPaths(taskContext);
        String taskName = taskContext.getConfigurationMap().get(CommonTaskConfigurationProperties.TASK_NAME);

        if(taskName.equals(i18nBean.getText(AlmLabManagementTaskConfigurator.TASK_NAME_VALUE))) {
            String taskRunLogPath = findRequiredStringFromLog(taskContext, linkSearchFilter);
            if (com.hpe.application.automation.tools.common.StringUtils.isNullOrEmpty(taskRunLogPath)) {
                taskContext.getBuildLogger().addErrorLogEntry(i18nBean.getText(CAN_NOT_SAVE_RUN_LOG_MESSAGE));
                return;
            }

            createResultFile(taskContext, taskRunLogPath, ".*processRunId=", i18nBean);
        }
        else if(taskName.equals(i18nBean.getText(RunFromAlmTaskConfigurator.TASK_NAME_VALUE))){
            List<String> links = null;
            if (resultFile != null && resultFile.exists()) {
                links = findRequiredStringsFromFile(taskContext.getBuildLogger(), resultFile);
            }
            if (links == null || links.size() < 1)
            {
                links = findRequiredStringsFromLog(taskContext.getBuildLogger(), linkSearchFilter);
            }
            Integer linksAmount = links.size();
            if (linksAmount.equals(0)) {
                taskContext.getBuildLogger().addErrorLogEntry(i18nBean.getText(CAN_NOT_SAVE_RUN_LOG_MESSAGE));
                return;
            }

            //taskContext.getBuildLogger().addBuildLogEntry("+++++++++++ " + links.size());
            for (String link : links) {
                createResultFile(taskContext, link, ".*EntityID=",i18nBean);
            }
        }
    }

    private static void clearSavedALMRunLogPaths(TaskContext taskContext)
    {
        int taskBuildNumber = taskContext.getBuildContext().getBuildNumber();

        if(savedALMRunLogPaths.size() > 0 && taskBuildNumber != currentBuildNumber)
        {
            savedALMRunLogPaths.clear();
        }
        currentBuildNumber=taskBuildNumber;
    }

    //is used for Run from Alm Lab Management task
    private static String findRequiredStringFromLog(TaskContext taskContext, String searchFilter)
    {
        BuildLogger logger = taskContext.getBuildLogger();
        List<LogEntry> buildLog = Lists.reverse(logger.getBuildLog());
        for(LogEntry logEntry: buildLog){
            String log = logEntry.getLog();
            if(log.contains(searchFilter)) {
                int pathBegin = log.indexOf("http");
                if(pathBegin > -1)
                {
                    log=log.substring(pathBegin);
                    if(!savedALMRunLogPaths.contains(log)){
                        return log;
                    }
                }
            }
        }
        return null;
    }

    //is used for Run from Alm task
    private static List<String> findRequiredStringsFromLog(BuildLogger logger, String searchFilter)
    {
        List<LogEntry> buildLog = Lists.reverse(logger.getBuildLog());
        List<String> results = new ArrayList<String>();
        for(LogEntry logEntry: buildLog){
            String log = logEntry.getLog();
            if(log.contains(searchFilter)) {
                int pathBegin = log.indexOf("td:");
                if(pathBegin > -1)
                {
                    String result = log.substring(pathBegin);
                    if(!results.contains(result) && !savedALMRunLogPaths.contains(result)){
                        results.add(result);
                    }
                }
            }
        }
        return results;
    }

    private static List<String> findRequiredStringsFromFile(BuildLogger logger, File resultFile) {
        List<String> results = new ArrayList<String>();
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(resultFile.getAbsoluteFile()));
            try{
                String s;
                while((s = in.readLine()) != null){
                    sb.append(s);
                }
            }finally {
                in.close();
            }
            //report link example: td://Automation.AUTOMATION.mydph0271.hpswlabs.adapps.hp.com:8080/qcbin/TestLabModule-000000003649890581?EntityType=IRun&amp;EntityID=1195091
            String sp = "td://.+?;EntityID=[0-9]+";
            Pattern p = Pattern.compile(sp);
            Matcher m = p.matcher(sb.toString());
            while(m.find()){
                results.add(m.group());
            }
        } catch (Exception e) {
            logger.addBuildLogEntry(e.getMessage());
        }
        return results;
    }

    private static void createResultFile(TaskContext taskContext, String link, String idFilter, I18nBean i18nBean){

        savedALMRunLogPaths.add(link);
        String RunReportFileId = link.replaceAll(idFilter, "");

        if(com.hpe.application.automation.tools.common.StringUtils.isNullOrEmpty(RunReportFileId))
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
            taskContext.getBuildLogger().addErrorLogEntry(i18nBean.getText(CAN_NOT_SAVE_RUN_LOG_MESSAGE));
        }
    }

}
