// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.results;

<<<<<<< HEAD
import hudson.AbortException;
=======
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
<<<<<<< HEAD
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Project;
=======
import hudson.AbortException;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Result;
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;
<<<<<<< HEAD

=======
import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
        
        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();
            final long nowMaster = System.currentTimeMillis();
            
            TestResult result = build.getWorkspace().act(new FileCallable<TestResult>() {
                
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
            archiveTestsReport(build, listener, fileSystemResultNames);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void archiveTestsReport(
            AbstractBuild<?, ?> build,
            BuildListener listener,
            List<String> resultFiles) throws ParserConfigurationException, SAXException,
            IOException, InterruptedException {
        
        if ((resultFiles == null) || (resultFiles.size() == 0)) {
            return;
        }
        
        ArrayList<String> zipFileNames = new ArrayList<String>();
        ArrayList<FilePath> reportFolders = new ArrayList<FilePath>();
        
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
                    
                    if (archiveTestResultMode.equals(ResultsPublisherModel.alwaysArchiveResults.getValue())) {
                        archiveTestResult = true;
                    } else if (archiveTestResultMode.equals(ResultsPublisherModel.ArchiveFailedTestsResults.getValue())) {
                        if (testStatus.equals("fail")) {
                            archiveTestResult = true;
                        } else if (archiveTestResultMode.equals(ResultsPublisherModel.dontArchiveResults.getValue())) {
                            archiveTestResult = false;
                        }
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
                }
            }
        }
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
=======
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.run.*;

/**
 * This class is adapted from {@link JunitResultArchiver}; Only the
 * {@code perform()} method slightly differs.
 * 
 * @author Thomas Maurel
 */
public class RunResultRecorder extends Recorder implements Serializable,
		MatrixAggregatable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ResultsPublisherModel resultsPublisherModel;

	@DataBoundConstructor
	public RunResultRecorder(boolean publishResults,
			String archiveTestResultsMode) {
		resultsPublisherModel = new ResultsPublisherModel(
				archiveTestResultsMode);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		TestResultAction action;
		List<Builder> builders = ((Project) build.getProject()).getBuilders();

		final List<String> almResultNames = new ArrayList<String>();
		final List<String> fileSystemResultNames = new ArrayList<String>();
		final List<String> mergedResultNames = new ArrayList<String>();

		// Get the TestSet report files names of the current build
		for (Builder builder : builders) {
			if (builder instanceof RunFromAlmBuilder) {
				almResultNames.add(((RunFromAlmBuilder) builder)
						.getRunResultsFileName());
			}
			if (builder instanceof RunFromFileBuilder) {
				fileSystemResultNames.add(((RunFromFileBuilder) builder)
						.getRunResultsFileName());
			}
			// names.add("APIResults.xml");
		}

		mergedResultNames.addAll(almResultNames);
		mergedResultNames.addAll(fileSystemResultNames);

		// Has any QualityCenter builder been set up?
		if (mergedResultNames.isEmpty()) {
			listener.getLogger().println(
					"RunResultRecorder: no results xml File provided");
			return true;
		}

		try {
			final long buildTime = build.getTimestamp().getTimeInMillis();
			final long nowMaster = System.currentTimeMillis();

			TestResult result = build.getWorkspace().act(
					new FileCallable<TestResult>() {
						private static final long serialVersionUID = 1L;

						public TestResult invoke(File ws, VirtualChannel channel)
								throws IOException {
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

							return new TestResult(buildTime
									+ (nowSlave - nowMaster), ds, true);
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
			ArchiveTestsReport(build, listener, fileSystemResultNames);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	@SuppressWarnings("unused")
	private void ArchiveTestsReport(AbstractBuild build,
			BuildListener listener, List<String> resultFiles)
			throws ParserConfigurationException, SAXException, IOException,
			InterruptedException {

		if ((resultFiles == null) || (resultFiles.size() == 0)) {
			return;
		}

		ArrayList<String> zipFileNames = new ArrayList<String>();
		ArrayList<FilePath> reportFolders = new ArrayList<FilePath>();

		listener.getLogger().println(
				"Report archiving mode is set to: "
						+ resultsPublisherModel.getArchiveTestResultsMode());

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

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(resultsFile.read());
			doc.getDocumentElement().normalize();

			Node testSuiteNode = doc.getElementsByTagName("testsuite").item(0);
			NodeList testCasesNodes = ((Element) testSuiteNode)
					.getElementsByTagName("testcase");

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

					FilePath reportFolder = new FilePath(
							projectWS.getChannel(), reportFolderPath);

					reportFolders.add(reportFolder);

					String archiveTestResultMode = resultsPublisherModel
							.getArchiveTestResultsMode();
					boolean archiveTestResult = false;

					if (archiveTestResultMode
							.equals(ResultsPublisherModel.alwaysArchiveResults
									.getValue())) {
						archiveTestResult = true;
					} else if (archiveTestResultMode.equals(ResultsPublisherModel.ArchiveFailedTestsResults.getValue())) {
						if (testStatus.equals("fail")) {
							archiveTestResult = true;
						}
						else if (archiveTestResultMode.equals(ResultsPublisherModel.dontArchiveResults.getValue())){
							archiveTestResult = false;
						}
					}

					if (archiveTestResult) {

						if (reportFolder.exists()) {

							FilePath testFolder = new FilePath(
									projectWS.getChannel(), testFolderPath);

							String zipFileName = GetUniqueZipFileNameInFolder(
									zipFileNames, testFolder.getName());
							zipFileNames.add(zipFileName);

							listener.getLogger().println(
									"Zipping report folder: "
											+ reportFolderPath);

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

							ByteArrayInputStream instr = new ByteArrayInputStream(
									outstr.toByteArray());

							FilePath archivedFile = new FilePath(new FilePath(
									artifactsDir), zipFileName);
							archivedFile.copyFrom(instr);

							outstr.close();
							instr.close();

						} else {
							listener.getLogger().println(
									"No report folder was found in: "
											+ reportFolderPath);
						}
					}
				}
			}
		}
		
		deleteReportsFolder(reportFolders,listener);
	}

	
	private void deleteReportsFolder(ArrayList<FilePath> reportFolders,BuildListener listener){
		if (reportFolders.size() > 0) {
			listener.getLogger().println("Starting to delete reports folder: " + reportFolders.size() + " were found");

			for (FilePath reportPath : reportFolders) {
				try {
					reportPath.deleteRecursive();

				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			listener.getLogger().println("Finished deleting reports folder");
		}		
	}
	
	
	/*
	 * if we have a directory with file name "file.zip" we will return
	 * "file_1.zip";
	 */
	private String GetUniqueZipFileNameInFolder(ArrayList<String> names,
			String fileName) throws IOException, InterruptedException {

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

	public MatrixAggregator createAggregator(MatrixBuild build,
			Launcher launcher, BuildListener listener) {
		return new TestResultAggregator(build, launcher, listener);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	public ResultsPublisherModel getResultsPublisherModel() {
		return resultsPublisherModel;
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			load();
		}

		public String getDisplayName() {
			return "Publish HP tests result";
		}

		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		public List<EnumDescription> getReportArchiveModes() {
			return ResultsPublisherModel.archiveModes;
		}

	}
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
}
