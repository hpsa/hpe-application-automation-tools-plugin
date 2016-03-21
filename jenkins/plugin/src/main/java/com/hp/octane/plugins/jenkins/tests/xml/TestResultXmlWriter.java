// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.xml;

import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import com.hp.octane.plugins.jenkins.tests.TestError;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultContainer;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import com.hp.octane.plugins.jenkins.tests.build.BuildHandlerUtils;
import com.hp.octane.plugins.jenkins.tests.build.BuildTypeDescriptor;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFields;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class TestResultXmlWriter {

    private FilePath targetPath;
    private AbstractBuild build;

    private XMLStreamWriter writer;
    private OutputStream outputStream;

    public TestResultXmlWriter(FilePath targetPath, AbstractBuild build) {
        this.targetPath = targetPath;
        this.build = build;
    }

    public void add(TestResultContainer container) throws InterruptedException, XMLStreamException, IOException {
        Iterator<TestResult> items = container.getIterator();
        initialize(container.getResultFields());

        while (items.hasNext()) {
            TestResult item = items.next();
            writer.writeStartElement("test_run");
            writer.writeAttribute("module", item.getModuleName());
            writer.writeAttribute("package", item.getPackageName());
            writer.writeAttribute("class", item.getClassName());
            writer.writeAttribute("name", item.getTestName());
            writer.writeAttribute("duration", String.valueOf(item.getDuration()));
            writer.writeAttribute("status", item.getResult().toPrettyName());
            writer.writeAttribute("started", String.valueOf(item.getStarted()));
            if(item.getResult().equals(TestResultStatus.FAILED) && item.getTestError() != null) {
                TestError testError = item.getTestError();
                writer.writeStartElement("error");
                writer.writeAttribute("type", String.valueOf(testError.getErrorType()));
                writer.writeAttribute("message", String.valueOf(testError.getErrorMsg()));
                writer.writeCharacters(testError.getStackTraceStr());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    public void close() throws XMLStreamException {
        if (outputStream != null) {
            writer.writeEndElement(); // test_runs
            writer.writeEndElement(); // test_result
            writer.writeEndDocument();
            writer.close();
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void initialize(ResultFields resultFields) throws IOException, InterruptedException, XMLStreamException {
        if (outputStream == null) {
            outputStream = targetPath.write();
            writer = possiblyCreateIndentingWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream));
            writer.writeStartDocument();

            writer.writeStartElement("test_result");
            writer.writeStartElement("build");
            writer.writeAttribute("server", ServerIdentity.getIdentity());
            BuildTypeDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
            writer.writeAttribute("build_type", descriptor.getBuildType());
            if (!StringUtils.isEmpty(descriptor.getSubType())) {
                writer.writeAttribute("sub_type", descriptor.getSubType());
            }
            writer.writeAttribute("build_sid", String.valueOf(build.getNumber()));
            writer.writeEndElement(); // build
            writeFields(resultFields);
            writer.writeStartElement("test_runs");
        }
    }

    private void writeFields(ResultFields resultFields) throws XMLStreamException {
        if (resultFields != null) {
            writer.writeStartElement("test_fields");
            writeField("Framework", resultFields.getFramework());
            writeField("Test_Level", resultFields.getTestLevel());
            writeField("Testing_Tool_Type", resultFields.getTestingTool());
            writer.writeEndElement();
        }
    }

    private void writeField(String type, String value) throws XMLStreamException {
        if (value != null) {
            writer.writeStartElement("test_field");
            writer.writeAttribute("type", type);
            writer.writeAttribute("value", value);
            writer.writeEndElement();
        }
    }

    // TODO: check if there is public mechanism yet
    private XMLStreamWriter possiblyCreateIndentingWriter(XMLStreamWriter writer) {
        try {
            Class<?> clazz = Class.forName("com.sun.xml.txw2.output.IndentingXMLStreamWriter");
            XMLStreamWriter xmlStreamWriter = (XMLStreamWriter) clazz.getConstructor(XMLStreamWriter.class).newInstance(writer);
            clazz.getMethod("setIndentStep", String.class).invoke(xmlStreamWriter, " ");
            return xmlStreamWriter;
        } catch (Exception e) {
            // do without indentation
            return writer;
        }
    }
}
