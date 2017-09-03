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
import hudson.model.AbstractBuild;
import org.junit.Assert;

import java.util.Collection;
import java.util.LinkedList;

@SuppressWarnings("squid:S2925")
public class TestQueue implements ResultQueue {

	private LinkedList<QueueItem> queue = new LinkedList<>();
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

	@Override
	public synchronized void clear() {
		while (!queue.isEmpty()) {
			queue.remove();
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
