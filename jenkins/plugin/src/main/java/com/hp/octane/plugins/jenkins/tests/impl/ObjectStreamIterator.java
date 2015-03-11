// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.impl;

import hudson.FilePath;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectStreamIterator<E> implements Iterator<E> {

    private static Logger logger = Logger.getLogger(ObjectStreamIterator.class.getName());

    private MyObjectInputStream ois;
    private FilePath filePath;
    private E next;

    public ObjectStreamIterator(FilePath filePath, boolean deleteOnClose) throws IOException, InterruptedException {
        this.filePath = filePath;
        ois = new MyObjectInputStream(new BufferedInputStream(filePath.read()), deleteOnClose);
    }

    @Override
    public boolean hasNext() {
        if(next != null) {
            return true;
        }
        try {
            next = (E)ois.readObject();
            return true;
        } catch (Exception e) {
            ois.close();
            return false;
        }
    }

    @Override
    public E next() {
        if(hasNext()) {
            E value = next;
            next = null;
            return value;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private class MyObjectInputStream extends ObjectInputStream {

        private boolean deleteOnClose;

        public MyObjectInputStream(InputStream in, boolean deleteOnClose) throws IOException {
            super(in);
            this.deleteOnClose = deleteOnClose;
        }

        @Override
        public void close() {
            try {
                super.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close the stream", e); // NON-NLS
            }
            if(deleteOnClose) {
                try {
                    filePath.delete();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to perform clean up", e); // NON-NLS
                }
            }
        }
    }
}
