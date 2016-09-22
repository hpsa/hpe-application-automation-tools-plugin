// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import com.hp.octane.plugins.jenkins.tests.build.BuildHandlerUtils;
import com.hp.octane.plugins.jenkins.tests.detection.UFTExtension;
import com.hp.octane.plugins.jenkins.tests.gherkin.GherkinTestResultsCollector;
import com.hp.octane.plugins.jenkins.tests.junit.JUnitExtension;
import com.hp.octane.plugins.jenkins.tests.xml.TestResultXmlWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestListener {

    static final String TEST_RESULT_FILE = "mqmTests.xml";
    public static final String JENKINS_STORM_TEST_RUNNER_CLASS = "com.hpe.sr.plugins.jenkins.StormTestRunner";

    private static Logger logger = Logger.getLogger(TestListener.class.getName());

    private TestResultQueue queue;

    public void processBuild(AbstractBuild build, TaskListener listener) {

        FilePath resultPath = new FilePath(new FilePath(build.getRootDir()), TEST_RESULT_FILE);
        TestResultXmlWriter resultWriter = new TestResultXmlWriter(resultPath, build);
        boolean success = false;
        boolean hasTests = false;
        String jenkinsRootUrl = Jenkins.getInstance().getRootUrl();
        JUnitExtension.HPRunnerType hpRunnerType = JUnitExtension.HPRunnerType.NONE;
        List<Builder> builders = AbstractProjectProcessor.getFlowProcessor(build.getProject()).tryGetBuilders();
        if (builders != null) {
            for (Builder builder : builders) {
                if (builder.getClass().getName().equals(JENKINS_STORM_TEST_RUNNER_CLASS)) {
                    hpRunnerType = JUnitExtension.HPRunnerType.StormRunner;
                    break;
                }
                if (builder.getClass().getName().equals(UFTExtension.RUN_FROM_FILE_BUILDER) || builder.getClass().getName().equals(UFTExtension.RUN_FROM_ALM_BUILDER)) {
                    hpRunnerType = JUnitExtension.HPRunnerType.UFT;
                    break;
                }
            }
        }

        try {
            for (MqmTestsExtension ext : MqmTestsExtension.all()) {
                try {
                    if (ext.supports(build)) {
                        GherkinTestResultsCollector gherkinResultsCollector = new GherkinTestResultsCollector(build.getRootDir());
                        List<CustomTestResult> gherkinTestResults = gherkinResultsCollector.getGherkinTestsResults();
                        if (gherkinTestResults != null && gherkinTestResults.size() > 0) {
                            resultWriter.setCustomTestResults(gherkinTestResults);
                            hasTests = true;
                        }

                        TestResultContainer testResultContainer = ext.getTestResults(build, hpRunnerType, jenkinsRootUrl);
                        if (testResultContainer != null && testResultContainer.getIterator().hasNext()) {
                            resultWriter.setTestResultContainer(testResultContainer, gherkinResultsCollector);
                            hasTests = true;
                        }

                        if (hasTests) {
                            resultWriter.writeResults();
                        }
                    }
                } catch (IllegalArgumentException e) {
                    listener.error(e.getMessage());
                    if (!build.getResult().isWorseOrEqualTo(Result.UNSTABLE)) {
                        build.setResult(Result.UNSTABLE);
                    }
                    return;
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted processing test results in " + ext.getClass().getName(), e);
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    // extensibility involved: catch both checked and RuntimeExceptions
                    logger.log(Level.SEVERE, "Error processing test results in " + ext.getClass().getName(), e);
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
            } catch (XMLStreamException e) {
                logger.log(Level.SEVERE, "Error processing test results", e);
            }
        }
    }

    private boolean isUFTRunner(AbstractBuild build) {
        UFTExtension uftExtension = new UFTExtension();
        return uftExtension.detect(build) != null;
    }

    @Inject
    public void setTestResultQueue(TestResultQueueImpl queue) {
        this.queue = queue;
    }

    /*
     * To be used in tests only.
     */
    public void _setTestResultQueue(TestResultQueue queue) {
        this.queue = queue;
    }
}
