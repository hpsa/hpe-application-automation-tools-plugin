// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.xml;

import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import com.hp.octane.plugins.jenkins.tests.*;
import com.hp.octane.plugins.jenkins.tests.build.BuildHandlerUtils;
import com.hp.octane.plugins.jenkins.tests.build.BuildDescriptor;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFields;
import com.hp.octane.plugins.jenkins.tests.testResult.TestResult;
import hudson.FilePath;
import hudson.model.Run;
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
	//private Run build;
	private BuildDescriptor buildDescriptor;

	private XMLStreamWriter writer;
	private OutputStream outputStream;

	public TestResultXmlWriter(FilePath targetPath, BuildDescriptor buildDescriptor) {
		this.targetPath = targetPath;
		this.buildDescriptor = buildDescriptor;
	}

	public TestResultXmlWriter(FilePath targetPath, Run build) {
		this.targetPath = targetPath;
		//this.build = build;
		this.buildDescriptor = BuildHandlerUtils.getBuildType(build);
	}

	public void writeResults(TestResultContainer testResultContainer) throws InterruptedException, XMLStreamException, IOException {
		if (testResultContainer != null) {
			ResultFields resultFields = testResultContainer.getResultFields();
			initialize(resultFields);

			Iterator<TestResult> testResults = testResultContainer.getIterator();
			while (testResults.hasNext()) {
				TestResult testResult = testResults.next();
				testResult.writeXmlElement(writer);
			}
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
			writer.writeAttribute("server_id", ServerIdentity.getIdentity());
			BuildDescriptor descriptor = this.buildDescriptor;
			writer.writeAttribute("job_id", descriptor.getJobId());
			writer.writeAttribute("job_name", descriptor.getJobName());
			writer.writeAttribute("build_id", descriptor.getBuildId());
			writer.writeAttribute("build_name", descriptor.getBuildName());
			if (!StringUtils.isEmpty(descriptor.getSubType())) {
				writer.writeAttribute("sub_type", descriptor.getSubType());
			}
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
