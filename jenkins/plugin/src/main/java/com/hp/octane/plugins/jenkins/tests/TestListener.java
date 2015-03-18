// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.tests.xml.TestResultXmlWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestListener {

    static final String TEST_RESULT_FILE = "mqmTests.xml";

    private static Logger logger = Logger.getLogger(TestListener.class.getName());

    @Inject
    private TestResultQueueImpl queue;

    public void processBuild(AbstractBuild build) {
        FilePath resultPath = new FilePath(new FilePath(build.getRootDir()), TEST_RESULT_FILE);
        TestResultXmlWriter resultWriter = new TestResultXmlWriter(resultPath, build);
        try {
            for (MqmTestsExtension ext: MqmTestsExtension.all()) {
                try {
                    if (ext.supports(build)) {
                        resultWriter.add(ext.getTestResults(build));
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error processing test results in " + ext.getClass().getName(), e);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted processing test results in " + ext.getClass().getName(), e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            try {
                resultWriter.close();
                queue.add(build.getProject().getName(), build.getNumber());
            } catch (XMLStreamException e) {
                logger.log(Level.SEVERE, "Error processing test results", e);
            }
        }
    }
}
