// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.Extension;

import java.util.LinkedList;

@Extension
public class TestResultQueueImpl implements TestResultQueue {

    // TODO: janotav: use persistent queue (square:tape?)

    private static final LinkedList<QueueItem> queue = new LinkedList<QueueItem>();

    @Override
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public synchronized QueueItem removeFirst() {
        return queue.removeFirst();
    }

    @Override
    public synchronized void add(String projectName, int buildNumber) {
        queue.add(new QueueItem(projectName, buildNumber));
    }
}
