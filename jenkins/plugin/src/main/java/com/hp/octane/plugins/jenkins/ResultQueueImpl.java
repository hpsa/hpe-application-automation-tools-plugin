package com.hp.octane.plugins.jenkins;

import com.squareup.tape.FileObjectQueue;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by benmeior on 11/21/2016.
 */
public abstract class ResultQueueImpl implements ResultQueue {

    private static final int RETRY_COUNT = 3;

    private FileObjectQueue<QueueItem> queue;

    private QueueItem currentItem;

    protected void init(File queueFile) throws IOException {
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

            remove();

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

    @Override
    public synchronized void add(String projectName, int buildNumber, String workspace) {
        queue.add(new QueueItem(projectName, buildNumber, workspace));
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
            return json.containsKey("workspace") ?
                    new QueueItem(
                            json.getString("project"),
                            json.getInt("build"),
                            json.getInt("count"),
                            json.getString("workspace")) :
                    new QueueItem(
                            json.getString("project"),
                            json.getInt("build"),
                            json.getInt("count"));
        }

        private JSONObject jsonFromObject(QueueItem item) {
            JSONObject json = new JSONObject();
            json.put("project", item.projectName);
            json.put("build", item.buildNumber);
            json.put("count", item.failCount);
            json.put("workspace", item.workspace);
            return json;
        }
    }
}
