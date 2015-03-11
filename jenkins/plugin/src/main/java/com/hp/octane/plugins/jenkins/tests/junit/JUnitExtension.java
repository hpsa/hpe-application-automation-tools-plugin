// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.junit;

import com.hp.octane.plugins.jenkins.tests.impl.AbstractXmlIterator;
import com.hp.octane.plugins.jenkins.tests.MqmTestsExtension;
import com.hp.octane.plugins.jenkins.tests.impl.ObjectStreamIterator;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
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
import java.util.Iterator;
import java.util.LinkedList;
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
        FilePath[] list = new FilePath(build.getRootDir()).list("**/" + JUNIT_RESULT_XML);
        logger.fine("Found " + list.length + " reports");

        FilePath filePath = build.getWorkspace().act(new GetJUnitTestResults(build, list));
        return new ObjectStreamIterator<TestResult>(filePath, true);
    }

    private static class GetJUnitTestResults implements FilePath.FileCallable<FilePath>  {

        private final FilePath[] reports;
        private FilePath filePath;
        private List<FilePath> pomDirs;
        private FilePath workspace;

        public GetJUnitTestResults(AbstractBuild<?, ?> build, FilePath[] reports) throws IOException, InterruptedException {
            this.reports = reports;
            this.filePath = new FilePath(build.getRootDir()).createTempFile(getClass().getSimpleName(), null);

            workspace = build.getWorkspace();
            pomDirs = listPomDirectories(build);
        }

        @Override
        public FilePath invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            OutputStream os = filePath.write();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            for (FilePath report: reports) {
                try {
                    JUnitXmlIterator iterator = new JUnitXmlIterator(report.read());
                    while (iterator.hasNext()) {
                        oos.writeObject(iterator.next());
                    }
                } catch (XMLStreamException e) {
                    throw new IOException(e);
                }
                os.flush();
            }

            oos.close();
            return filePath;
        }

        @Override
        public void checkRoles(RoleChecker roleChecker) throws SecurityException {
            roleChecker.check(this, Role.UNKNOWN);
        }

        private boolean childOf(FilePath parent, FilePath child) {
            while (child != null) {
                if (parent.equals(child)) {
                    return true;
                }
                child = child.getParent();
            }
            return false;
        }

        private String locatePom(FilePath filePath) throws IOException, InterruptedException {
            while (filePath != null) {
                FilePath parentPath = filePath.getParent();
                FilePath pomPath = new FilePath(parentPath, "pom.xml");
                if (pomPath.exists()) {
                    return parentPath.getRemote().substring(workspace.getRemote().length());
                }
                filePath = parentPath;
            }
            return "";
        }

        private List<FilePath> listPomDirectories(AbstractBuild build) {
            List<FilePath> ret = new LinkedList<FilePath>();
            if (build.getProject() instanceof FreeStyleProject) {
                for (Builder builder: ((FreeStyleProject) build.getProject()).getBuilders()) {
                    if (builder instanceof Maven) {
                        Maven maven = (Maven) builder;
                        if (maven.pom != null) {
                            if (maven.pom.endsWith("/pom.xml") || maven.pom.endsWith("\\pom.xml")) {
                                ret.add(new FilePath(workspace, maven.pom.substring(0, maven.pom.length() - 8)));
                                continue;
                            } else {
                                int p = maven.pom.lastIndexOf(File.separatorChar);
                                if (p > 0) {
                                    ret.add(new FilePath(workspace, maven.pom.substring(0, p)));
                                    continue;
                                }
                            }
                        }
                        ret.add(workspace);
                    }
                }
            }
            return ret;
        }

        private String guessMavenModule(FilePath resultFile) throws IOException, InterruptedException {
            for (FilePath pomDir: pomDirs) {
                if (childOf(pomDir, resultFile)) {
                    return locatePom(resultFile);
                }
            }
            // unable to determine module
            return "";
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
                        moduleName = guessMavenModule(new FilePath(new File(path)));
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
