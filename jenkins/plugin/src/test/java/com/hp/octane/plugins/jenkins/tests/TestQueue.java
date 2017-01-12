// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.hp.octane.plugins.jenkins.ResultQueue;
import hudson.model.AbstractBuild;
import org.junit.Assert;

import java.util.Collection;
import java.util.LinkedList;

public class TestQueue implements ResultQueue {

	private LinkedList<QueueItem> queue = new LinkedList<QueueItem>();
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
		if (item.incrementFailCount() < 1) {
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

	@Override
	public void add(String projectName, int buildNumber, String workspace) {
		queue.add(new QueueItem(projectName, buildNumber, workspace));
	}

	public synchronized void add(Collection<? extends AbstractBuild> builds) {
		for (AbstractBuild build : builds) {
			queue.add(new QueueItem(build.getProject().getName(), build.getNumber()));
		}
	}

	public synchronized int size() {
		return queue.size();
	}

	public synchronized int getDiscards() {
		return discard;
	}

	//  test usage only; [YG] - TODO: remove this code from here ASAP
	private long ticks;
	public void waitForTicks(int n) throws InterruptedException {
		long current = ticks;
		long target = current + n;
		for (int i = 0; i < 2000; i++) {
			current = ticks;
			if (current >= target) {
				return;
			}
			Thread.sleep(10);
		}
		Assert.fail("Timed out: ticks: expected=" + target + "; actual=" + current);
	}
}
