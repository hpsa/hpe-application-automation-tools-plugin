package com.hp.octane.plugins.jenkins.tests.junit;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.tests.Property;
import com.hp.octane.integrations.dto.tests.TestSuite;
import com.hp.octane.plugins.jenkins.tests.TestError;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import com.hp.octane.plugins.jenkins.tests.xml.AbstractXmlIterator;
import hudson.FilePath;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

public class JUnitXmlIterator extends AbstractXmlIterator<TestResult> {
    private static Logger logger = Logger.getLogger(JUnitXmlIterator.class.getName());
    public static final String DASHBOARD_URL = "dashboardUrl";
    private final FilePath workspace;
    private final long buildStarted;
    private final String buildId;
    private final String jobName;
    private final JUnitExtension.HPRunnerType hpRunnerType;
    private boolean stripPackageAndClass;
    private String moduleName;
    private String packageName;
    private String className;
    private String testName;
    private long duration;
    private TestResultStatus status;
    private String stackTraceStr;
    private String errorType;
    private String errorMsg;
    private String externalURL;
    private List<ModuleDetection> moduleDetection;
    private String jenkinsRootUrl;

    public JUnitXmlIterator(InputStream read, List<ModuleDetection> moduleDetection, FilePath workspace, String jobName, String buildId, long buildStarted, boolean stripPackageAndClass, JUnitExtension.HPRunnerType hpRunnerType, String jenkinsRootUrl) throws XMLStreamException {
        super(read);
        this.stripPackageAndClass = stripPackageAndClass;
        this.moduleDetection = moduleDetection;
        this.workspace = workspace;
        this.buildId = buildId;
        this.jobName = jobName;
        this.buildStarted = buildStarted;
        this.hpRunnerType = hpRunnerType;
        this.jenkinsRootUrl = jenkinsRootUrl;
    }

    private static long parseTime(String timeString) {
        String time = timeString.replace(",", "");
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

    private String getStormRunnerURL(String path) {

        String srUrl = null;
        File srReport = new File(path);
        if (srReport.exists()) {
            TestSuite testSuite = DTOFactory.getInstance().dtoFromXmlFile(srReport, TestSuite.class);

            for (Property property : testSuite.getProperties()) {
                if (property.getPropertyName().equals(DASHBOARD_URL)) {
                    srUrl = property.getPropertyValue();
                    break;
                }
            }
        }
        return srUrl;
    }

    @Override
    protected void onEvent(XMLEvent event) throws XMLStreamException, IOException, InterruptedException {
        if (event instanceof StartElement) {
            StartElement element = (StartElement) event;
            String localName = element.getName().getLocalPart();
            if ("file".equals(localName)) {  // NON-NLS
                String path = readNextValue();
                for (ModuleDetection detection : moduleDetection) {
                    moduleName = detection.getModule(new FilePath(new File(path)));
                    if (moduleName != null) {
                        break;
                    }
                }
                if (hpRunnerType.equals(JUnitExtension.HPRunnerType.StormRunner)) {
                    logger.severe("HP Runner: " + hpRunnerType);
                    externalURL = getStormRunnerURL(path);
                }
            } else if ("case".equals(localName)) { // NON-NLS
                packageName = "";
                className = "";
                testName = "";
                duration = 0;
                status = TestResultStatus.PASSED;
                stackTraceStr = "";
                errorType = "";
                errorMsg = "";
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
                if (testName.startsWith(workspace.getRemote())) {
                    // if workspace is prefix of the method name, cut it off
                    // currently this handling is needed for UFT tests
                    testName = testName.substring(workspace.getRemote().length()).replaceAll("^[/\\\\]", "");
                }
                if (hpRunnerType.equals(JUnitExtension.HPRunnerType.UFT)) {
                    externalURL = jenkinsRootUrl + "job/" + jobName + "/" + buildId + "/artifact/UFTReport/" + testName + "/run_results.html";
                }
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
            } else if ("errorStackTrace".equals(localName)) { // NON-NLS
                status = TestResultStatus.FAILED;
                stackTraceStr = "";
                if (peek() instanceof Characters) {
                    stackTraceStr = readNextValue();
                    int index = stackTraceStr.indexOf("at ");
                    if (index >= 0) {
                        errorType = stackTraceStr.substring(0, index);
                    }
                }
            } else if ("errorDetails".equals(localName)) { // NON-NLS
                status = TestResultStatus.FAILED;
                errorMsg = readNextValue();
                int index = stackTraceStr.indexOf(":");
                if (index >= 0) {
                    errorType = stackTraceStr.substring(0, index);
                }

            }
        } else if (event instanceof EndElement) {
            EndElement element = (EndElement) event;
            String localName = element.getName().getLocalPart();

            if ("case".equals(localName)) { // NON-NLS
                TestError testError = new TestError(stackTraceStr, errorType, errorMsg);
                if (stripPackageAndClass) {
                    //workaround only for UFT - we do not want packageName="All-Tests" and className="&lt;None>" as it comes from JUnit report
                    addItem(new TestResult(moduleName, "", "", testName, status, duration, buildStarted, testError, externalURL));
                } else {
                    addItem(new TestResult(moduleName, packageName, className, testName, status, duration, buildStarted, testError, externalURL));
                }
            }
        }
    }
}