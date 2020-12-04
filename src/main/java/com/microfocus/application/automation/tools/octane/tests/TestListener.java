/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.tests;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import com.microfocus.application.automation.tools.octane.tests.xml.TestResultXmlWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLStreamException;

/**
 * Jenkins events life cycle listener for processing test results on build completed
 */
@Extension
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872"})
public class TestListener {
	private static Logger logger = SDKBasedLoggerProvider.getLogger(TestListener.class);

	public static final String TEST_RESULT_FILE = "mqmTests.xml";


	public boolean processBuild(Run run) {
		FilePath resultPath = new FilePath(new FilePath(run.getRootDir()), TEST_RESULT_FILE);
		TestResultXmlWriter resultWriter = new TestResultXmlWriter(resultPath, run);
		boolean success = true;
		boolean hasTests = false;
		String jenkinsRootUrl = Jenkins.get().getRootUrl();

		try {
			for (OctaneTestsExtension ext : OctaneTestsExtension.all()) {
				if (ext.supports(run)) {
					TestResultContainer testResultContainer = ext.getTestResults(run, jenkinsRootUrl);
					if (testResultContainer != null && testResultContainer.getIterator().hasNext()) {
						resultWriter.writeResults(testResultContainer);
						hasTests = true;
					}
				}
			}
		} catch (Throwable t) {
			success = false;
			logger.error("failed to process test results", t);
		} finally {
			try {
				resultWriter.close();

				// we don't push individual maven module results (although we create the file for future use)
				if (!"hudson.maven.MavenBuild".equals(run.getClass().getName())) {
					if (success && hasTests) {
						String projectFullName = BuildHandlerUtils.getJobCiId(run);
						String parents = BuildHandlerUtils.getRootJobCiIds(run);
						logger.info("enqueued build '" + projectFullName + " #" + run.getNumber() + "' for test result submission");
						if (projectFullName != null) {
							OctaneSDK.getClients().forEach(octaneClient ->
									octaneClient.getTestsService().enqueuePushTestsResult(projectFullName, String.valueOf(run.getNumber()), parents));
						}
					}
				}
			} catch (XMLStreamException xmlse) {
				success = false;
				logger.error("failed to finalize test results processing", xmlse);
			}
		}
		return success && hasTests;
	}
}
