// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

public class TestResultIterable implements Iterable<TestResult> {

    private File file;

    public TestResultIterable(File file) {
        this.file = file;
    }

    @Override
    public Iterator<TestResult> iterator() {
        try {
            return new TestResultIterator(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
