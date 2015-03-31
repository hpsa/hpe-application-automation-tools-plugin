package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import hudson.model.AbstractBuild;

/**
 * Created by gullery on 31/03/2015.
 */
public abstract class AbstractSCMProcessor {
	public abstract boolean isAppropriate(String className);

	public abstract SCMData getSCMChanges(AbstractBuild build);
}
