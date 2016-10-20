/*
 * MIT License
 *
 * Copyright (c) 2016 Hewlett-Packard Development Company, L.P.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hp.application.automation.tools.run;

import com.hp.application.automation.tools.common.PcException;
import com.hp.application.automation.tools.model.PcModel;
import com.hp.application.automation.tools.model.PostRunAction;
import com.hp.application.automation.tools.model.SecretContainer;
import com.hp.application.automation.tools.model.TimeslotDuration;
import com.hp.application.automation.tools.pc.*;
import com.hp.application.automation.tools.sse.result.model.junit.Error;
import com.hp.application.automation.tools.sse.result.model.junit.Failure;
import com.hp.application.automation.tools.sse.result.model.junit.*;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.HyperlinkNote;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.hp.application.automation.tools.pc.RunState.FINISHED;
import static com.hp.application.automation.tools.pc.RunState.RUN_FAILURE;

public class PcBuilder extends Builder implements SimpleBuildStep{
    
    private static final String artifactsDirectoryName = "archive";
    public static final String artifactsResourceName = "artifact";
    public static final String runReportStructure = "%s/%s/performanceTestsReports/pcRun%s";
    public static final String trendReportStructure = "%s/%s/performanceTestsReports/TrendReports";
    public static final String pcReportArchiveName = "Reports.zip";
    public static final String pcReportFileName = "Report.html";
    private static final String RUNID_BUILD_VARIABLE = "HP_RUN_ID";

    public static final String    TRENDED         = "Trended";
    public static final String    PENDING         = "Pending";
    public static final String    PUBLISHING      = "Publishing";
    public static final String    ERROR           = "Error";
    
    private final PcModel pcModel;


    private final String almPassword;
    private final String timeslotDurationHours;
    private final String timeslotDurationMinutes;
    private final boolean statusBySLA;
    private int runId;
    private FilePath pcReportFile;
    private String junitResultsFileName;
    private PrintStream logger;
  //  private boolean trendReportReady;
    
    @DataBoundConstructor
    public PcBuilder(
            String pcServerName,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String testId,
            String testInstanceId,
            String timeslotDurationHours,
            String timeslotDurationMinutes,
            PostRunAction postRunAction,
            boolean vudsMode,
            boolean statusBySLA,
            String description,
            boolean addRunToTrendReport,
            String trendReportId) {
        this.almUserName = almUserName;
        this.almPassword = almPassword;
        this.timeslotDurationHours = timeslotDurationHours;
        this.timeslotDurationMinutes = timeslotDurationMinutes;
        this.statusBySLA = statusBySLA;
        
        pcModel =
                new PcModel(
                        pcServerName.trim(),
                        almUserName.trim(),
                        almPassword,
                        almDomain.trim(),
                        almProject.trim(),
                        testId.trim(),
                        testInstanceId.trim(),
                        timeslotDurationHours.trim(),
                        timeslotDurationMinutes.trim(),
                        postRunAction,
                        vudsMode,
                        description,
                        addRunToTrendReport,
                        trendReportId);
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        
        return (DescriptorImpl) super.getDescriptor();
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        perform(build, build.getWorkspace(), launcher, listener);

        return true;
    }
    
    public PcModel getPcModel() {
        
        return pcModel;
    }
    
    public String getRunResultsFileName() {
        
        return junitResultsFileName;
    }
    
    public static String getArtifactsDirectoryName() {
        
        return artifactsDirectoryName;
    }
    
    public static String getArtifactsResourceName() {
        
        return artifactsResourceName;
    }
    
    public static String getRunReportStructure() {
        
        return runReportStructure;
    }
    
    public static String getPcReportArchiveName() {
        
        return pcReportArchiveName;
    }
    
    public static String getPcreportFileName() {
        
        return pcReportFileName;
    }

    private Testsuites execute(PcClient pcClient, Run<?, ?> build)
            throws InterruptedException,NullPointerException {
        try {
            if (!StringUtils.isBlank(pcModel.getDescription()))
                logger.println("- - -\nTest description: " + pcModel.getDescription());
            if (!beforeRun(pcClient))
                return null;

            return run(pcClient, build);

        } catch (InterruptedException e) {
            build.setResult(Result.ABORTED);
            pcClient.stopRun(runId);
            throw e;
        } catch (NullPointerException e) {
            logger.println("Error: Run could not start!");
        } catch (Exception e) {
            logger.println(e);
        } finally {
            pcClient.logout();
        }
        return null;
    }


    private Testsuites run(PcClient pcClient, Run<?, ?> build)
            throws InterruptedException, ClientProtocolException,
            IOException, PcException {
        
        PcRunResponse response = null;
        String errorMessage = "";
        String eventLogString = "";
        boolean trendReportReady = false;
        try {
            runId = pcClient.startRun();
            
            // This allows a user to access the runId from within Jenkins using a build variable.
            build.addAction(new ParametersAction(new StringParameterValue(RUNID_BUILD_VARIABLE, "" + runId))); 
            logger.print("Set " + RUNID_BUILD_VARIABLE + " Env Variable to " + runId + "\n");
            
            response = pcClient.waitForRunCompletion(runId);


            if (response != null && RunState.get(response.getRunState()) == FINISHED) {
                pcReportFile = pcClient.publishRunReport(runId, getReportDirectory(build));

                // Adding the trend report section
                if(pcModel.isAddRunToTrendReport() && pcModel.getTrendReportId() != null && RunState.get(response.getRunState()) != RUN_FAILURE){
                    pcClient.addRunToTrendReport(this.runId, pcModel.getTrendReportId());
                    pcClient.waitForRunToPublishOnTrendReport(this.runId, pcModel.getTrendReportId());
                    pcClient.downloadTrendReportAsPdf(pcModel.getTrendReportId(), getTrendReportsDirectory(build));
                    trendReportReady = true;
                }

            } else if (response != null && RunState.get(response.getRunState()).ordinal() > FINISHED.ordinal()) {
                PcRunEventLog eventLog = pcClient.getRunEventLog(runId);
                eventLogString = buildEventLogString(eventLog);
            }


        } catch (PcException e) {
            errorMessage = e.getMessage();
            logger.println("Error: " + errorMessage);
        }

        Testsuites ret = new Testsuites();
        parsePcRunResponse(ret,response, build, errorMessage, eventLogString);
        parsePcTrendResponse(ret,pcClient,trendReportReady,pcModel.getTrendReportId(),runId);
        return ret;
    }
    
    private String buildEventLogString(PcRunEventLog eventLog) {
        
        String logFormat = "%-5s | %-7s | %-19s | %s\n";
        StringBuilder eventLogStr = new StringBuilder("Event Log:\n\n" + String.format(logFormat, "ID", "TYPE", "TIME","DESCRIPTION"));
        for (PcRunEventLogRecord record : eventLog.getRecordsList()) {
            eventLogStr.append(String.format(logFormat, record.getID(), record.getType(), record.getTime(), record.getDescription()));            
        }
        return eventLogStr.toString();
    }

    private boolean beforeRun(PcClient pcClient) {
        return validatePcForm() && pcClient.login();
    }
    
    private String getReportDirectory(Run<?, ?> build) {
        return String.format(
                runReportStructure,
                build.getRootDir().getPath(),
                artifactsDirectoryName,
                runId);
    }

    private String getTrendReportsDirectory(Run<?, ?> build) {
        return String.format(
                trendReportStructure,
                build.getRootDir().getPath(),
                artifactsDirectoryName);
    }


    @Override
    @Deprecated
    public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return super.perform(build, launcher, listener);
    }

    private boolean validatePcForm() {
        
        logger.println("Validating parameters before run");
        String prefix = "doCheck";
        boolean ret = true;
        Method[] methods = getDescriptor().getClass().getMethods();
        Method[] modelMethods = pcModel.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith(prefix)) {
                name = name.replace(prefix, "").toLowerCase();
                for (Method modelMethod : modelMethods) {
                    String modelMethodName = modelMethod.getName();
                    if (modelMethodName.toLowerCase().equals("get" + name)) {
                        try {
                            Object obj =
                                    method.invoke(getDescriptor(), modelMethod.invoke(getPcModel()));
                            if (!obj.equals(FormValidation.ok())) {
                                logger.println(obj);
                                ret = false;
                            }
                            break;
                        } catch (Exception e) {
                            logger.println(e);
                        }
                    }
                }
            }
        }

        boolean isTrendReportIdValid = validateTrendReportIdIsNumeric(getPcModel().getTrendReportId(),
                getPcModel().isAddRunToTrendReport());

        ret &= isTrendReportIdValid;
        return ret;
        
    }

    private boolean validateTrendReportIdIsNumeric(String trendReportId, boolean addRunToTrendReport){

        FormValidation res = FormValidation.ok();
        if(addRunToTrendReport){
            if(trendReportId.isEmpty()){
                res = FormValidation.error("Parameter Is Missing: trend report ID is missing");
            }
            else{

                try{

                    Integer.parseInt(trendReportId);
                }
                catch(NumberFormatException e) {

                    res = FormValidation.error("Illegal Parameter: trend report ID is is not a number");
                }

            }
        }

        logger.println(res.toString().replace(": <div/>",""));

        return res.equals(FormValidation.ok());
    }
    
    private Testsuites parsePcRunResponse(Testsuites ret,
                                          PcRunResponse runResponse,
                                          Run<?, ?> build,
                                          String errorMessage, String eventLogString) throws IOException, InterruptedException {

        RunState runState = RunState.get(runResponse.getRunState());


        List<Testsuite> testSuites = ret.getTestsuite();
        Testsuite testSuite = new Testsuite();
        Testcase testCase = new Testcase();
        testCase.setClassname("Performance Tests.Test ID: " + runResponse.getTestID());
        testCase.setName("Run ID: " + runResponse.getID());
        testCase.setTime(String.valueOf(runResponse.getDuration() * 60));
        if (pcReportFile != null && pcReportFile.exists() && runState == FINISHED)
            testCase.getSystemOut().add(getOutputForReportLinks(build));
        updateTestStatus(testCase, runResponse, errorMessage, eventLogString);
        testSuite.getTestcase().add(testCase);
        testSuites.add(testSuite);
        return ret;
    }

    private Testsuites parsePcTrendResponse(Testsuites ret,PcClient pcClient,boolean trendReportReady,String TrendReportID, int runID) throws IOException, InterruptedException {


        // Create Trend Report
        if(trendReportReady){
            List<Testsuite> testSuites = ret.getTestsuite();
            Testsuite testSuite = new Testsuite();
            Testcase testCase = new Testcase();

            String reportUrlTemp = trendReportStructure.replaceFirst("%s/", "") + "/trendReport%s.pdf";
            String reportUrl = String.format(reportUrlTemp, artifactsResourceName, pcModel.getTrendReportId());

            testCase.setClassname("Trend report publishing .Trend Report ID: " + TrendReportID);
            testCase.setName("Run ID: " + runID);
            pcClient.publishTrendReport(reportUrl, pcModel.getTrendReportId());
            //testCase.setTime(String.valueOf(runResponse.getDuration() * 60));
            testCase.setStatus(JUnitTestCaseStatus.PASS);
            testSuite.getTestcase().add(testCase);
            testSuites.add(testSuite);
        }
        // End Create Trend Report

        return ret;
    }
    
    private void updateTestStatus(Testcase testCase, PcRunResponse response, String errorMessage, String eventLog) {
        RunState runState = RunState.get(response.getRunState());
        if (runState == RUN_FAILURE) {
            setError(testCase, String.format("%s. %s", runState, errorMessage), eventLog);
        } else if (statusBySLA && runState == FINISHED && !(response.getRunSLAStatus().equalsIgnoreCase("passed"))) {
            setFailure(testCase, "Run measurements did not reach SLA criteria. Run SLA Status: "
                                 + response.getRunSLAStatus(), eventLog);
        } else if (runState.hasFailure()) {          
            setFailure(testCase, String.format("%s. %s", runState, errorMessage), eventLog);
        } else if(errorMessage != null && !errorMessage.isEmpty()){
            setFailure(testCase, String.format("%s. %s", runState, errorMessage), eventLog);
        }
        else{
            testCase.setStatus(JUnitTestCaseStatus.PASS);
        }
    }
    
    private void setError(Testcase testCase, String message, String eventLog) {
        Error error = new Error();
        error.setMessage(message);
        if (!(eventLog == null || eventLog.isEmpty()))
            testCase.getSystemErr().add(eventLog);
        testCase.getError().add(error);
        testCase.setStatus(JUnitTestCaseStatus.ERROR);
        logger.println(String.format("%s %s", message ,eventLog));
    }
    
    private void setFailure(Testcase testCase, String message, String eventLog) {
        Failure failure = new Failure();
        failure.setMessage(message);
        if (!(eventLog == null || eventLog.isEmpty()))
            testCase.getSystemErr().add(eventLog);
        testCase.getFailure().add(failure);
        testCase.setStatus(JUnitTestCaseStatus.FAILURE);
        logger.println(String.format("Failure: %s %s", message ,eventLog));
    }
    
    private String getOutputForReportLinks(Run<?, ?> build) {
        String urlPattern = getArtifactsUrlPattern(build);
        String viewUrl = String.format(urlPattern, pcReportFileName);
        String downloadUrl = String.format(urlPattern, "*zip*/pcRun" + runId);
        logger.println(HyperlinkNote.encodeTo(viewUrl, "View analysis report of run " + runId));
        return String.format("View analysis report:\n%s\n\n\nDownload Report:\n%s", viewUrl, downloadUrl);

    }
    
    private String getArtifactsUrlPattern(Run<?, ?> build) {

        String runReportUrlTemp = runReportStructure.replaceFirst("%s/", "");
        return String.format(
                runReportUrlTemp,
                artifactsResourceName,
                runId + "/%s");
    }
    
    private void provideStepResultStatus(Result resultStatus, Run<?, ?> build) {
        String runIdStr =
                (runId > 0) ? String.format(" (PC RunID: %s)", String.valueOf(runId)) : "";
        logger.println(String.format(
                "Result Status%s: %s\n- - -",
                runIdStr,
                resultStatus.toString()));
        build.setResult(resultStatus);
        
    }
    
    private Result createRunResults(FilePath filePath, Testsuites testsuites) {
        Result ret = Result.SUCCESS;
        try {
            if (testsuites != null) {
                StringWriter writer = new StringWriter();
                JAXBContext context = JAXBContext.newInstance(Testsuites.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(testsuites, writer);
                filePath.write(writer.toString(), null);
                if (containsErrorsOrFailures(testsuites.getTestsuite())) {
                    ret = Result.UNSTABLE;
                }
            } else {
                logger.println("Empty Results");
                ret = Result.UNSTABLE;
            }
            
        } catch (Exception cause) {
            logger.print(String.format(
                    "Failed to create run results, Exception: %s",
                    cause.getMessage()));
            ret = Result.UNSTABLE;
        }
        return ret;
    }
    
    private boolean containsErrorsOrFailures(List<Testsuite> testsuites) {
        boolean ret = false;
        for (Testsuite testsuite : testsuites) {
            for (Testcase testcase : testsuite.getTestcase()) {
                String status = testcase.getStatus();
                if (status.equals(JUnitTestCaseStatus.ERROR)
                    || status.equals(JUnitTestCaseStatus.FAILURE)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    
    private String getJunitResultsFileName() {
        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String time = formatter.format(new Date());
        junitResultsFileName = String.format("Results%s.xml", time);
        return junitResultsFileName;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        Result resultStatus = Result.FAILURE;
        //trendReportReady = false;
        logger = listener.getLogger();
        PcClient pcClient = new PcClient(pcModel, logger);
        Testsuites testsuites = execute(pcClient, build);

//        // Create Trend Report
//        if(trendReportReady){
//            String reportUrlTemp = trendReportStructure.replaceFirst("%s/", "") + "/trendReport%s.pdf";
//            String reportUrl = String.format(reportUrlTemp, artifactsResourceName, pcModel.getTrendReportId());
//            pcClient.publishTrendReport(reportUrl, pcModel.getTrendReportId());
//        }
//        // End Create Trend Report

        FilePath resultsFilePath = workspace.child(getJunitResultsFileName());
        resultStatus = createRunResults(resultsFilePath, testsuites);
        provideStepResultStatus(resultStatus, build);

        if (!Result.SUCCESS.equals(resultStatus) && !Result.FAILURE.equals(resultStatus)) {
            return;
        }
        //Only do this if build worked (Not unstable or aborted - which might mean there is no report
        JUnitResultArchiver jUnitResultArchiver = new JUnitResultArchiver(this.getRunResultsFileName());
        jUnitResultArchiver.setKeepLongStdio(true);
        jUnitResultArchiver.perform(build, workspace, launcher, listener);

    }

    public String getPcServerName()
    {
        return getPcModel().getPcServerName();
    }

    public String getAlmProject()
    {
        return getPcModel().getAlmProject();
    }
    public String getTestId()
    {
        return getPcModel().getTestId();
    }
    public String getAlmDomain()
    {
        return getPcModel().getAlmDomain();
    }
    public String getTimeslotDurationHours()
    {
        return timeslotDurationHours;
    }
    public String getTimeslotDurationMinutes()
    {
        return timeslotDurationMinutes;
    }
    public PostRunAction getPostRunAction()
    {
        return getPcModel().getPostRunAction();
    }

    public String getTrendReportId()
    {
        return getPcModel().getTrendReportId();
    }

    public String getTestInstanceId()
    {
        return getPcModel().getTestInstanceId();
    }


    public boolean isAddRunToTrendReport()
    {
        return getPcModel().isAddRunToTrendReport();
    }

    public boolean isVudsMode()
    {
        return getPcModel().isVudsMode();
    }

    public String getDescription()
    {
        return getPcModel().getDescription();
    }
    public String getAlmUserName() {
        return almUserName;
    }

    private final String almUserName;

    public String getAlmPassword() {
        return almPassword;
    }

    public boolean isStatusBySLA() {
        return statusBySLA;
    }

    // This indicates to Jenkins that this is an implementation of an extension
    // point
    @Extension
    @Symbol("pcBuild")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        public DescriptorImpl() {
            
            load();
        }
        
        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            
            return true;
        }
        
        @Override
        public String getDisplayName() {
            
            return "Execute HP tests using HP Performance Center";
        }
        
        public FormValidation doCheckPcServerName(@QueryParameter String value) {
            
           return validateString(value, "PC Server");
        }
        
        public FormValidation doCheckAlmUserName(@QueryParameter String value) {
            
            return validateString(value, "User name");
        }
        
        public FormValidation doCheckAlmDomain(@QueryParameter String value) {
            
            return validateString(value, "Domain");
        }
        
        public FormValidation doCheckAlmProject(@QueryParameter String value) {
            
            return validateString(value, "Project");
        }
        
        public FormValidation doCheckTestId(@QueryParameter String value) {
            
            return validateHigherThanInt(value, "Test ID", 0, true);
        }
        
        public FormValidation doCheckTestInstanceId(@QueryParameter String value) {
            
            return validateHigherThanInt(value, "Test Instance ID", 0, true);
        }
        
        public FormValidation doCheckTimeslotDuration(@QueryParameter TimeslotDuration value) {
            
            return validateHigherThanInt(
                    String.valueOf(value.toMinutes()),
                    "Timeslot Duration (in minutes)",
                    30,
                    false);
        }
        
        public FormValidation doCheckTimeslotId(@QueryParameter String value) {
            
            return validateHigherThanInt(value, "Timeslot ID", 0, true);
        }


        /**
         * @param limitIncluded
         *            if true, value must be higher than limit. if false, value must be equal to or
         *            higher than limit.
         */
        private FormValidation validateHigherThanInt(
                String value,
                String field,
                int limit,
                boolean limitIncluded) {
            FormValidation ret = FormValidation.ok();
            value = value.trim();
            String messagePrefix = field + " must be ";
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error(messagePrefix + "set");
            } else {
                try {
                    if (limitIncluded && Integer.parseInt(value) <= limit)
                        ret = FormValidation.error(messagePrefix + "higher than " + limit);
                    else if (Integer.parseInt(value) < limit)
                        ret = FormValidation.error(messagePrefix + "at least " + limit);
                } catch (Exception e) {
                    ret = FormValidation.error(messagePrefix + "a whole number");
                }
            }
            
            return ret;
            
        }
        
        private FormValidation validateString(String value, String field) {
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value.trim())) {
                ret = FormValidation.error(field + " must be set");
            }
            
            return ret;
        }


        
        public List<PostRunAction> getPostRunActions() {
            
            return PcModel.getPostRunActions();
        }
        
    }
    
}
