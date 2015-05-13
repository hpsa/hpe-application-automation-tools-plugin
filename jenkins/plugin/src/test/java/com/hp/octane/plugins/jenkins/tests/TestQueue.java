// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.model.AbstractBuild;
import org.junit.Assert;

import java.util.Collection;
import java.util.LinkedList;

class TestQueue implements TestResultQueue {

    private LinkedList<QueueItem> queue = new LinkedList<QueueItem>();
    private long ticks;
    private int discard;

    @Override
    public synchronized QueueItem peekFirst() {
        ++ticks;
        if (!queue.isEmpty()) {
            return queue.getFirst();
        } else {
            return null;
        }
    }

    @Override
    public synchronized boolean failed() {
        QueueItem item = queue.removeFirst();
        if (item.failCount++ < 1) {
            queue.add(item);
            return true;
        } else {
            ++discard;
            return false;
        }
    }

    @Override
    public synchronized void remove() {
        queue.removeFirst();
    }

    @Override
    public synchronized void add(String projectName, int buildNumber) {
        queue.add(new QueueItem(projectName, buildNumber));
    }

    public synchronized void add(Collection<? extends AbstractBuild> builds) {
        for (AbstractBuild build: builds) {
            queue.add(new QueueItem(build.getProject().getName(), build.getNumber()));
        }
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized long getTicks() {
        return ticks;
    }

    public synchronized int getDiscards() {
        return discard;
    }

    public void waitForTicks(int n) throws InterruptedException {
        long current = getTicks();
        long target = current + n;
        for (int i = 0; i < 2000; i++) {
            current = getTicks();
            if (current >= target) {
                return;
            }
            Thread.sleep(10);
        }
        Assert.fail("Timed out: ticks: expected=" + target + "; actual=" + current);
    }
}
