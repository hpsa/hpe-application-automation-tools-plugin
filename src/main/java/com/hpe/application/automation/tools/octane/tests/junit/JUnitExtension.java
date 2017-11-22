/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests.junit;

import com.google.inject.Inject;
import com.hpe.application.automation.tools.octane.actions.cucumber.CucumberTestResultsAction;
import com.hpe.application.automation.tools.octane.executor.CheckOutSubDirEnvContributor;
import com.hpe.application.automation.tools.octane.tests.HPRunnerType;
import com.hpe.application.automation.tools.octane.tests.MqmTestsExtension;
import com.hpe.application.automation.tools.octane.tests.TestResultContainer;
import com.hpe.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import com.hpe.application.automation.tools.octane.tests.detection.ResultFields;
import com.hpe.application.automation.tools.octane.tests.detection.ResultFieldsDetectionService;
import com.hpe.application.automation.tools.octane.tests.impl.ObjectStreamIterator;
import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;
import hudson.Extension;
import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;
import hudson.tasks.test.AbstractTestResultAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Converter of Jenkins test report to ALM Octane test report format(junitResult.xml->mqmTests.xml)
 */
@Extension
public class JUnitExtension extends MqmTestsExtension {
	private static Logger logger = LogManager.getLogger(JUnitExtension.class);

	private static final String STORM_RUNNER = "StormRunner";
	private static final String LOAD_RUNNER = "LoadRunner";
	private static final String PERFORMANCE_CENTER_RUNNER = "Performance Center";
	private static final String PERFORMANCE_TEST_TYPE = "Performance";

	private static final String JUNIT_RESULT_XML = "junitResult.xml"; // NON-NLS

	private static final String PERFORMANCE_REPORT = "PerformanceReport";
	private static final String TRANSACTION_SUMMARY = "TransactionSummary";

	@Inject
	private ResultFieldsDetectionService resultFieldsDetectionService;

	public boolean supports(Run<?, ?> build) throws IOException, InterruptedException {
		if (build.getAction(CucumberTestResultsAction.class) != null) {
			logger.debug("CucumberTestResultsAction found. Will not process JUnit results.");
			return false;
		} else if (build.getAction(AbstractTestResultAction.class) != null) {
			logger.debug("AbstractTestResultAction found, JUnit results expected");
			return true;
		} else {
			logger.debug("AbstractTestResultAction not found, no JUnit results expected");
			return false;
		}
	}

	@Override
	public TestResultContainer getTestResults(Run<?, ?> run, HPRunnerType hpRunnerType, String jenkinsRootUrl) throws IOException, InterruptedException {
		logger.debug("Collecting JUnit results");

		boolean isLoadRunnerProject = isLoadRunnerProject(run);
		FilePath resultFile = new FilePath(run.getRootDir()).child(JUNIT_RESULT_XML);
		if (resultFile.exists()) {
			logger.debug("JUnit result report found");
			ResultFields detectedFields = getResultFields(run, hpRunnerType, isLoadRunnerProject);
			FilePath filePath = BuildHandlerUtils.getWorkspace(run).act(new GetJUnitTestResults(run, Arrays.asList(resultFile), false, hpRunnerType, jenkinsRootUrl));
			return new TestResultContainer(new ObjectStreamIterator<TestResult>(filePath, true), detectedFields);
		} else {
			//avoid java.lang.NoClassDefFoundError when maven plugin is not present
			if ("hudson.maven.MavenModuleSetBuild".equals(run.getClass().getName())) {
				logger.debug("MavenModuleSetBuild detected, looking for results in maven modules");

				List<FilePath> resultFiles = new LinkedList<>();
				Map<MavenModule, MavenBuild> moduleLastBuilds = ((MavenModuleSetBuild) run).getModuleLastBuilds();
				for (MavenBuild mavenBuild : moduleLastBuilds.values()) {
					AbstractTestResultAction action = mavenBuild.getAction(AbstractTestResultAction.class);
					if (action != null) {
						FilePath moduleResultFile = new FilePath(mavenBuild.getRootDir()).child(JUNIT_RESULT_XML);
						if (moduleResultFile.exists()) {
							logger.debug("Found results in " + mavenBuild.getFullDisplayName());
							resultFiles.add(moduleResultFile);
						}
					}
				}
				if (!resultFiles.isEmpty()) {
					ResultFields detectedFields = getResultFields(run, hpRunnerType, isLoadRunnerProject);
					FilePath filePath = BuildHandlerUtils.getWorkspace(run).act(new GetJUnitTestResults(run, resultFiles, false, hpRunnerType, jenkinsRootUrl));
					return new TestResultContainer(new ObjectStreamIterator<TestResult>(filePath, true), detectedFields);
				}
			}
			logger.debug("No JUnit result report found");
			return null;
		}
	}

	private ResultFields getResultFields(Run<?, ?> build, HPRunnerType hpRunnerType, boolean isLoadRunnerProject) throws InterruptedException {
		ResultFields detectedFields;
		if (hpRunnerType.equals(HPRunnerType.StormRunner)) {
			detectedFields = new ResultFields(null, STORM_RUNNER, null);
		} else if (isLoadRunnerProject) {
			detectedFields = new ResultFields(null, LOAD_RUNNER, null);
		} else if (hpRunnerType.equals(HPRunnerType.PerformanceCenter)) {
			detectedFields = new ResultFields(null, PERFORMANCE_CENTER_RUNNER, null, PERFORMANCE_TEST_TYPE);
		} else {
			detectedFields = resultFieldsDetectionService.getDetectedFields(build);
		}

		return detectedFields;
	}

	private boolean isLoadRunnerProject(Run run) throws IOException, InterruptedException {
		FilePath performanceReportFolder = new FilePath(run.getRootDir()).child(PERFORMANCE_REPORT);
		FilePath transactionSummaryFolder = new FilePath(run.getRootDir()).child(TRANSACTION_SUMMARY);
		return performanceReportFolder.exists() &&
				performanceReportFolder.isDirectory() &&
				transactionSummaryFolder.exists() &&
				transactionSummaryFolder.isDirectory();
	}

	private static class GetJUnitTestResults implements FilePath.FileCallable<FilePath> {

		private final List<FilePath> reports;
		private final String jobName;
		private final String buildId;
		private final String jenkinsRootUrl;
		private final HPRunnerType hpRunnerType;
		private FilePath filePath;
		private List<ModuleDetection> moduleDetection;
		private long buildStarted;
		private FilePath workspace;
		private boolean stripPackageAndClass;
		private String sharedCheckOutDirectory;

		//this class is run on master and JUnitXmlIterator is runnning on slave.
		//this object pass some master2slave data
		private Object additionalContext;
		private String buildRootDir;

		public GetJUnitTestResults(Run<?, ?> build, List<FilePath> reports, boolean stripPackageAndClass, HPRunnerType hpRunnerType, String jenkinsRootUrl) throws IOException, InterruptedException {
			this.reports = reports;
			this.filePath = new FilePath(build.getRootDir()).createTempFile(getClass().getSimpleName(), null);
			this.buildStarted = build.getStartTimeInMillis();
			this.workspace = BuildHandlerUtils.getWorkspace(build);
			this.stripPackageAndClass = stripPackageAndClass;
			this.hpRunnerType = hpRunnerType;
			this.jenkinsRootUrl = jenkinsRootUrl;
			this.buildRootDir = build.getRootDir().getCanonicalPath();
			this.sharedCheckOutDirectory = CheckOutSubDirEnvContributor.getSharedCheckOutDirectory(build.getParent());

			this.jobName = build.getParent().getName();
			this.buildId = build.getId();
			moduleDetection = Arrays.asList(
					new MavenBuilderModuleDetection(build),
					new MavenSetModuleDetection(build),
					new ModuleDetection.Default());


			if (HPRunnerType.UFT.equals(hpRunnerType)) {

				//extract folder names for created tests
				String reportFolder = buildRootDir + "/archive/UFTReport";
				Set<String> testFolderNames = new HashSet<>();
				File reportFolderFile = new File(reportFolder);
				if (reportFolderFile.exists()) {
					File[] children = reportFolderFile.listFiles();
					if (children != null) {
						for (File child : children) {
							testFolderNames.add(child.getName());
						}
					}
				}
				additionalContext = testFolderNames;
			}
			if (HPRunnerType.StormRunner.equals(hpRunnerType)) {
				try {
					File file = new File(build.getRootDir(), "log");
					Path path = Paths.get(file.getPath());
					additionalContext = Files.readAllLines(path, StandardCharsets.UTF_8);
				} catch (Exception e) {
					logger.error("Failed to add log file for StormRunner :" + e.getMessage());
				}
			}
		}

		@Override
		public FilePath invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
			OutputStream os = filePath.write();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			try {
				for (FilePath report : reports) {
					JUnitXmlIterator iterator = new JUnitXmlIterator(report.read(), moduleDetection, workspace, sharedCheckOutDirectory, jobName, buildId, buildStarted, stripPackageAndClass, hpRunnerType, jenkinsRootUrl, additionalContext);
					while (iterator.hasNext()) {
						oos.writeObject(iterator.next());
					}
				}
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}
			os.flush();

			oos.close();
			return filePath;
		}

		@Override
		public void checkRoles(RoleChecker roleChecker) throws SecurityException {
			roleChecker.check(this, Role.UNKNOWN);
		}
	}

	/*
	 * To be used in tests only.
	 */
	public void _setResultFieldsDetectionService(ResultFieldsDetectionService detectionService) {
		this.resultFieldsDetectionService = detectionService;
	}
}
