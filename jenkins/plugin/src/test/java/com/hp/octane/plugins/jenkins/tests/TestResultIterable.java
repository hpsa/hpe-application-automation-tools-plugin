// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;

public class TestResultIterable implements Iterable<TestResult> {

    private Reader reader;

    public TestResultIterable(File file) throws FileNotFoundException {
        this.reader = new FileReader(file);
    }

    public TestResultIterable(Reader reader) {
        this.reader = reader;
    }

    @Override
    public Iterator<TestResult> iterator() {
        try {
            return new TestResultIterator(reader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
