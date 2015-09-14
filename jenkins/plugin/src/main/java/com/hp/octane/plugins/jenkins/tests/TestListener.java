// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.tests.build.BuildHandlerUtils;
import com.hp.octane.plugins.jenkins.tests.xml.TestResultXmlWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;

import javax.xml.stream.XMLStreamException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestListener {

    static final String TEST_RESULT_FILE = "mqmTests.xml";

    private static Logger logger = Logger.getLogger(TestListener.class.getName());

    private TestResultQueue queue;

    public void processBuild(AbstractBuild build) {
        FilePath resultPath = new FilePath(new FilePath(build.getRootDir()), TEST_RESULT_FILE);
        TestResultXmlWriter resultWriter = new TestResultXmlWriter(resultPath, build);
        boolean success = false;
        boolean hasTests = false;
        try {
            for (MqmTestsExtension ext: MqmTestsExtension.all()) {
                try {
                    if (ext.supports(build)) {
                        TestResultContainer testResultContainer = ext.getTestResults(build);
                        if (testResultContainer != null && testResultContainer.getIterator().hasNext()) {
                            resultWriter.add(testResultContainer);
                            hasTests = true;
                        }
                    }
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
