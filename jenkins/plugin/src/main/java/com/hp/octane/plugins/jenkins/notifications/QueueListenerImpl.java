package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.commons.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.events.CIEventQueued;
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

	public void onEnterWaiting(Queue.WaitingItem wi) {
		AbstractProject project;
		CIEventQueued event;
		if (wi.task instanceof AbstractProject) {
			project = (AbstractProject) wi.task;
			event = new CIEventQueued(
					CIServerType.JENKINS,
					EventDispatcher.SELF_URL,
					project.getName(),
					CIEventCausesFactory.convertCause(wi.getCauses())
			);
			EventDispatcher.dispatchEvent(event);
		}
	}

	public void onLeft(Queue.LeftItem li) {
		AbstractProject project;
		if (li.task instanceof AbstractProject) {
			project = (AbstractProject) li.task;
			System.out.println(project.getName() + " left queue");
		}
	}
}
