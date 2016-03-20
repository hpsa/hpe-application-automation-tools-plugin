// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.junit;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.tests.*;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFields;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFieldsDetectionService;
import com.hp.octane.plugins.jenkins.tests.impl.ObjectStreamIterator;
import com.hp.octane.plugins.jenkins.tests.xml.AbstractXmlIterator;
import hudson.Extension;
import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;
import hudson.tasks.test.AbstractTestResultAction;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Extension
public class JUnitExtension extends MqmTestsExtension {

	private static Logger logger = Logger.getLogger(JUnitExtension.class.getName());

	private static final String JUNIT_RESULT_XML = "junitResult.xml"; // NON-NLS

	@Inject
	ResultFieldsDetectionService resultFieldsDetectionService;

	public boolean supports(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		if (build.getAction(AbstractTestResultAction.class) != null) {
			logger.fine("AbstractTestResultAction found, JUnit results expected");
			return true;
		} else {
			logger.fine("AbstractTestResultAction not found, no JUnit results expected");
			return false;
		}
	}

	@Override
	public TestResultContainer getTestResults(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
		logger.fine("Collecting JUnit results");
		FilePath resultFile = new FilePath(build.getRootDir()).child(JUNIT_RESULT_XML);
		if (resultFile.exists()) {
			logger.fine("JUnit result report found");
			ResultFields detectedFields = resultFieldsDetectionService.getDetectedFields(build);
			FilePath filePath = build.getWorkspace().act(new GetJUnitTestResults(build, Arrays.asList(resultFile), shallStripPackageAndClass(detectedFields)));
			return new TestResultContainer(new ObjectStreamIterator<TestResult>(filePath, true), detectedFields);
		} else {
			//avoid java.lang.NoClassDefFoundError when maven plugin is not present
			if ("hudson.maven.MavenModuleSetBuild".equals(build.getClass().getName())) {
				logger.fine("MavenModuleSetBuild detected, looking for results in maven modules");

				List<FilePath> resultFiles = new LinkedList<FilePath>();
				Map<MavenModule, MavenBuild> moduleLastBuilds = ((MavenModuleSetBuild) build).getModuleLastBuilds();
				for (MavenBuild mavenBuild : moduleLastBuilds.values()) {
					AbstractTestResultAction action = mavenBuild.getAction(AbstractTestResultAction.class);
					if (action != null) {
						FilePath moduleResultFile = new FilePath(mavenBuild.getRootDir()).child(JUNIT_RESULT_XML);
						if (moduleResultFile.exists()) {
							logger.fine("Found results in " + mavenBuild.getFullDisplayName());
							resultFiles.add(moduleResultFile);
						}
					}
				}
				if (!resultFiles.isEmpty()) {
					ResultFields detectedFields = resultFieldsDetectionService.getDetectedFields(build);
					FilePath filePath = build.getWorkspace().act(new GetJUnitTestResults(build, resultFiles, shallStripPackageAndClass(detectedFields)));
					return new TestResultContainer(new ObjectStreamIterator<TestResult>(filePath, true), detectedFields);
				}
			}
			logger.fine("No JUnit result report found");
			return null;
		}
	}

	private boolean shallStripPackageAndClass(ResultFields resultFields) {
		if (resultFields == null) {
			return false;
		}
		return resultFields.equals(new ResultFields("UFT", "UFT", null));
	}

	private static class GetJUnitTestResults implements FilePath.FileCallable<FilePath> {

		private final List<FilePath> reports;
		private FilePath filePath;
		private List<ModuleDetection> moduleDetection;
		private long buildStarted;
		private FilePath workspace;
		private boolean stripPackageAndClass;

		public GetJUnitTestResults(AbstractBuild<?, ?> build, List<FilePath> reports, boolean stripPackageAndClass) throws IOException, InterruptedException {
			this.reports = reports;
			this.filePath = new FilePath(build.getRootDir()).createTempFile(getClass().getSimpleName(), null);
			this.buildStarted = build.getStartTimeInMillis();
			this.workspace = build.getWorkspace();
			this.stripPackageAndClass = stripPackageAndClass;

			moduleDetection = Arrays.asList(
					new MavenBuilderModuleDetection(build),
					new MavenSetModuleDetection(build),
					new ModuleDetection.Default());
		}

		@Override
		public FilePath invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
			OutputStream os = filePath.write();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			try {
				for (FilePath report : reports) {
					JUnitXmlIterator iterator = new JUnitXmlIterator(report.read(), stripPackageAndClass);
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

		private static long parseTime(String timeString) {
			String time = timeString.replace(",", "");
			try {
				float seconds = Float.parseFloat(time);
				return (long) (seconds * 1000);
			} catch (NumberFormatException e) {
				try {
					return new DecimalFormat().parse(time).longValue();
				} catch (ParseException ex) {
					logger.fine("Unable to parse test duration: " + timeString);
				}
			}
			return 0;
		}

		private class JUnitXmlIterator extends AbstractXmlIterator<TestResult> {

			private boolean stripPackageAndClass;
			private String moduleName;
			private String packageName;
			private String className;
			private String testName;
			private long duration;
			private TestResultStatus status;
			private String stackTraceStr;
			private String errorType;
			private String errorMsg;

			public JUnitXmlIterator(InputStream read, boolean stripPackageAndClass) throws XMLStreamException {
				super(read);
				this.stripPackageAndClass = stripPackageAndClass;
			}

			@Override
			protected void onEvent(XMLEvent event) throws XMLStreamException, IOException, InterruptedException {
				if (event instanceof StartElement) {
					StartElement element = (StartElement) event;
					String localName = element.getName().getLocalPart();
					if ("file".equals(localName)) {  // NON-NLS
						String path = readNextValue();
						for (ModuleDetection detection : moduleDetection) {
							moduleName = detection.getModule(new FilePath(new File(path)));
							if (moduleName != null) {
								break;
							}
						}
					} else if ("case".equals(localName)) { // NON-NLS
						packageName = "";
						className = "";
						testName = "";
						duration = 0;
						status = TestResultStatus.PASSED;
						stackTraceStr = "";
						errorType = "";
						errorMsg = "";
					} else if ("className".equals(localName)) { // NON-NLS
						String fqn = readNextValue();
						int p = fqn.lastIndexOf(".");
						className = fqn.substring(p + 1);
						if (p > 0) {
							packageName = fqn.substring(0, p);
						} else {
							packageName = "";
						}
					} else if ("testName".equals(localName)) { // NON-NLS
						testName = readNextValue();
						if (testName.startsWith(workspace.getRemote())) {
							// if workspace is prefix of the method name, cut it off
							// currently this handling is needed for UFT tests
							testName = testName.substring(workspace.getRemote().length()).replaceAll("^[/\\\\]", "");
						}
					} else if ("duration".equals(localName)) { // NON-NLS
						duration = parseTime(readNextValue());
					} else if ("skipped".equals(localName)) { // NON-NLS
						if ("true".equals(readNextValue())) { // NON-NLS
							status = TestResultStatus.SKIPPED;
						}
					} else if ("failedSince".equals(localName)) { // NON-NLS
						if (!"0".equals(readNextValue()) && !TestResultStatus.SKIPPED.equals(status)) {
							status = TestResultStatus.FAILED;
						}
					} else if ("errorStackTrace".equals(localName)) { // NON-NLS
						status = TestResultStatus.FAILED;
						stackTraceStr = readElementText();
						errorType = stackTraceStr.substring(0, stackTraceStr.indexOf("\n\t"));
					} else if ("errorDetails".equals(localName)) { // NON-NLS
						status = TestResultStatus.FAILED;
						errorMsg = readElementText();
						errorType = stackTraceStr.substring(0, stackTraceStr.indexOf(":"));

					}
				} else if (event instanceof EndElement) {
					EndElement element = (EndElement) event;
					String localName = element.getName().getLocalPart();

					if ("case".equals(localName)) { // NON-NLS
						TestError testError = new TestError(stackTraceStr, errorType, errorMsg);
						if (stripPackageAndClass) {
							//workaround only for UFT - we do not want packageName="All-Tests" and className="&lt;None>" as it comes from JUnit report
							addItem(new TestResult(moduleName, "", "", testName, status, duration, buildStarted, testError));
						} else {
							addItem(new TestResult(moduleName, packageName, className, testName, status, duration, buildStarted, testError));
						}
					}
				}
			}
		}
	}

	/*
	 * To be used in tests only.
	 */
	public void _setResultFieldsDetectionService(ResultFieldsDetectionService detectionService) {
		this.resultFieldsDetectionService = detectionService;
	}
}
