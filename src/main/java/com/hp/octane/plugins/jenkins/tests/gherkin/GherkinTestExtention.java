package com.hp.octane.plugins.jenkins.tests.gherkin;

import com.hp.octane.plugins.jenkins.actions.cucumber.CucumberTestResultsAction;
import com.hp.octane.plugins.jenkins.tests.HPRunnerType;
import com.hp.octane.plugins.jenkins.tests.MqmTestsExtension;
import com.hp.octane.plugins.jenkins.tests.TestProcessingException;
import com.hp.octane.plugins.jenkins.tests.TestResultContainer;
import com.hp.octane.plugins.jenkins.tests.testResult.TestResult;
import hudson.Extension;
import hudson.model.Run;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Extension
public class GherkinTestExtention extends MqmTestsExtension {
    private static Logger logger = LogManager.getLogger(GherkinTestExtention.class);

    @Override
    public boolean supports(Run<?, ?> build) throws IOException, InterruptedException {
        if (build.getAction(CucumberTestResultsAction.class) != null) {
            logger.debug("CucumberTestResultsAction found, gherkin results expected");
            return true;
        } else {
            logger.debug("CucumberTestResultsAction not found, no gherkin results expected");
            return false;
        }
    }

    @Override
    public TestResultContainer getTestResults(Run<?, ?> build, HPRunnerType hpRunnerType, String jenkinsRootUrl) throws TestProcessingException, IOException, InterruptedException {
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
