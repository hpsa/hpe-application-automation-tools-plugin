// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.xml;

import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import com.hp.octane.plugins.jenkins.tests.TestResult;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.apache.commons.io.IOUtils;

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

    public void add(Iterator<TestResult> items) throws InterruptedException, XMLStreamException, IOException {
        initialize();

        while (items.hasNext()) {
            TestResult item = items.next();
            writer.writeStartElement("test");
            writer.writeAttribute("module", item.getModuleName());
            writer.writeAttribute("package", item.getPackageName());
            writer.writeAttribute("class", item.getClassName());
            writer.writeAttribute("name", item.getTestName());
            writer.writeAttribute("duration", String.valueOf(item.getDuration()));
            writer.writeAttribute("status", item.getResult().toPrettyName());
            writer.writeAttribute("started", String.valueOf(item.getStarted()));
            writer.writeEndElement();
        }
    }

    public void close() throws XMLStreamException {
        if (outputStream != null) {
            writer.writeEndElement(); // tests
            writer.writeEndElement(); // build
            writer.writeEndElement(); // testResult
            writer.writeEndDocument();
            writer.close();
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void initialize() throws IOException, InterruptedException, XMLStreamException {
        if (outputStream == null) {
            outputStream = targetPath.write();
            writer = possiblyCreateIndentingWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream));
            writer.writeStartDocument();

            writer.writeStartElement("testResult");
            writer.writeStartElement("build");
            writer.writeAttribute("server", ServerIdentity.getIdentity());
            writer.writeAttribute("buildType", build.getProject().getName());
            writer.writeAttribute("buildSid", String.valueOf(build.getNumber()));
            writer.writeStartElement("tests");
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
