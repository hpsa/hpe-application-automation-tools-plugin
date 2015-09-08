// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import org.apache.commons.io.IOUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class TestResultIterator implements Iterator<TestResult> {

    private Reader input;
    private XMLEventReader reader;
    private LinkedList<TestResult> items = new LinkedList<TestResult>();
    private boolean closed;
    private String buildType;

    public TestResultIterator(Reader input) throws FileNotFoundException, XMLStreamException {
        this.input = input;
        reader = XMLInputFactory.newInstance().createXMLEventReader(input);
    }

    @Override
    public boolean hasNext() {
        try {
            while (items.isEmpty() && !closed) {
                if (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event instanceof StartElement) {
                        StartElement element = (StartElement) event;
                        String localName = element.getName().getLocalPart();
                        if ("test".equals(localName)) {
                            String moduleName = element.getAttributeByName(new QName("module")).getValue();
                            String packageName = element.getAttributeByName(new QName("package")).getValue();
                            String className = element.getAttributeByName(new QName("class")).getValue();
                            String testName  = element.getAttributeByName(new QName("name")).getValue();
                            long duration = Long.valueOf(element.getAttributeByName(new QName("duration")).getValue());
                            TestResultStatus status = TestResultStatus.fromPrettyName(element.getAttributeByName(new QName("status")).getValue());
                            long started = Long.valueOf(element.getAttributeByName(new QName("started")).getValue());
                            items.add(new TestResult(moduleName, packageName, className, testName, status, duration, started));
                        } else if ("build".equals(localName)) {
                            buildType = element.getAttributeByName(new QName("buildType")).getValue();
                        }
                    }
                } else {
                    closed = true;
                    IOUtils.closeQuietly(input);
                    reader.close();
                }
            }
            return !items.isEmpty();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestResult next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return items.removeFirst();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public String getBuildType() {
        hasNext();
        return buildType;
    }
}
