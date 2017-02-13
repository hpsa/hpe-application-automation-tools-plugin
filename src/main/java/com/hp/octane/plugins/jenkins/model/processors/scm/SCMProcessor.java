package com.hp.octane.plugins.jenkins.model.processors.scm;

import com.hp.octane.integrations.dto.scm.SCMData;
import hudson.model.AbstractBuild;

/**
 * Created by gullery on 31/03/2015.
 */

public interface SCMProcessor {
	SCMData getSCMData(AbstractBuild build);
}
