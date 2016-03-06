package com.hp.octane.plugins.jenkins.events;

import com.hp.nga.integrations.dto.events.CIEventQueued;
import com.hp.octane.plugins.jenkins.model.CIEventCausesFactory;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class QueueListenerImpl extends QueueListener {

	@Override
	public void onEnterWaiting(Queue.WaitingItem wi) {
		AbstractProject project;
		CIEventQueued event;
		if (wi.task instanceof AbstractProject) {
			project = (AbstractProject) wi.task;
			event = new CIEventQueued(
					project.getName(),
					CIEventCausesFactory.processCauses(wi.getCauses())
			);
			//  REMARK: temporary decided to not send QUEUED event
			//EventsDispatcher.getExtensionInstance().dispatchEvent(event);
		}
	}
}
