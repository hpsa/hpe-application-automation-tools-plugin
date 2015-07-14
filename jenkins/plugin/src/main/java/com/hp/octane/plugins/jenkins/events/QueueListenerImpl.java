package com.hp.octane.plugins.jenkins.events;

import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.events.CIEventQueued;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class QueueListenerImpl extends QueueListener {
	private static Logger logger = Logger.getLogger(QueueListenerImpl.class.getName());

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
			EventsDispatcher.getExtensionInstance().dispatchEvent(event);
		}
	}

	@Override
	public void onEnterBlocked(Queue.BlockedItem bi) {
		if (bi.task instanceof AbstractProject) {

		}
	}

	@Override
	public void onEnterBuildable(Queue.BuildableItem bi) {
		if (bi.task instanceof AbstractProject) {

		}
	}

	@Override
	public void onLeft(Queue.LeftItem li) {
		AbstractProject project;
		if (li.task instanceof AbstractProject) {
			project = (AbstractProject) li.task;
			logger.info(project.getName() + " left queue");
		}
	}
}
