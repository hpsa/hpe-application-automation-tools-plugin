package com.hp.application.automation.tools.octane.events;

import com.google.inject.Inject;
import com.hp.application.automation.tools.octane.tests.TestListener;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

/**
 * Created by leviy on 27/03/2017.
 */
@Extension
public class TestListenerImpl extends RunListener<Run> {

	@Inject
	private TestListener testListener;

	@Override
	public void onCompleted(Run r, @Nonnull TaskListener listener) {
		testListener.processBuild(r, listener);
	}
}
