// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.junit;

import com.hp.octane.plugins.jenkins.tests.testResult.TestResult;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Serializable;

final public class JUnitTestResult implements Serializable, TestResult {

    private final String moduleName;
    private final String packageName;
    private final String className;
    private final String testName;
    private final TestResultStatus result;
    private final long duration;
    private final long started;
    private final TestError testError;
    private final String externalReportUrl;

    public JUnitTestResult(String moduleName, String packageName, String className, String testName, TestResultStatus result, long duration, long started, TestError testError, String externalReportUrl) {
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.className = className;
        this.testName = testName;
        this.result = result;
        this.duration = duration;
        this.started = started;
        this.testError = testError;
        this.externalReportUrl = externalReportUrl;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getTestName() {
        return testName;
    }

    public TestResultStatus getResult() {
        return result;
    }

    public long getDuration() {
        return duration;
    }

    public long getStarted() {
        return started;
    }

    public TestError getTestError() {
        return testError;
    }

    public String getExternalReportUrl() {return externalReportUrl;}

    @Override
    public void writeXmlElement(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("test_run");
        writer.writeAttribute("module", moduleName);
        writer.writeAttribute("package", packageName);
        writer.writeAttribute("class", className);
        writer.writeAttribute("name", testName);
        writer.writeAttribute("duration", String.valueOf(duration));
        writer.writeAttribute("status", result.toPrettyName());
        writer.writeAttribute("started", String.valueOf(started));
        if(externalReportUrl != null) {
            writer.writeAttribute("external_report_url", externalReportUrl);
        }
        if (result.equals(TestResultStatus.FAILED) && testError != null) {
            writer.writeStartElement("error");
            writer.writeAttribute("type", String.valueOf(testError.getErrorType()));
            writer.writeAttribute("message", String.valueOf(testError.getErrorMsg()));
            writer.writeCharacters(testError.getStackTraceStr());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
}
