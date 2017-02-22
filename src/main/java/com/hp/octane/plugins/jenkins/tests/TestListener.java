// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.ResultQueue;
import com.hp.octane.plugins.jenkins.model.processors.projects.JobProcessorFactory;
import com.hp.octane.plugins.jenkins.tests.build.BuildHandlerUtils;
import com.hp.octane.plugins.jenkins.tests.detection.UFTExtension;
import com.hp.octane.plugins.jenkins.tests.xml.TestResultXmlWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.util.List;

@Extension
public class TestListener {
	private static Logger logger = LogManager.getLogger(TestListener.class);

	static final String TEST_RESULT_FILE = "mqmTests.xml";
	public static final String JENKINS_STORM_TEST_RUNNER_CLASS = "com.hpe.sr.plugins.jenkins.StormTestRunner";

	private ResultQueue queue;

	public void processBuild(Run build, TaskListener listener) {

		FilePath resultPath = new FilePath(new FilePath(build.getRootDir()), TEST_RESULT_FILE);
		TestResultXmlWriter resultWriter = new TestResultXmlWriter(resultPath, build);
		boolean success = false;
		boolean hasTests = false;
		String jenkinsRootUrl = Jenkins.getInstance().getRootUrl();
		HPRunnerType hpRunnerType = HPRunnerType.NONE;
		List<Builder> builders = JobProcessorFactory.getFlowProcessor(build.getParent()).tryGetBuilders();
		if (builders != null) {
			for (Builder builder : builders) {
				if (builder.getClass().getName().equals(JENKINS_STORM_TEST_RUNNER_CLASS)) {
					hpRunnerType = HPRunnerType.StormRunner;
					break;
				}
				if (builder.getClass().getName().equals(UFTExtension.RUN_FROM_FILE_BUILDER) || builder.getClass().getName().equals(UFTExtension.RUN_FROM_ALM_BUILDER)) {
					hpRunnerType = HPRunnerType.UFT;
					break;
				}
			}
		}

		try {
			for (MqmTestsExtension ext : MqmTestsExtension.all()) {
				try {
					if (ext.supports(build)) {

						List<Run> buildsList = BuildHandlerUtils.getBuildPerWorkspaces(build);
						for(Run buildX : buildsList){
							TestResultContainer testResultContainer = ext.getTestResults(buildX, hpRunnerType, jenkinsRootUrl);
							if (testResultContainer != null && testResultContainer.getIterator().hasNext()) {
								resultWriter.writeResults(testResultContainer);
								hasTests = true;
							}
						}
					}
				} catch (IllegalArgumentException e) {
					listener.error(e.getMessage());
					if (!build.getResult().isWorseOrEqualTo(Result.UNSTABLE)) {
						build.setResult(Result.UNSTABLE);
					}
					return;
				} catch (InterruptedException ie) {
					logger.error("Interrupted processing test results in " + ext.getClass().getName(), ie);
					Thread.currentThread().interrupt();
					return;
				} catch (Exception e) {
					// extensibility involved: catch both checked and RuntimeExceptions
					logger.error("Error processing test results in " + ext.getClass().getName(), e);
					return;
				}
			}
			success = true;
		} finally {
			try {
				resultWriter.close();
				if (success && hasTests) {
					String projectFullName = BuildHandlerUtils.getProjectFullName(build);
					if (projectFullName != null) {
						queue.add(projectFullName, build.getNumber());
					}
				}
			} catch (XMLStreamException xmlse) {
				logger.error("Error processing test results", xmlse);
			}
		}
	}

	private boolean isUFTRunner(AbstractBuild build) {
		UFTExtension uftExtension = new UFTExtension();
		return uftExtension.detect(build) != null;
	}

	@Inject
	public void setTestResultQueue(TestResultQueue queue) {
		this.queue = queue;
	}

	/*
	 * To be used in tests only.
	 */
	public void _setTestResultQueue(ResultQueue queue) {
		this.queue = queue;
	}
}
