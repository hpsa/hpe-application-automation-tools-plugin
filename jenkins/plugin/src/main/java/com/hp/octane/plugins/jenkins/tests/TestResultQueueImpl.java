// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.squareup.tape.FileObjectQueue;
import hudson.Extension;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

@Extension
public class TestResultQueueImpl implements TestResultQueue {

    private static final int RETRY_COUNT = 3;

    private FileObjectQueue<QueueItem> queue;

    private QueueItem currentItem;

    public TestResultQueueImpl() throws IOException {
        File queueFile = new File(Jenkins.getInstance().getRootDir(), "octane-test-result-queue.dat");
        init(queueFile);
    }

    /*
     * To be used in tests only.
     */
    TestResultQueueImpl(File queueFile) throws IOException {
        init(queueFile);
    }

    private void init(File queueFile) throws IOException {
        queue = new FileObjectQueue<QueueItem>(queueFile, new JsonConverter());
    }

    @Override
    public synchronized QueueItem peekFirst() {
        if (currentItem == null) {
            currentItem = queue.peek();
        }
        return currentItem;
    }

    @Override
    public synchronized boolean failed() {
        if (currentItem != null) {
            boolean retry;
            if (++currentItem.failCount <= RETRY_COUNT) {
                queue.add(currentItem);
                retry = true;
            } else {
                retry = false;
            }
            queue.remove();
            currentItem = null;
            return retry;
        } else {
            throw new IllegalStateException("no outstanding item");
        }
    }

    @Override
    public synchronized void remove() {
        if (currentItem != null) {
            queue.remove();
            currentItem = null;
        } else {
            throw new IllegalStateException("no outstanding item");
        }
    }

    @Override
    public synchronized void add(String projectName, int buildNumber) {
        queue.add(new QueueItem(projectName, buildNumber));
    }

    private static class JsonConverter implements FileObjectQueue.Converter<QueueItem> {

        @Override
        public QueueItem from(byte[] bytes) throws IOException {
            JSONObject json = (JSONObject) JSONSerializer.toJSON(IOUtils.toString(new ByteArrayInputStream(bytes)));
            return objectFromJson(json);
        }

        @Override
        public void toStream(QueueItem item, OutputStream bytes) throws IOException {
            JSONObject json = jsonFromObject(item);
            OutputStreamWriter writer = new OutputStreamWriter(bytes);
            writer.append(json.toString());
            writer.close();
        }

        private QueueItem objectFromJson(JSONObject json) {
            return new QueueItem(
                    json.getString("project"),
                    json.getInt("build"),
                    json.getInt("count"));
        }

        private JSONObject jsonFromObject(QueueItem item) {
            JSONObject json = new JSONObject();
            json.put("project", item.projectName);
            json.put("build", item.buildNumber);
            json.put("count", item.failCount);
            return json;
        }
    }
}
