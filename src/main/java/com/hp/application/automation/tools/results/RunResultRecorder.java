// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.results;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.application.automation.tools.common.RuntimeUtils;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.run.RunFromAlmBuilder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import com.hp.application.automation.tools.run.SseBuilder;
import com.hp.application.automation.tools.run.PcBuilder;

/**
 * This class is adapted from {@link JunitResultArchiver}; Only the {@code perform()} method
 * slightly differs.
 * 
 * @author Thomas Maurel
 */
public class RunResultRecorder extends Recorder implements Serializable, MatrixAggregatable {
    
    private static final long serialVersionUID = 1L;
    private static final String PERFORMANCE_REPORT_FOLDER = "PerformanceReport";
    private static final String IE_REPORT_FOLDER = "IE";
    private static final String HTML_REPORT_FOLDER = "HTML";
    private static final String INDEX_HTML_NAME = "index.html";
    private static final String REPORT_INDEX_NAME = "report.index";
    private static final String TRANSACTION_SUMMARY_FOLDER = "TransactionSummary";
    private static final String TRANSACTION_REPORT_NAME = "Report3";
    private final ResultsPublisherModel _resultsPublisherModel;
    
    @DataBoundConstructor
    public RunResultRecorder(boolean publishResults, String archiveTestResultsMode) {
        
        _resultsPublisherModel = new ResultsPublisherModel(archiveTestResultsMode);
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        
        return (DescriptorImpl) super.getDescriptor();
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        
        TestResultAction action;
        Project<?, ?> project = RuntimeUtils.cast(build.getProject());
        List<Builder> builders = project.getBuilders();
        
        final List<String> almResultNames = new ArrayList<String>();
        final List<String> fileSystemResultNames = new ArrayList<String>();
        final List<String> mergedResultNames = new ArrayList<String>();
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
                if (resultsFileName != null)
                    almSSEResultNames.add(resultsFileName);
            } else if (builder instanceof PcBuilder) {
            	String resultsFileName = ((PcBuilder) builder).getRunResultsFileName();
            	if (resultsFileName != null)
            		pcResultNames.add(resultsFileName);
            }
        }
        
        mergedResultNames.addAll(almResultNames);
        mergedResultNames.addAll(fileSystemResultNames);
        mergedResultNames.addAll(almSSEResultNames);
        mergedResultNames.addAll(pcResultNames);
        
        // Has any QualityCenter builder been set up?
        if (mergedResultNames.isEmpty()) {
            listener.getLogger().println("RunResultRecorder: no results xml File provided");
            return true;
        }

        TestResult result = null;
        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();
            final long nowMaster = System.currentTimeMillis();
            
            result = build.getWorkspace().act(new FileCallable<TestResult>() {
                
                private static final long serialVersionUID = 1L;
                
                @Override
                public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                    final long nowSlave = System.currentTimeMillis();
                    List<String> files = new ArrayList<String>();
                    DirectoryScanner ds = new DirectoryScanner();
                    ds.setBasedir(ws);
                    
                    // Transform the report file names list to a
                    // File
                    // Array,
                    // and add it to the DirectoryScanner includes
                    // set
                    for (String name : mergedResultNames) {
                        File file = new File(ws, name);
                        if (file.exists()) {
                            files.add(file.getName());
                        }
                    }
                    
                    Object[] objectArray = new String[files.size()];
                    files.toArray(objectArray);
                    ds.setIncludes((String[]) objectArray);
                    ds.scan();
                    if (ds.getIncludedFilesCount() == 0) {
                        // no test result. Most likely a
                        // configuration
                        // error or
                        // fatal problem
                        throw new AbortException("Report not found");
                    }
                    
                    return new TestResult(buildTime + (nowSlave - nowMaster), ds, true);
                }
            });
            
            action = new TestResultAction(build, result, listener);
            if (result.getPassCount() == 0 && result.getFailCount() == 0) {
                throw new AbortException("Result is empty");
            }
        } catch (AbortException e) {
            if (build.getResult() == Result.FAILURE) {
                // most likely a build failed before it gets to the test
                // phase.
                // don't report confusing error message.
                return true;
            }
            
            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to archive testing tool reports"));
            build.setResult(Result.FAILURE);
            return true;
        }
        
        build.getActions().add(action);
        
        try {
            archiveTestsReport(build, listener, fileSystemResultNames, result);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        File artifactsDir = build.getArtifactsDir();
        if (artifactsDir.exists()) {
            File reportDirectory = new File(artifactsDir.getParent(), PERFORMANCE_REPORT_FOLDER);
            if (reportDirectory.exists()) {
                File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
                if (htmlIndexFile.exists())
                    build.getActions().add(new PerformanceReportAction(build));
            }

            File summaryDirectory = new File(artifactsDir.getParent(), TRANSACTION_SUMMARY_FOLDER);
            if (summaryDirectory.exists()) {
                File htmlIndexFile = new File(summaryDirectory, INDEX_HTML_NAME);
                if (htmlIndexFile.exists())
                    build.getActions().add(new TransactionSummaryAction(build));
            }
        }
        
        return true;
    }
    
    private void archiveTestsReport(
            AbstractBuild<?, ?> build,
            BuildListener listener,
            List<String> resultFiles,
            TestResult testResult) throws ParserConfigurationException, SAXException,
            IOException, InterruptedException {
        
        if ((resultFiles == null) || (resultFiles.size() == 0)) {
            return;
        }
        
        ArrayList<String> zipFileNames = new ArrayList<String>();
        ArrayList<FilePath> reportFolders = new ArrayList<FilePath>();
        List<String> reportNames = new ArrayList<String>();
        
        listener.getLogger().println(
                "Report archiving mode is set to: "
                        + _resultsPublisherModel.getArchiveTestResultsMode());
        
        // if we dont want to archive any results
        /*if (resultsPublisherModel.getArchiveTestResultsMode().equals(
        		ResultsPublisherModel.dontArchiveResults.getValue())) {
        	
        	deleteReportsFolder(reportFolders,listener);
        	return;
        }*/

        FilePath projectWS = build.getWorkspace();
        
        // get the artifacts directory where we will upload the zipped report
        // folder
        File artifactsDir = build.getArtifactsDir();
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

        for (String resultsFilePath : resultFiles) {
            FilePath resultsFile = projectWS.child(resultsFilePath);
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            Document doc = dBuilder.parse(resultsFile.read());
            doc.getDocumentElement().normalize();
            
            Node testSuiteNode = doc.getElementsByTagName("testsuite").item(0);
            Element testSuiteElement = (Element) testSuiteNode;
            if(testSuiteElement.hasAttribute("name") && testSuiteElement.getAttribute("name").endsWith(".lrs")) {
                NodeList testSuiteNodes = doc.getElementsByTagName("testsuite");
                for (int i = 0; i < testSuiteNodes.getLength(); i++) {
                    testSuiteNode = testSuiteNodes.item(i);
                    testSuiteElement = (Element) testSuiteNode;
                    if (!testSuiteElement.hasAttribute("name")) {
                        continue;
                    }
                    String testFolderPath = testSuiteElement.getAttribute("name");
                    String testStatus = (testSuiteElement.getAttribute("failures").equals("0")) ? "pass" : "fail";

                    Node testCaseNode = testSuiteElement.getElementsByTagName("testcase").item(0);
                    if (testCaseNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element testCaseElement = (Element) testCaseNode;

                        if (!testCaseElement.hasAttribute("report")) {
                            continue;
                        }

                        String reportFolderPath = testCaseElement.getAttribute("report");
                        FilePath reportFolder = new FilePath(projectWS.getChannel(), reportFolderPath);
                        reportFolders.add(reportFolder);

                        FilePath testFolder =
                                new FilePath(projectWS.getChannel(), testFolderPath);
                        String zipFileName =
                                getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                        FilePath archivedFile =
                                new FilePath(new FilePath(artifactsDir), zipFileName);

                        if (archiveFolder(reportFolder, testStatus, archivedFile, listener))
                            zipFileNames.add(zipFileName);

                        reportNames.add(testFolder.getName());
                        createHtmlReport(reportFolder, testFolderPath, artifactsDir, reportNames, testResult);
                        createTransactionSummary(reportFolder, testFolderPath, artifactsDir, reportNames, testResult);
                    }
                }
            } else {
                NodeList testCasesNodes = ((Element) testSuiteNode).getElementsByTagName("testcase");

                for (int i = 0; i < testCasesNodes.getLength(); i++) {

                    Node nNode = testCasesNodes.item(i);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        if (!eElement.hasAttribute("report")) {
                            continue;
                        }

                        String reportFolderPath = eElement.getAttribute("report");
                        String testFolderPath = eElement.getAttribute("name");
                        String testStatus = eElement.getAttribute("status");

                        FilePath reportFolder = new FilePath(projectWS.getChannel(), reportFolderPath);
                        reportFolders.add(reportFolder);

                        FilePath testFolder =
                                new FilePath(projectWS.getChannel(), testFolderPath);
                        String zipFileName =
                                getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                        FilePath archivedFile =
                                new FilePath(new FilePath(artifactsDir), zipFileName);

                        if (archiveFolder(reportFolder, testStatus, archivedFile, listener))
                            zipFileNames.add(zipFileName);
                    }
                }
            }
        }
    }

    private boolean archiveFolder(FilePath reportFolder,
                                  String testStatus,
                                  FilePath archivedFile,
                                  BuildListener listener) throws IOException, InterruptedException {
        String archiveTestResultMode =
                _resultsPublisherModel.getArchiveTestResultsMode();
        boolean archiveTestResult = false;

        if (archiveTestResultMode.equals(ResultsPublisherModel.alwaysArchiveResults.getValue())) {
            archiveTestResult = true;
        } else if (archiveTestResultMode.equals(ResultsPublisherModel.ArchiveFailedTestsResults.getValue())) {
            if (testStatus.equals("fail")) {
                archiveTestResult = true;
            } else if (archiveTestResultMode.equals(ResultsPublisherModel.dontArchiveResults.getValue())) {
                archiveTestResult = false;
            }
        } else if (archiveTestResultMode.equals(ResultsPublisherModel.CreateHtmlReportResults.getValue())) {
            archiveTestResult = true;
        }

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

                ByteArrayInputStream instr =
                        new ByteArrayInputStream(outstr.toByteArray());

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
                    if (!reportDirectory.exists())
                        reportDirectory.mkdir();
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    FilePath reportDirectoryFilePath = new FilePath(reportDirectory);
                    FilePath tmpZipFile = new FilePath(reportDirectoryFilePath, "tmp.zip");
                    tmpZipFile.copyFrom(bais);
                    bais.close();
                    baos.close();
                    tmpZipFile.unzip(reportDirectoryFilePath);
                    FileUtils.moveDirectory(new File(reportDirectory, IE_REPORT_FOLDER), new File(reportDirectory, testFolderPathFile.getName()));
                    tmpZipFile.delete();
                    outputReportFiles(reportNames, reportDirectory, testResult, false);
                }
            }
        }
    }

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
            if (!reportDirectory.exists())
                reportDirectory.mkdir();

            File testDirectory = new File(reportDirectory, testFolderPathFile.getName());
            if (!testDirectory.exists())
                testDirectory.mkdir();

            FilePath dstReportPath = new FilePath(testDirectory);
            File dir = new File(htmlReportPath.toURI());
            FileFilter fileFilter = new WildcardFileFilter(TRANSACTION_REPORT_NAME + ".*");
            List<FilePath> files = htmlReportPath.list(fileFilter);
            for (Iterator i = files.iterator(); i.hasNext(); ) {
                FilePath fileToCopy = (FilePath) i.next();
                FilePath dstFilePath = new FilePath(dstReportPath, fileToCopy.getName());
                fileToCopy.copyTo(dstFilePath);
            }

            outputReportFiles(reportNames, reportDirectory, testResult, true);

        }

    }

    private void outputReportFiles(List<String> reportNames, File reportDirectory, TestResult testResult, boolean tranSummary) throws IOException {

        if (reportNames.size() <= 0)
            return;
        String title = (tranSummary) ? "Transaction Summary" : "Performance Report";
        String htmlFileName = (tranSummary) ? (TRANSACTION_REPORT_NAME + ".html") : "HTML.html";
        File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
        BufferedWriter writer = new BufferedWriter(new FileWriter(htmlIndexFile));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        writer.write("<HTML><HEAD>\n");
        writer.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n");
        writer.write(String.format("<TITLE>%s</TITLE>\n", title));
        writer.write("</HEAD>\n");
        writer.write("<BODY>\n");
        writer.write("<table style=\"font-size:15px;width:100%;max-width:100%;border-left:1px solid #DDD;border-right:1px solid #DDD;border-bottom:1px solid #DDD;\">\n");
        writer.write("<tr style=\"background-color: #F1F1F1;\"><th style=\"padding:8px;line-height:1.42857;vertical-align:top;border-top:1px solid #DDD;\">Name</th></tr>\n");
        boolean rolling = true;
        for (String report : reportNames) {
            if (rolling) {
                writer.write(String.format("<tr style=\"background-color: #FFF;\"><td style=\"padding:8px;line-height:1.42857;vertical-align:top;border-top:1px solid #DDD;\"><a href=\"./%s/%s\">%s</a></td></tr>\n", report, htmlFileName, report));
                rolling = false;
            } else {
                writer.write(String.format("<tr style=\"background-color: #F1F1F1;\"><td style=\"padding:8px;line-height:1.42857;vertical-align:top;border-top:1px solid #DDD;\"><a href=\"./%s/%s\">%s</a></td></tr>\n", report, htmlFileName, report));
                rolling = true;
            }
        }
        writer.write("</table>\n");
        writer.write("</BODY>\n");
        writer.flush();
        writer.close();

        File indexFile = new File(reportDirectory, REPORT_INDEX_NAME);
        writer = new BufferedWriter(new FileWriter(indexFile));

        Iterator<SuiteResult> resultIterator = null;
        if ((testResult != null) && (testResult.getSuites().size() > 0)) {
            resultIterator = testResult.getSuites().iterator();//get the first
        }
        for (String report : reportNames) {
            SuiteResult suiteResult = null;
            if ((resultIterator != null) && resultIterator.hasNext())
                suiteResult = resultIterator.next();
            if (suiteResult == null)
                writer.write(report + "\t##\t##\t##\n");
            else {
                int iDuration = (int) suiteResult.getDuration();
                String duration = "";
                if ((iDuration / 86400) > 0) {
                    duration += String.format("%dday ", iDuration / 86400);
                    iDuration = iDuration % 86400;
                }
                if ((iDuration / 3600) > 0) {
                    duration += String.format("%02dhr ", iDuration / 3600);
                    iDuration = iDuration % 3600;
                } else if (!duration.isEmpty()) {
                    duration += "00hr ";
                }
                if ((iDuration / 60) > 0) {
                    duration += String.format("%02dmin ", iDuration / 60);
                    iDuration = iDuration % 60;
                } else if (!duration.isEmpty()) {
                    duration += "00min ";
                }
                duration += String.format("%02dsec", iDuration);

                int suitePassCount = 0;
                int suiteFailCount = 0;
                for (CaseResult caseResult : suiteResult.getCases()) {
                    suitePassCount += caseResult.getPassCount();
                    suiteFailCount += caseResult.getFailCount();
                }
                writer.write(
                        String.format("%s\t%s\t%d\t%d\n",
                                report,
                                duration,
                                suitePassCount,
                                suiteFailCount));
            }
        }
        writer.flush();
        writer.close();
    }

    /*
     * if we have a directory with file name "file.zip" we will return
     * "file_1.zip";
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
    public Action getProjectAction(AbstractProject<?, ?> project) {
        
        return new TestResultProjectAction(project);
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
    
    public ResultsPublisherModel getResultsPublisherModel() {
        
        return _resultsPublisherModel;
    }
    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        
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
        
        public List<EnumDescription> getReportArchiveModes() {
            
            return ResultsPublisherModel.archiveModes;
        }
    }
}
