// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.xml;

import org.apache.commons.io.IOUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public abstract class AbstractXmlIterator<E> {

    private InputStream is;
    private XMLEventReader reader;
    private LinkedList<E> queue;
    private boolean closed;

    public AbstractXmlIterator(InputStream is) throws XMLStreamException {
        this.is = is;
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
