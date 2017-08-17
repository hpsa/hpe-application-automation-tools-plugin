/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests.junit;

import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Serializable;

/**
 * Test Run XML writer to mqmTests.xml
 */
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
