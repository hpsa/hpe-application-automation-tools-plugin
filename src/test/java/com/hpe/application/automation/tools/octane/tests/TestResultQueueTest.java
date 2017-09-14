/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests;

import com.hpe.application.automation.tools.octane.ResultQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("squid:S2699")
public class TestResultQueueTest {

    private TestsResultQueue queue;

    @Before
    public void init() throws IOException {
        File file = File.createTempFile("TestResultQueueTest", "");
        file.delete();
        queue = new TestsResultQueue(file);
    }

    @Test
    public void testQueue() {
        queue.add("foo", 1);
        ResultQueue.QueueItem item = queue.peekFirst();
        Assert.assertEquals("foo", item.getProjectName());
        Assert.assertEquals(1, item.getBuildNumber());
        Assert.assertEquals(0, item.getFailCount());
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
        Assert.assertEquals(0, queue.peekFirst().getFailCount());
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().getProjectName());
        Assert.assertEquals(1, queue.peekFirst().getBuildNumber());
        Assert.assertEquals(1, queue.peekFirst().getFailCount());
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().getProjectName());
        Assert.assertEquals(1, queue.peekFirst().getBuildNumber());
        Assert.assertEquals(2, queue.peekFirst().getFailCount());
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().getProjectName());
        Assert.assertEquals(1, queue.peekFirst().getBuildNumber());
        Assert.assertEquals(3, queue.peekFirst().getFailCount());
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
