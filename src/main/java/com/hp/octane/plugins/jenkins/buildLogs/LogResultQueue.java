package com.hp.octane.plugins.jenkins.buildLogs;

import com.hp.octane.plugins.jenkins.ResultQueueImpl;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;

/**
 * Created by benmeior on 11/21/2016.
 */
public class LogResultQueue extends ResultQueueImpl {

    public LogResultQueue() throws IOException {
        File queueFile = new File(Jenkins.getInstance().getRootDir(), "octane-log-result-queue.dat");
        init(queueFile);
    }
}
