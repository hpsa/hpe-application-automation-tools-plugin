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
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
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
                    
                    String archiveTestResultMode =
                            _resultsPublisherModel.getArchiveTestResultsMode();
                    boolean archiveTestResult = false;
                    boolean createHtmlReport = false;
                    
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
                        createHtmlReport = true;
                    }
                    
                    if (archiveTestResult) {
                        
                        if (reportFolder.exists()) {
                            
                            FilePath testFolder =
                                    new FilePath(projectWS.getChannel(), testFolderPath);
                            
                            String zipFileName =
                                    getUniqueZipFileNameInFolder(zipFileNames, testFolder.getName());
                            zipFileNames.add(zipFileName);
                            
                            listener.getLogger().println(
                                    "Zipping report folder: " + reportFolderPath);
                            
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
                            
                            FilePath archivedFile =
                                    new FilePath(new FilePath(artifactsDir), zipFileName);
                            archivedFile.copyFrom(instr);
                            
                            outstr.close();
                            instr.close();
                            
                        } else {
                            listener.getLogger().println(
                                    "No report folder was found in: " + reportFolderPath);
                        }
                    }
                    
                    if (createHtmlReport) {
                        File testFolderPathFile = new File(testFolderPath);
                        FilePath testFolderFilePath = new FilePath(projectWS.getChannel(), reportFolderPath);
                        FilePath srcDirectoryFilePath = new FilePath(testFolderFilePath, HTML_REPORT_FOLDER);
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
                                reportNames.add(testFolderPathFile.getName());
                                outputReportFiles(reportNames, reportDirectory, testResult);
                            }
                        }
                    }
                }
            }
        }
    }

    private void outputReportFiles(List<String> reportNames, File reportDirectory, TestResult testResult) throws IOException {

        if (reportNames.size() <= 0)
            return;
        File htmlIndexFile = new File(reportDirectory, INDEX_HTML_NAME);
        BufferedWriter writer = new BufferedWriter(new FileWriter(htmlIndexFile));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        writer.write("<HTML><HEAD>\n");
        writer.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n");
        writer.write("<TITLE>Performance Report</TITLE>\n");
        writer.write("</HEAD>\n");
        writer.write("<BODY>\n");
        writer.write("<table style=\"font-size:15px;width:100%;max-width:100%;border-left:1px solid #DDD;border-right:1px solid #DDD;border-bottom:1px solid #DDD;\">\n");
        writer.write("<tr style=\"background-color: #F1F1F1;\"><th style=\"padding:8px;line-height:1.42857;vertical-align:top;border-top:1px solid #DDD;\">Name</th></tr>\n");
        boolean rolling = true;
        for (String report : reportNames) {
            if (rolling) {
                writer.write(String.format("<tr style=\"background-color: #FFF;\"><td style=\"padding:8px;line-height:1.42857;vertical-align:top;border-top:1px solid #DDD;\"><a href=\"./%s/HTML.html\">%s</a></td></tr>\n", report, report));
                rolling = false;
            } else {
                writer.write(String.format("<tr style=\"background-color: #F1F1F1;\"><td style=\"padding:8px;line-height:1.42857;vertical-align:top;border-top:1px solid #DDD;\"><a href=\"./%s/HTML.html\">%s</a></td></tr>\n", report, report));
                rolling = true;
            }
        }
        writer.write("</table>\n");
        writer.write("</BODY>\n");
        writer.flush();
        writer.close();

        File indexFile = new File(reportDirectory, REPORT_INDEX_NAME);
        writer = new BufferedWriter(new FileWriter(indexFile));

        Iterator<CaseResult> resultIterator = null;
        if ((testResult != null) && (testResult.getSuites().size() > 0)) {
            resultIterator = testResult.getSuites().iterator().next().getCases().iterator();//get the first
        }
        for (String report : reportNames) {
            CaseResult caseResult = null;
            if ((resultIterator != null) && resultIterator.hasNext())
                caseResult = resultIterator.next();
            if (caseResult == null)
                writer.write(report + "\t##\t##\t##\n");
            else {
                int iDuration = (int) caseResult.getDuration();
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
                
                writer.write(
                        String.format("%s\t%s\t%d\t%d\n", 
                                report, 
                                duration, 
                                caseResult.getPassCount(), 
                                caseResult.getFailCount()));
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
