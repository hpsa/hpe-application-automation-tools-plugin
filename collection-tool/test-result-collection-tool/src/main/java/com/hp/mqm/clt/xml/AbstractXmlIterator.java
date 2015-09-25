// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.xml;

import org.apache.commons.io.IOUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.ValidationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public abstract class AbstractXmlIterator<E> {

    private File xmlFile;
    private InputStream is;
    private XMLEventReader reader;
    private LinkedList<E> queue;
    private boolean closed;

    private static final String INSECURE_XML_ERROR_MSG = "Insecure XML- contains external entity(s) and/or entity declaration(s).";


    public AbstractXmlIterator(File xmlFile) throws XMLStreamException, FileNotFoundException {
        this.xmlFile = xmlFile;
        this.is = new FileInputStream(xmlFile);
        reader = createXmlInputFactory().createXMLEventReader(is);
        queue = new LinkedList<E>();
    }

    public boolean hasNext() throws XMLStreamException, IOException, InterruptedException {
        while (queue.isEmpty() && !closed) {
            if (reader.hasNext()) {
                onEvent(reader.nextEvent());
            } else {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // close quietly
                }
                IOUtils.closeQuietly(is);
                closed = true;
            }
        }
        return !queue.isEmpty();
    }

    public E next() throws XMLStreamException, IOException, InterruptedException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            return queue.removeFirst();
        }
    }

    protected void validateSecureXML(String xsdSchema) throws ValidationException, IOException {
        FileInputStream fis = null;
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream xsdStream = new ByteArrayInputStream(xsdSchema.getBytes("UTF-8"));
            Schema schema = sf.newSchema(new StreamSource(xsdStream));

            Validator validator = schema.newValidator();

            // Check whether XML contains a DTD declaration that can be used for potential XXE and DoS attack(s)
            XMLReader xmlReader = createXMLParser();
            fis = new FileInputStream(xmlFile);
            SAXSource source = new SAXSource(xmlReader, new InputSource(fis));
            validator.validate(source);
        } catch (SAXException e) {
            throw new ValidationException("Error");
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private static XMLReader createXMLParser() throws UnsupportedEncodingException, ValidationException {
        XMLReader xmlReader;
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            xmlReader.setProperty("http://xml.org/sax/properties/declaration-handler", new SecureDeclHandler());

            xmlReader.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    throw new RuntimeException(INSECURE_XML_ERROR_MSG);
                }
            });
        } catch (SAXException e) {
            throw new RuntimeException("Can not create XML parser");
        }

        return xmlReader;
    }

    private static class SecureDeclHandler implements DeclHandler {

        private SecureDeclHandler() {
        }

        @Override
        public void elementDecl(String name, String model) throws SAXException {}

        @Override
        public void attributeDecl(String eName, String aName, String type, String mode, String value)
                throws SAXException {}

        @Override
        public void internalEntityDecl(String name, String value) throws SAXException {
            throw new RuntimeException(INSECURE_XML_ERROR_MSG);
        }

        @Override
        public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
            throw new RuntimeException(INSECURE_XML_ERROR_MSG);

        }
    }
    protected abstract void onEvent(XMLEvent event) throws IOException, InterruptedException;

    protected void addItem(E item) {
        queue.add(item);
    }

    private static XMLInputFactory createXmlInputFactory() {
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return xmlFactory;
    }
}
