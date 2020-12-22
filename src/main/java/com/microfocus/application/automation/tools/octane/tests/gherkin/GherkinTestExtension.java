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

package com.microfocus.application.automation.tools.octane.tests.gherkin;

import com.microfocus.application.automation.tools.octane.actions.cucumber.CucumberTestResultsAction;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.tests.OctaneTestsExtension;
import com.microfocus.application.automation.tools.octane.tests.TestProcessingException;
import com.microfocus.application.automation.tools.octane.tests.TestResultContainer;
import com.microfocus.application.automation.tools.octane.tests.testResult.TestResult;
import hudson.Extension;
import hudson.model.Run;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Extension
public class GherkinTestExtension extends OctaneTestsExtension {
	private static Logger logger = SDKBasedLoggerProvider.getLogger(GherkinTestExtension.class);

	@Override
	public boolean supports(Run<?, ?> build) {
		if (build.getAction(CucumberTestResultsAction.class) != null) {
			logger.debug("CucumberTestResultsAction found, gherkin results expected");
			return true;
		} else {
			logger.debug("CucumberTestResultsAction not found, no gherkin results expected");
			return false;
		}
	}

	@Override
	public TestResultContainer getTestResults(Run<?, ?> build, String jenkinsRootUrl) throws
			TestProcessingException, IOException, InterruptedException {
		try {
			List<TestResult> testResults = GherkinTestResultsCollector.collectGherkinTestsResults(build.getRootDir());
			return new TestResultContainer(testResults.iterator(), null);
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new TestProcessingException("Error while processing gherkin test results", e);
		}
	}
}
