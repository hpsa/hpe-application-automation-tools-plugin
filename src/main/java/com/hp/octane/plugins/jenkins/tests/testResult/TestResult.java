package com.hp.octane.plugins.jenkins.tests.testResult;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface TestResult {

  /**
   * Writes an XML element from the test result
   */
  void writeXmlElement(XMLStreamWriter writer) throws XMLStreamException;
}
