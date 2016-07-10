package com.hp.octane.plugins.jenkins.events;

import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Created by gullery on 10/07/2016.
 */

@Extension
public class SCMListenerImpl extends SCMListener {
	private static final Logger logger = LogManager.getLogger(SCMListenerImpl.class);

	@Override
	public void onCheckout(Run<?, ?> build, SCM scm, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState pollingBaseline) throws Exception {
		super.onCheckout(build, scm, workspace, listener, changelogFile, pollingBaseline);
	}

	@Override
	public void onChangeLogParsed(Run<?, ?> build, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {
		super.onChangeLogParsed(build, scm, listener, changelog);

		if (changelog != null && !changelog.isEmptySet()) {
			if (build instanceof AbstractBuild) {
				SCMProcessor scmProcessor = SCMProcessors.getAppropriate(scm.getClass().getName());
				if (scmProcessor != null) {
					SCMData scmData = scmProcessor.getSCMData((AbstractBuild) build);
					//  TODO: generate event with SCM data
				} else {
					logger.info("SCM changes detected, but no processors found for SCM provider of type " + scm.getClass().getName());
				}
			}
		}
	}
}
