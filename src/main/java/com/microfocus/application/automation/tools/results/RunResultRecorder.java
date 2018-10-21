/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.results;

import com.microfocus.application.automation.tools.common.RuntimeUtils;
import com.microfocus.application.automation.tools.lr.results.RunLrResultRecorder;
import com.microfocus.application.automation.tools.model.EnumDescription;
import com.microfocus.application.automation.tools.model.ResultsPublisherModel;
import com.microfocus.application.automation.tools.run.PcBuilder;
import com.microfocus.application.automation.tools.run.RunFromAlmBuilder;
import com.microfocus.application.automation.tools.run.RunFromFileBuilder;
import com.microfocus.application.automation.tools.run.SseBuilder;
import com.microfocus.application.automation.tools.uft.results.RunUftResultRecorder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.*;
import hudson.tasks.*;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultAggregator;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * using {@link JUnitResultArchiver};
 *
 * @author Thomas Maurel
 */
public class RunResultRecorder extends Recorder implements Serializable, MatrixAggregatable, SimpleBuildStep {
    private static final String ARCHIVING_TEST_REPORTS_FAILED_DUE_TO_XML_PARSING_ERROR =
            "Archiving test reports failed due to xml parsing error: ";
    private static final long serialVersionUID = 1L;
    private final ResultsPublisherModel _resultsPublisherModel;
    private final RunLrResultRecorder runLrResultRecorder;
    private final RunUftResultRecorder runUftResultRecorder;
    private List<FilePath> runReportList;

    /**
     * Instantiates a new Run result recorder.
     *
     * @param archiveTestResultsMode the archive test results mode
     */
    @DataBoundConstructor
    public RunResultRecorder(String archiveTestResultsMode) {
        _resultsPublisherModel = new ResultsPublisherModel(archiveTestResultsMode);
        runLrResultRecorder = new RunLrResultRecorder(_resultsPublisherModel);
        runUftResultRecorder = new RunUftResultRecorder(_resultsPublisherModel);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Pipeline perform.
     *
     * @param build              the build
     * @param workspace          the workspace
     * @param launcher           the launcher
     * @param listener           the listener
     * @param builderResultNames the builder result names
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    // temporary solution to deal with lack of support in builder list when Project is replaced by job type in pipeline.
    // Should be dealt with general refactoring - making this a job property or change folder structure to scan instead
    // of passing file name from builder.
    @SuppressWarnings("squid:S1160")
    public void pipelinePerform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                                @Nonnull TaskListener listener, @Nonnull Map<String, String> builderResultNames)
            throws IOException, InterruptedException {
        runReportList = new ArrayList<>();
        runLrResultRecorder.setRunReportList(runReportList);
        final List<String> fileSystemResultNames = new ArrayList<>();
        fileSystemResultNames.add(builderResultNames.get(RunFromFileBuilder.class.getName()));

        final List<String> mergedResultNames = new ArrayList<>(builderResultNames.values());

        if (mergedResultNames.isEmpty()) {
            listener.getLogger().println("RunResultRecorder: no results xml File provided");
            return;
        }

        recordRunResults(build, workspace, launcher, listener, mergedResultNames, fileSystemResultNames);
    }

    /**
     * Records LR test results copied in JUnit format
     *
     * @param build
     * @param workspace
     * @param launcher
     * @param listener
     * @param mergedResultNames
     * @param fileSystemResultNames
     * @throws InterruptedException
     * @throws IOException
     */
    private void recordRunResults(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                                  @Nonnull TaskListener listener, List<String> mergedResultNames,
                                  List<String> fileSystemResultNames) throws InterruptedException, IOException {

        for (String resultFile : mergedResultNames) {
            JUnitResultArchiver jUnitResultArchiver = new JUnitResultArchiver(resultFile);
            jUnitResultArchiver.setKeepLongStdio(true);
            jUnitResultArchiver.setAllowEmptyResults(true);
            jUnitResultArchiver.perform(build, workspace, launcher, listener);
        }

        final TestResultAction tempAction = build.getAction(TestResultAction.class);

        if (tempAction == null || tempAction.getResult() == null) {
            listener.getLogger().println("RunResultRecorder: didn't find any test results to record");
            return;
        }

        TestResult result = tempAction.getResult();

        try {
            archiveTestsReport(build, listener, fileSystemResultNames, result, workspace);
        } catch (ParserConfigurationException | SAXException e) {
            listener.error(ARCHIVING_TEST_REPORTS_FAILED_DUE_TO_XML_PARSING_ERROR + e);
        }

        runLrResultRecorder.setActionsAndPublishReports(build, listener);
    }

    /**
     * copies, archives and creates the Test reports of LR and UFT runs.
     *
     * @param build
     * @param listener
     * @param resultFiles
     * @param testResult
     * @param runWorkspace
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings({"squid:S134", "squid:S135"})
    private void archiveTestsReport(
            Run<?, ?> build,
            TaskListener listener,
            List<String> resultFiles,
            TestResult testResult, FilePath runWorkspace) throws ParserConfigurationException, SAXException,
            IOException, InterruptedException {

        if ((resultFiles == null) || (resultFiles.isEmpty())) {
            return;
        }

        ArrayList<String> zipFileNames = new ArrayList<>();
        ArrayList<FilePath> reportFolders = new ArrayList<>();
        List<String> reportNames = new ArrayList<>();

        listener.getLogger().println(
                "Report archiving mode is set to: "
                        + _resultsPublisherModel.getArchiveTestResultsMode());

        // if user specified not to archive report
        if (_resultsPublisherModel.getArchiveTestResultsMode().equals(ResultsPublisherModel.dontArchiveResults.getValue()))
            return;

        FilePath projectWS = runWorkspace;

        // get the artifacts directory where we will upload the zipped report
        // folder
        File artifactsDir = new File(build.getRootDir(), "archive");
        artifactsDir.mkdirs();

        // read each result.xml
        /*
         * The structure of the result file is: <testsuites> <testsuite>
         * <testcase.........report="path-to-report"/>
         * <testcase.........report="path-to-report"/>
         * <testcase.........report="path-to-report"/>
         * <testcase.........report="path-to-report"/> </testsuite>
         * </testsuites>
         */

        // add previous report names for aggregation when using pipelines.
        PerformanceJobReportAction performanceJobReportAction = build.getAction(PerformanceJobReportAction.class);
        if (performanceJobReportAction != null) {
            reportNames.addAll(performanceJobReportAction.getLrResultBuildDataset().getLrScenarioResults().keySet());
        }

        for (String resultsFilePath : resultFiles) {
            FilePath resultsFile = projectWS.child(resultsFilePath);

            List<ReportMetaData> ReportInfoToCollect = new ArrayList<>();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(resultsFile.read());
            doc.getDocumentElement().normalize();

            Node testSuiteNode = doc.getElementsByTagName("testsuite").item(0);
            Element testSuiteElement = (Element) testSuiteNode;
            if (testSuiteElement.hasAttribute("name") &&
                    testSuiteElement.getAttribute("name").endsWith(".lrs")) {
                if (runLrResultRecorder.archiveLrReport(build, listener, testResult, zipFileNames, reportFolders, reportNames, projectWS, artifactsDir, doc))
                    return;
            } else {
                runUftResultRecorder.archiveUftReport(build, listener, runWorkspace, zipFileNames, reportFolders, projectWS, artifactsDir, ReportInfoToCollect, (Element) testSuiteNode);
            }
        }
    }


    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        final List<String> mergedResultNames = new ArrayList<>();

        Project<?, ?> project = RuntimeUtils.cast(build.getParent());
        List<Builder> builders = project.getBuilders();
        runReportList = new ArrayList<>();
        runLrResultRecorder.setRunReportList(runReportList);
        final List<String> almResultNames = new ArrayList<>();
        final List<String> fileSystemResultNames = new ArrayList<>();
        final List<String> almSSEResultNames = new ArrayList<>();
        final List<String> pcResultNames = new ArrayList<>();

        // Get the TestSet report files names of the current build
        for (Builder builder : builders) {
            if (builder instanceof RunFromAlmBuilder) {
                almResultNames.add(((RunFromAlmBuilder) builder).getRunResultsFileName());
            } else if (builder instanceof RunFromFileBuilder) {
                fileSystemResultNames.add(((RunFromFileBuilder) builder).getRunResultsFileName());
            } else if (builder instanceof SseBuilder) {
                String resultsFileName = ((SseBuilder) builder).getRunResultsFileName();
                if (resultsFileName != null) {
                    almSSEResultNames.add(resultsFileName);
                }
            } else if (builder instanceof PcBuilder) {
                String resultsFileName = ((PcBuilder) builder).getRunResultsFileName();
                if (resultsFileName != null) {
                    pcResultNames.add(resultsFileName);
                }
            }
        }

        mergedResultNames.addAll(almResultNames);
        mergedResultNames.addAll(fileSystemResultNames);
        mergedResultNames.addAll(almSSEResultNames);
        mergedResultNames.addAll(pcResultNames);

        // Has any QualityCenter builder been set up?
        if (mergedResultNames.isEmpty()) {
            listener.getLogger().println("RunResultRecorder: no results xml File provided");
            return;
        }

        recordRunResults(build, workspace, launcher, listener, mergedResultNames, fileSystemResultNames);
    }

    @Override
    public MatrixAggregator createAggregator(
            MatrixBuild build,
            Launcher launcher,
            BuildListener listener) {

        return new TestResultAggregator(build, launcher, listener);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * Gets results publisher model.
     *
     * @return the results publisher model
     */
    public ResultsPublisherModel getResultsPublisherModel() {
        return _resultsPublisherModel;
    }

    public String getArchiveTestResultsMode() {
        return _resultsPublisherModel.getArchiveTestResultsMode();
    }

    /**
     * The type Descriptor.
     */
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Publish Micro Focus tests result";
        }

        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Gets report archive modes.
         *
         * @return the report archive modes
         */
        public List<EnumDescription> getReportArchiveModes() {
            return ResultsPublisherModel.archiveModes;
        }
    }
}
