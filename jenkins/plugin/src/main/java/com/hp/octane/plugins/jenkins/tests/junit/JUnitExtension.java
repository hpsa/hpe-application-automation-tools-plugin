// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.junit;

import com.hp.octane.plugins.jenkins.tests.ModuleDetection;
import com.hp.octane.plugins.jenkins.tests.xml.AbstractXmlIterator;
import com.hp.octane.plugins.jenkins.tests.MqmTestsExtension;
import com.hp.octane.plugins.jenkins.tests.maven.FreeStyleModuleDetection;
import com.hp.octane.plugins.jenkins.tests.impl.ObjectStreamIterator;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import com.hp.octane.plugins.jenkins.tests.maven.MavenSetModuleDetection;
import hudson.Extension;
import hudson.FilePath;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@Extension
public class JUnitExtension extends MqmTestsExtension {

    private static Logger logger = Logger.getLogger(JUnitExtension.class.getName());

    private static final String JUNIT_RESULT_XML = "junitResult.xml"; // NON-NLS

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
    public Iterator<TestResult> getTestResults(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        logger.fine("Collecting JUnit results");
        FilePath resultFile = new FilePath(build.getRootDir()).child(JUNIT_RESULT_XML);
        if (resultFile.exists()) {
            logger.fine("JUnit result report found");
            FilePath filePath = build.getWorkspace().act(new GetJUnitTestResults(build, resultFile));
            return new ObjectStreamIterator<TestResult>(filePath, true);
        } else {
            return Collections.emptyListIterator();
        }
    }

    private static class GetJUnitTestResults implements FilePath.FileCallable<FilePath>  {

        private final FilePath report;
        private FilePath filePath;
        private List<ModuleDetection> moduleDetection;

        public GetJUnitTestResults(AbstractBuild<?, ?> build, FilePath report) throws IOException, InterruptedException {
            this.report = report;
            this.filePath = new FilePath(build.getRootDir()).createTempFile(getClass().getSimpleName(), null);

            moduleDetection = Arrays.asList(
                    new FreeStyleModuleDetection(build),
                    new MavenSetModuleDetection(build),
                    new ModuleDetection.Default());
        }

        @Override
        public FilePath invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            OutputStream os = filePath.write();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            try {
                JUnitXmlIterator iterator = new JUnitXmlIterator(report.read());
                while (iterator.hasNext()) {
                    oos.writeObject(iterator.next());
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
            String time = timeString.replace(",","");
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

            private String moduleName;
            private String packageName;
            private String className;
            private String testName;
            private long duration;
            private TestResultStatus status;

            public JUnitXmlIterator(InputStream read) throws XMLStreamException {
                super(read);
            }

            @Override
            protected void onEvent(XMLEvent event) throws XMLStreamException, IOException, InterruptedException {
                if (event instanceof StartElement) {
                    StartElement element = (StartElement) event;
                    String localName = element.getName().getLocalPart();
                    if ("file".equals(localName)) {  // NON-NLS
                        String path = readNextValue();
                        for (ModuleDetection detection: moduleDetection) {
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
                    } else if (("errorStackTrace".equals(localName) || "errorDetails".equals(localName))) { // NON-NLS
                        status = TestResultStatus.FAILED;
                    }
                } else if (event instanceof EndElement) {
                    EndElement element = (EndElement) event;
                    String localName = element.getName().getLocalPart();

                    if ("case".equals(localName)) { // NON-NLS
                        addItem(new TestResult(moduleName, packageName, className, testName, status, duration));
                    }
                }
            }
        }
    }
}
