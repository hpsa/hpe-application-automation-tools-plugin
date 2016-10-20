// (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of
// the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
// THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
// CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package com.hp.application.automation.tools.results;

import com.hp.application.automation.tools.common.RuntimeUtils;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.run.PcBuilder;
import com.hp.application.automation.tools.run.RunFromAlmBuilder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import com.hp.application.automation.tools.run.SseBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultAggregator;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * using {@link JUnitResultArchiver};
 *
 * @author Thomas Maurel
 */
public class RunResultRecorder extends Recorder implements Serializable, MatrixAggregatable, SimpleBuildStep {

    public static final String REPORT_NAME_FIELD = "report";
    private static final long serialVersionUID = 1L;
    private static final String PERFORMANCE_REPORT_FOLDER = "PerformanceReport";
    private static final String IE_REPORT_FOLDER = "IE";
    private static final String HTML_REPORT_FOLDER = "HTML";
    private static final String INDEX_HTML_NAME = "index.html";
    private static final String REPORT_INDEX_NAME = "report.index";
    private static final String REPORTMETADATE_XML = "report_metadata.xml";
    private static final String TRANSACTION_SUMMARY_FOLDER = "TransactionSummary";
    private static final String TRANSACTION_REPORT_NAME = "Report3";
    public static final int SECS_IN_DAY = 86400;
    public static final int SECS_IN_HOUR = 3600;
    public static final int SECS_IN_MINUTE = 60;
    private final ResultsPublisherModel _resultsPublisherModel;

    /**
     * Instantiates a new Run result recorder.
     *
     * @param archiveTestResultsMode the archive test results mode
     */
    @DataBoundConstructor
    public RunResultRecorder(String archiveTestResultsMode) {

        _resultsPublisherModel = new ResultsPublisherModel(archiveTestResultsMode);
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
        final List<String> mergedResultNames = new ArrayList<String>();

        final List<String> fileSystemResultNames = new ArrayList<String>();
        fileSystemResultNames.add(builderResultNames.get(RunFromFileBuilder.class.getName()));

        mergedResultNames.addAll(builderResultNames.values());

        if (mergedResultNames.isEmpty()) {
            listener.getLogger().println("RunResultRecorder: no results xml File provided");
            return;
        }

        recordRunResults(build, workspace, launcher, listener, mergedResultNames, fileSystemResultNames);
        return;
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
            jUnitResultArchiver.perform(build, workspace, launcher, listener);
        }

        final TestResultAction tempAction = build.getAction(TestResultAction.class);
        if (tempAction == null || tempAction.getResult() == null) {
            // Since recording returned null - there no result to this test run to preform on
            return;
        }

        TestResult result = tempAction.getResult();

        try {
            archiveTestsReport(build, listener, fileSystemResultNames, result, workspace);
        } catch (ParserConfigurationException | SAXException e) {
            listener.error("Archiving test reports failed due to xml parsing error: " + e);
        }
        publishLrReports(build);
    }

    /**
     * Adds the html reports actions to the left side menu.
     *
     * @param build
     */
    private void publishLrReports(@Nonnull Run<?, ?> build) throws IOException {
        File reportDirectory = new File(build.getRootDir(), PERFORMANCE_REPORT_FOLDER);
        if (reportDirectory.exists()) {
            File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
            if (htmlIndexFile.exists()) {
                build.replaceAction(new PerformanceReportAction(build));
            }
        }

        File summaryDirectory = new File(build.getRootDir(), TRANSACTION_SUMMARY_FOLDER);
        if (summaryDirectory.exists()) {
            File htmlIndexFile = new File(summaryDirectory, INDEX_HTML_NAME);
            if (htmlIndexFile.exists()) {
                build.replaceAction(new TransactionSummaryAction(build));
            }
        }
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

        ArrayList<String> zipFileNames = new ArrayList<String>();
        ArrayList<FilePath> reportFolders = new ArrayList<FilePath>();
        List<String> reportNames = new ArrayList<String>();

        listener.getLogger().println(
                "Report archiving mode is set to: "
                        + _resultsPublisherModel.getArchiveTestResultsMode());

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
        for (SuiteResult suiteResult : testResult.getSuites()) {
            String[] temp = suiteResult.getName().split("_");
            reportNames.add(temp[temp.length - 1]);
        }

        for (String resultsFilePath : resultFiles) {
            FilePath resultsFile = projectWS.child(resultsFilePath);

            List<ReportMetaData> ReportInfoToCollect = new ArrayList<ReportMetaData>();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(resultsFile.read());
            doc.getDocumentElement().normalize();

            Node testSuiteNode = doc.getElementsByTagName("testsuite").item(0);
            Element testSuiteElement = (Element) testSuiteNode;
            if (testSuiteElement.hasAttribute("name") &&
                    testSuiteElement.getAttribute("name").endsWith(".lrs")) { // LR test
                NodeList testSuiteNodes = doc.getElementsByTagName("testsuite");
                for (int i = 0; i < testSuiteNodes.getLength(); i++) {
                    testSuiteNode = testSuiteNodes.item(i);
                    testSuiteElement = (Element) testSuiteNode;
                    if (!testSuiteElement.hasAttribute("name")) {
                        continue;
                    }
                    String testFolderPath = testSuiteElement.getAttribute("name");
                    String testStatus = ("0".equals(testSuiteElement.getAttribute("failures"))) ? "pass" : "fail";

                    Node testCaseNode = testSuiteElement.getElementsByTagName("testcase").item(0);
                    if (testCaseNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element testCaseElement = (Element) testCaseNode;

                        if (!testCaseElement.hasAttribute(REPORT_NAME_FIELD)) {
                            continue;
                        }

                        String reportFolderPath = testCaseElement.getAttribute(REPORT_NAME_FIELD);
                        FilePath reportFolder = new FilePath(projectWS.getChannel(), reportFolderPath);
                        reportFolders.add(reportFolder);

                        FilePath testFolder = new FilePath(projectWS.getChannel(), testFolderPath);
                        String zipFileName = getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                        FilePath archivedFile = new FilePath(new FilePath(artifactsDir), zipFileName);

                        if (archiveFolder(reportFolder, testStatus, archivedFile, listener)) {
                            zipFileNames.add(zipFileName);
                        }

                        createHtmlReport(reportFolder, testFolderPath, artifactsDir, reportNames, testResult);
                        createTransactionSummary(reportFolder, testFolderPath, artifactsDir, reportNames, testResult);
                    }
                }
            } else { // UFT Test
                boolean reportIsHtml = false;
                NodeList testCasesNodes = ((Element) testSuiteNode).getElementsByTagName("testcase");
                for (int i = 0; i < testCasesNodes.getLength(); i++) {

                    Node nNode = testCasesNodes.item(i);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        if (!eElement.hasAttribute(REPORT_NAME_FIELD)) {
                            continue;
                        }

                        String reportFolderPath =
                                eElement.getAttribute(REPORT_NAME_FIELD); // e.g. "C:\UFTTest\GuiTest1\Report"
                        String testFolderPath = eElement.getAttribute("name"); // e.g. "C:\UFTTest\GuiTest1"
                        String testStatus = eElement.getAttribute("status"); // e.g. "pass"

                        Node nodeSystemInfo = eElement.getElementsByTagName("system-out").item(0);
                        String sysinfo = nodeSystemInfo.getFirstChild().getNodeValue();
                        String testDateTime = sysinfo.substring(0, 19);

                        FilePath reportFolder = new FilePath(projectWS.getChannel(), reportFolderPath);

                        reportFolders.add(reportFolder);

                        String archiveTestResultMode = _resultsPublisherModel.getArchiveTestResultsMode();
                        boolean archiveTestResult;

                        // check for the new html report
                        FilePath htmlReport = new FilePath(reportFolder, "run_results.html");
                        FilePath rrvReport = new FilePath(reportFolder, "Results.xml");
                        if (htmlReport.exists()) {
                            reportIsHtml = true;
                            String htmlReportDir = reportFolder.getRemote();

                            ReportMetaData reportMetaData = new ReportMetaData();
                            reportMetaData.setFolderPath(htmlReportDir);
                            reportMetaData.setIsHtmlReport(true);
                            reportMetaData.setDateTime(testDateTime);
                            reportMetaData.setStatus(testStatus);

                            File testFileFullName = new File(testFolderPath);
                            String testName = org.apache.commons.io.FilenameUtils.getName(testFileFullName.getPath());
                            String resourceUrl = "artifact/UFTReport/" + testName;
                            reportMetaData.setResourceURL(resourceUrl);
                            reportMetaData.setDisPlayName(testName); // use the name, not the full path
                            // don't know reportMetaData's URL path yet, we will generate it later.
                            ReportInfoToCollect.add(reportMetaData);

                            listener.getLogger()
                                    .println("add html report info to ReportInfoToCollect: " + "[date]" + testDateTime);
                        }

                        archiveTestResult = isArchiveTestResult(testStatus, archiveTestResultMode);

                        if (archiveTestResult && rrvReport.exists()) {

                            if (reportFolder.exists()) {

                                FilePath testFolder = new FilePath(projectWS.getChannel(), testFolderPath);

                                String zipFileName = getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                                zipFileNames.add(zipFileName);

                                listener.getLogger().println(
                                        "Zipping report folder: " + reportFolderPath);

                                ByteArrayOutputStream outstr = new ByteArrayOutputStream();

                                // don't use FileFilter for zip, or it will cause bug when files are on slave
                                reportFolder.zip(outstr);

                /*
                 * I did't use copyRecursiveTo or copyFrom due to
                 * bug in
                 * jekins:https://issues.jenkins-ci.org/browse
                 * /JENKINS-9189 //(which is cleaimed to have been
                 * fixed, but not. So I zip the folder to stream and
                 * copy it to the master.
                 */

                                ByteArrayInputStream instr = new ByteArrayInputStream(outstr.toByteArray());

                                FilePath archivedFile = new FilePath(new FilePath(artifactsDir), zipFileName);
                                archivedFile.copyFrom(instr);
                                listener.getLogger().println(
                                        "copy from slave to master: " + archivedFile);
                                outstr.close();
                                instr.close();

                                // add to Report list
                                ReportMetaData reportMetaData = new ReportMetaData();
                                reportMetaData.setIsHtmlReport(false);
                                // reportMetaData.setFolderPath(htmlReportDir); //no need for RRV
                                File testFileFullName = new File(testFolderPath);
                                String testName = testFileFullName.getName();
                                reportMetaData.setDisPlayName(testName); // use the name, not the full path
                                String zipFileUrlName = "artifact/" + zipFileName;
                                reportMetaData.setUrlName(
                                        zipFileUrlName); // for RRV, the file url and resource url are the same.
                                reportMetaData.setResourceURL(zipFileUrlName);
                                reportMetaData.setDateTime(testDateTime);
                                reportMetaData.setStatus(testStatus);
                                ReportInfoToCollect.add(reportMetaData);

                            } else {
                                listener.getLogger().println(
                                        "No report folder was found in: " + reportFolderPath);
                            }
                        }

                    }
                }

                if (reportIsHtml && !ReportInfoToCollect.isEmpty()) {

                    listener.getLogger().println("begin to collectAndPrepareHtmlReports");
                    collectAndPrepareHtmlReports(build, listener, ReportInfoToCollect, runWorkspace);
                }

                if (!ReportInfoToCollect.isEmpty()) {
                    // serialize report metadata
                    File reportMetaDataXmlFile = new File(artifactsDir.getParent(), REPORTMETADATE_XML);
                    String reportMetaDataXml = reportMetaDataXmlFile.getAbsolutePath();
                    writeReportMetaData2XML(ReportInfoToCollect, reportMetaDataXml, listener);

                    // Add UFT report action
                    try {
                        listener.getLogger().println("Adding a report action to the current build.");
                        HtmlBuildReportAction reportAction = new HtmlBuildReportAction(build);
                        build.addAction(reportAction);

                    } catch (Exception ex) {
                        listener.getLogger().println("a problem adding action: " + ex);
                    }
                }
            }
        }
    }

    private void writeReportMetaData2XML(List<ReportMetaData> htmlReportsInfo, String xmlFile, TaskListener _logger) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            _logger.error("Failed creating xml doc report: " + e);
            return;
        }
        Document doc = builder.newDocument();
        Element root = doc.createElement("reports_data");
        doc.appendChild(root);

        for (ReportMetaData htmlReportInfo : htmlReportsInfo) {
            String disPlayName = htmlReportInfo.getDisPlayName();
            String urlName = htmlReportInfo.getUrlName();
            String resourceURL = htmlReportInfo.getResourceURL();
            String dateTime = htmlReportInfo.getDateTime();
            String status = htmlReportInfo.getStatus();
            String isHtmlReport = htmlReportInfo.getIsHtmlReport() ? "true" : "false";
            Element elmReport = doc.createElement(REPORT_NAME_FIELD);
            elmReport.setAttribute("disPlayName", disPlayName);
            elmReport.setAttribute("urlName", urlName);
            elmReport.setAttribute("resourceURL", resourceURL);
            elmReport.setAttribute("dateTime", dateTime);
            elmReport.setAttribute("status", status);
            elmReport.setAttribute("isHtmlreport", isHtmlReport);
            root.appendChild(elmReport);

        }

        try {
            write2XML(doc, xmlFile);
        } catch (TransformerException e) {
            _logger.error("Failed transforming xml file: " + e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            _logger.error("Failed to find " + xmlFile + ": " + e);
        }
    }

    private void write2XML(Document document, String filename) throws TransformerException, FileNotFoundException {
        document.normalize();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(document);
        PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
        StreamResult result = new StreamResult(pw);
        transformer.transform(source, result);

    }

    private Boolean collectAndPrepareHtmlReports(Run build, TaskListener listener, List<ReportMetaData> htmlReportsInfo,
                                                 FilePath runWorkspace)
            throws IOException, InterruptedException {
        File reportDir = new File(new File(build.getRootDir(), "archive"), "UFTReport");

        FilePath rootTarget = new FilePath(reportDir);

        try {
            for (ReportMetaData htmlReportInfo : htmlReportsInfo) {

                // make sure it's a html report
                if (!htmlReportInfo.getIsHtmlReport()) {
                    continue;
                }
                String htmlReportDir = htmlReportInfo.getFolderPath(); // C:\UFTTest\GuiTest1\Report

                listener.getLogger().println("collectAndPrepareHtmlReports, collecting:" + htmlReportDir);
                listener.getLogger().println("workspace: " + runWorkspace);

                // copy to the subdirs of master
                FilePath source = new FilePath(runWorkspace, htmlReportDir);
                listener.getLogger().println("source: " + source);
                String testName = htmlReportInfo.getDisPlayName(); // like "GuiTest1"
                String dest = testName;
                FilePath targetPath = new FilePath(rootTarget, dest); // target path is something like "C:\Program Files
                // (x86)\Jenkins\jobs\testAction\builds\35\archive\UFTReport\GuiTest1"

                // zip copy and unzip
                ByteArrayOutputStream outstr = new ByteArrayOutputStream();

                // don't use FileFilter for zip, or it will cause bug when files are on slave
                source.zip(outstr);
                ByteArrayInputStream instr = new ByteArrayInputStream(outstr.toByteArray());

                String zipFileName = "UFT_Report_HTML_tmp.zip";
                FilePath archivedFile = new FilePath(rootTarget, zipFileName);

                archivedFile.copyFrom(instr);

                listener.getLogger().println("copy from slave to master: " + archivedFile);
                outstr.close();
                instr.close();

                // unzip
                archivedFile.unzip(rootTarget);
                archivedFile.delete();

                // now,all the files are in the C:\Program Files (x86)
                // \Jenkins\jobs\testAction\builds\35\archive\UFTReport\Report
                // we need to rename the above path to targetPath.
                // So at last we got files in C:\Program Files (x86)
                // \Jenkins\jobs\testAction\builds\35\archive\UFTReport\GuiTest

                String unzippedFileName = org.apache.commons.io.FilenameUtils.getName(htmlReportDir);
                FilePath unzippedFolderPath = new FilePath(rootTarget, unzippedFileName); // C:\Program Files
                // (x86)\Jenkins\jobs\testAction\builds\35\archive\UFTReport\Report
                // FilePath unzippedFolderPath = new FilePath(rootTarget, source.getName()); //C:\Program Files
                // (x86)\Jenkins\jobs\testAction\builds\35\archive\UFTReport\Report
                unzippedFolderPath.renameTo(targetPath);
                listener.getLogger()
                        .println("UnzippedFolderPath is: " + unzippedFolderPath + " targetPath is: " + targetPath);
                // end zip copy and unzip

                // fill in the urlName of this report. we need a network path not a FS path
                String resourceUrl = htmlReportInfo.getResourceURL();
                String urlName = resourceUrl + "/run_results.html"; // like artifact/UFTReport/GuiTest1/run_results.html

                listener.getLogger().println("set the report urlName to " + urlName);
                htmlReportInfo.setUrlName(urlName);

            }
        } catch (Exception ex) {
            listener.getLogger().println("catch exception in collectAndPrepareHtmlReports: " + ex);
        }

        return true;
    }

    private boolean archiveFolder(FilePath reportFolder,
                                  String testStatus,
                                  FilePath archivedFile,
                                  TaskListener listener) throws IOException, InterruptedException {
        String archiveTestResultMode = _resultsPublisherModel.getArchiveTestResultsMode();
        boolean archiveTestResult;

        archiveTestResult = isArchiveTestResult(testStatus, archiveTestResultMode);

        if (archiveTestResult) {

            if (reportFolder.exists()) {

                listener.getLogger().println(
                        "Zipping report folder: " + reportFolder);

                ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                reportFolder.zip(outstr);

        /*
         * I did't use copyRecursiveTo or copyFrom due to
         * bug in
         * jekins:https://issues.jenkins-ci.org/browse
         * /JENKINS-9189 //(which is cleaimed to have been
         * fixed, but not. So I zip the folder to stream and
         * copy it to the master.
         */

                ByteArrayInputStream instr = new ByteArrayInputStream(outstr.toByteArray());

                archivedFile.copyFrom(instr);

                outstr.close();
                instr.close();
                return true;
            } else {
                listener.getLogger().println(
                        "No report folder was found in: " + reportFolder);
            }
        }

        return false;
    }

    private boolean isArchiveTestResult(String testStatus, String archiveTestResultMode) {
        if (archiveTestResultMode.equals(ResultsPublisherModel.alwaysArchiveResults.getValue()) ||
                archiveTestResultMode.equals(ResultsPublisherModel.CreateHtmlReportResults.getValue())) {
            return true;
        } else if (archiveTestResultMode.equals(ResultsPublisherModel.ArchiveFailedTestsResults.getValue())) {
            if ("fail".equals(testStatus)) {
                return true;
            } else if (archiveTestResultMode.equals(ResultsPublisherModel.dontArchiveResults.getValue())) {
                return false;
            }
        }
        return false;
    }

    /**
     * Copy Summary Html reports created by LoadRunner
     *
     * @param reportFolder
     * @param testFolderPath
     * @param artifactsDir
     * @param reportNames
     * @param testResult
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("squid:S134")
    private void createHtmlReport(FilePath reportFolder,
                                  String testFolderPath,
                                  File artifactsDir,
                                  List<String> reportNames,
                                  TestResult testResult) throws IOException, InterruptedException {
        String archiveTestResultMode =
                _resultsPublisherModel.getArchiveTestResultsMode();
        boolean createReport = archiveTestResultMode.equals(ResultsPublisherModel.CreateHtmlReportResults.getValue());


        if (createReport) {
            File testFolderPathFile = new File(testFolderPath);
            FilePath srcDirectoryFilePath = new FilePath(reportFolder, HTML_REPORT_FOLDER);
            if (srcDirectoryFilePath.exists()) {
                FilePath srcFilePath = new FilePath(srcDirectoryFilePath, IE_REPORT_FOLDER);
                if (srcFilePath.exists()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    srcFilePath.zip(baos);
                    File reportDirectory = new File(artifactsDir.getParent(), PERFORMANCE_REPORT_FOLDER);
                    if (!reportDirectory.exists()) {
                        reportDirectory.mkdir();
                    }
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    FilePath reportDirectoryFilePath = new FilePath(reportDirectory);
                    FilePath tmpZipFile = new FilePath(reportDirectoryFilePath, "tmp.zip");
                    tmpZipFile.copyFrom(bais);
                    bais.close();
                    baos.close();
                    tmpZipFile.unzip(reportDirectoryFilePath);
                    String newFolderName = org.apache.commons.io.FilenameUtils.getName(testFolderPathFile.getPath());
                    FileUtils.moveDirectory(new File(reportDirectory, IE_REPORT_FOLDER),
                            new File(reportDirectory, newFolderName));
                    tmpZipFile.delete();
                    outputReportFiles(reportNames, reportDirectory, testResult, false);
                }
            }
        }
    }

    /**
     * creates index files as index for the different scenarios.
     *
     * @param reportNames
     * @param reportDirectory
     * @param testResult
     * @param tranSummary
     * @throws IOException
     */
    private void outputReportFiles(List<String> reportNames, File reportDirectory, TestResult testResult,
                                   boolean tranSummary) throws IOException {

        if (reportNames.isEmpty()) {
            return;
        }
        String title = (tranSummary) ? "Transaction Summary" : "Performance Report";
        String htmlFileName = (tranSummary) ? (TRANSACTION_REPORT_NAME + ".html") : "HTML.html";
        File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
        BufferedWriter writer = new BufferedWriter(new FileWriter(htmlIndexFile));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>%n");
        writer.write("<HTML><HEAD>%n");
        writer.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">%n");
        writer.write(String.format("<TITLE>%s</TITLE>%n", title));
        writer.write("</HEAD>%n");
        writer.write("<BODY>%n");
        writer.write(
                "<table style=\"font-size:15px;width:100%;max-width:100%;border-left:1px solid #DDD;border-right:1px " +
                        "solid #DDD;border-bottom:1px solid #DDD;\">%n");
        writer.write(
                "<tr style=\"background-color: #F1F1F1;\"><th style=\"padding:8px;line-height:1.42857;" +
                        "vertical-align:top;border-top:1px solid #DDD;\">Name</th></tr>%n");
        boolean rolling = true;
        for (String report : reportNames) {
            if (rolling) {
                writer.write(String.format(
                        "<tr style=\"background-color: #FFF;\"><td style=\"padding:8px;line-height:1.42857;" +
                                "vertical-align:top;border-top:1px solid #DDD;" +
                                "\"><a href=\"./%s/%s\">%s</a></td></tr>%n",
                        report, htmlFileName, report));
                rolling = false;
            } else {
                writer.write(String.format(
                        "<tr style=\"background-color: #F1F1F1;\"><td style=\"padding:8px;line-height:1.42857;" +
                                "vertical-align:top;border-top:1px solid #DDD;" +
                                "\"><a href=\"./%s/%s\">%s</a></td></tr>%n",
                        report, htmlFileName, report));
                rolling = true;
            }
        }
        writer.write("</table>%n");
        writer.write("</BODY>%n");
        writer.flush();
        writer.close();

        File indexFile = new File(reportDirectory, REPORT_INDEX_NAME);
        writer = new BufferedWriter(new FileWriter(indexFile));

        Iterator<SuiteResult> resultIterator = null;
        if ((testResult != null) && (!testResult.getSuites().isEmpty())) {
            resultIterator = testResult.getSuites().iterator();//get the first
        }
        for (String report : reportNames) {
            SuiteResult suitResult = null;
            if ((resultIterator != null) && resultIterator.hasNext()) {
                suitResult = resultIterator.next();
            }
            if (suitResult == null) {
                writer.write(report + "\t##\t##\t##%n");
            } else {
                int iDuration = (int) suitResult.getDuration();
                StringBuilder bld = new StringBuilder();
                String duration = "";
                if ((iDuration / SECS_IN_DAY) > 0) {
                    bld.append(String.format("%dday ", iDuration / SECS_IN_DAY));
                    iDuration = iDuration % SECS_IN_DAY;
                }
                if ((iDuration / SECS_IN_HOUR) > 0) {
                    bld.append(String.format("%02dhr ", iDuration / SECS_IN_HOUR));
                    iDuration = iDuration % SECS_IN_HOUR;
                } else if (!duration.isEmpty()) {
                    bld.append("00hr ");
                }
                if ((iDuration / SECS_IN_MINUTE) > 0) {
                    bld.append(String.format("%02dmin ", iDuration / SECS_IN_MINUTE));
                    iDuration = iDuration % SECS_IN_MINUTE;
                } else if (!duration.isEmpty()) {
                    bld.append("00min ");
                }
                bld.append(String.format("%02dsec", iDuration));
                duration = bld.toString();
                int iPassCount = 0;
                int iFailCount = 0;
                for (Iterator i = suitResult.getCases().iterator(); i.hasNext(); ) {
                    CaseResult caseResult = (CaseResult) i.next();
                    iPassCount += caseResult.getPassCount();
                    iFailCount += caseResult.getFailCount();
                }
                writer.write(
                        String.format("%s\t%s\t%d\t%d%n",
                                report,
                                duration,
                                iPassCount,
                                iFailCount));
            }
        }
        writer.flush();
        writer.close();
    }

    /**
     * Copies and creates the transaction summery on the master
     *
     * @param reportFolder
     * @param testFolderPath
     * @param artifactsDir
     * @param reportNames
     * @param testResult
     * @throws IOException
     * @throws InterruptedException
     */
    private void createTransactionSummary(FilePath reportFolder,
                                          String testFolderPath,
                                          File artifactsDir,
                                          List<String> reportNames,
                                          TestResult testResult) throws IOException, InterruptedException {

        File testFolderPathFile = new File(testFolderPath);
        String subFolder = HTML_REPORT_FOLDER + File.separator + IE_REPORT_FOLDER + File.separator + HTML_REPORT_FOLDER;
        FilePath htmlReportPath = new FilePath(reportFolder, subFolder);
        if (htmlReportPath.exists()) {
            File reportDirectory = new File(artifactsDir.getParent(), TRANSACTION_SUMMARY_FOLDER);
            if (!reportDirectory.exists()) {
                reportDirectory.mkdir();
            }
            String newFolderName = org.apache.commons.io.FilenameUtils.getName(testFolderPathFile.getPath());
            File testDirectory = new File(reportDirectory, newFolderName);
            if (!testDirectory.exists()) {
                testDirectory.mkdir();
            }

            FilePath dstReportPath = new FilePath(testDirectory);
            FileFilter reportFileFilter = new WildcardFileFilter(TRANSACTION_REPORT_NAME + ".*");
            List<FilePath> reporFiles = htmlReportPath.list(reportFileFilter);
            for (FilePath fileToCopy : reporFiles) {
                FilePath dstFilePath = new FilePath(dstReportPath, fileToCopy.getName());
                fileToCopy.copyTo(dstFilePath);
            }
            FilePath cssFilePath = new FilePath(htmlReportPath, "Properties.css");
            if (cssFilePath.exists()) {
                FilePath dstFilePath = new FilePath(dstReportPath, cssFilePath.getName());
                cssFilePath.copyTo(dstFilePath);
            }
            FilePath pngFilePath = new FilePath(htmlReportPath, "tbic_toexcel.png");
            if (pngFilePath.exists()) {
                FilePath dstFilePath = new FilePath(dstReportPath, pngFilePath.getName());
                pngFilePath.copyTo(dstFilePath);
            }

            outputReportFiles(reportNames, reportDirectory, testResult, true);

        }

    }

    /*
     * if we have a directory with file name "file.zip" we will return "file_1.zip"
     */
    private String getUniqueZipFileNameInFolder(ArrayList<String> names, String fileName)
            throws IOException, InterruptedException {

        String result = fileName + "_Report.zip";

        int index = 0;

        while (names.indexOf(result) > -1) {
            result = fileName + "_" + (++index) + "_Report.zip";
        }

        return result;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        final List<String> mergedResultNames = new ArrayList<String>();

        Project<?, ?> project = RuntimeUtils.cast(build.getParent());
        List<Builder> builders = project.getBuilders();

        final List<String> almResultNames = new ArrayList<String>();
        final List<String> fileSystemResultNames = new ArrayList<String>();
        final List<String> almSSEResultNames = new ArrayList<String>();
        final List<String> pcResultNames = new ArrayList<String>();

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
        return;
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

            return "Publish HP tests result";
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

    /**
     * The type Rrv file filter.
     */
    public class RRVFileFilter implements FileFilter {
        private final String[] excludedFilenames =
                new String[]{"run_results.xml", "run_results.html", "diffcompare", "Resources"};
        private final String[] excludedDirnames = new String[]{"diffcompare", "Resources", "CheckPoints", "Snapshots"};

        @Override
        public boolean accept(File file) {
            boolean bRet = true;

            for (String filename : excludedFilenames) {
                if (file.getName().equals(filename)) {
                    bRet = false;
                    break;
                }
            }

            if (bRet) {
                for (String parentname : excludedDirnames) {
                    if (file.getParent().contains(parentname)) {
                        bRet = false;
                        break;
                    }
                }
            }
            return bRet;
        }

    }
}
