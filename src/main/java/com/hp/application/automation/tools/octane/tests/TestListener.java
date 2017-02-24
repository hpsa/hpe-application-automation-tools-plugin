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

package com.hp.application.automation.tools.octane.tests;

import com.google.inject.Inject;
import com.hp.application.automation.tools.octane.ResultQueue;
import com.hp.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.hp.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import com.hp.application.automation.tools.octane.tests.detection.UFTExtension;
import com.hp.application.automation.tools.octane.tests.xml.TestResultXmlWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.util.List;

@Extension
@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872"})
public class TestListener {
	private static Logger logger = LogManager.getLogger(TestListener.class);

	static final String TEST_RESULT_FILE = "mqmTests.xml";
	public static final String JENKINS_STORM_TEST_RUNNER_CLASS = "com.hpe.sr.plugins.jenkins.StormTestRunner";

	private ResultQueue queue;

	public void processBuild(AbstractBuild build, TaskListener listener) {

		FilePath resultPath = new FilePath(new FilePath(build.getRootDir()), TEST_RESULT_FILE);
		TestResultXmlWriter resultWriter = new TestResultXmlWriter(resultPath, build);
		boolean success = false;
		boolean hasTests = false;
		String jenkinsRootUrl = Jenkins.getInstance().getRootUrl();
		HPRunnerType hpRunnerType = HPRunnerType.NONE;
		List<Builder> builders = JobProcessorFactory.getFlowProcessor(build.getProject()).tryGetBuilders();
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
						TestResultContainer testResultContainer = ext.getTestResults(build, hpRunnerType, jenkinsRootUrl);
						if (testResultContainer != null && testResultContainer.getIterator().hasNext()) {
							resultWriter.writeResults(testResultContainer);
							hasTests = true;
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
	public void setTestResultQueue(TestAbstractResultQueue queue) {
		this.queue = queue;
	}

	/*
	 * To be used in tests only.
	 */
	public void _setTestResultQueue(ResultQueue queue) {
		this.queue = queue;
	}
}
