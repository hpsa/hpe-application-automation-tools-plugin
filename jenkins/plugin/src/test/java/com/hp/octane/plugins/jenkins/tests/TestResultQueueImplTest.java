// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestResultQueueImplTest {

    private TestResultQueueImpl queue;

    @Before
    public void init() throws IOException {
        File file = File.createTempFile("TestResultQueueImplTest", "");
        file.delete();
        queue = new TestResultQueueImpl(file);
    }

    @Test
    public void testQueue() {
        queue.add("foo", 1);
        TestResultQueue.QueueItem item = queue.peekFirst();
        Assert.assertEquals("foo", item.projectName);
        Assert.assertEquals(1, item.buildNumber);
        Assert.assertEquals(0, item.failCount);
    }

    @Test
    public void testAddRemove() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        Assert.assertNotNull(queue.peekFirst());
        Assert.assertEquals(queue.peekFirst(), queue.peekFirst());
        queue.remove();
        Assert.assertNull(queue.peekFirst());
    }

    @Test
    public void testRetry() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        Assert.assertEquals(0, queue.peekFirst().failCount);
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().projectName);
        Assert.assertEquals(1, queue.peekFirst().buildNumber);
        Assert.assertEquals(1, queue.peekFirst().failCount);
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().projectName);
        Assert.assertEquals(1, queue.peekFirst().buildNumber);
        Assert.assertEquals(2, queue.peekFirst().failCount);
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().projectName);
        Assert.assertEquals(1, queue.peekFirst().buildNumber);
        Assert.assertEquals(3, queue.peekFirst().failCount);
        Assert.assertFalse(queue.failed());
        Assert.assertNull(queue.peekFirst());
    }

    @Test
    public void testInvalidRemove() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        try {
            queue.remove();
            Assert.fail("should have failed");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testInvalidFailed() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        try {
            queue.failed();
            Assert.fail("should have failed");
        } catch (IllegalStateException e) {
            // expected
        }
    }
}
