// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.tests;

import com.hpe.application.automation.tools.octane.tests.junit.JUnitTestResult;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class TestResultIterable implements Iterable<JUnitTestResult> {

    private Reader reader;

    public TestResultIterable(File file) throws FileNotFoundException {
        this.reader = new FileReader(file);
    }

    public TestResultIterable(Reader reader) {
        this.reader = reader;
    }

    @Override
    public TestResultIterator iterator() {
        try {
            return new TestResultIterator(reader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
