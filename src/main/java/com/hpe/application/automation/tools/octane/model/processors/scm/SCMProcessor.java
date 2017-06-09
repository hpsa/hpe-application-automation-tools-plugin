package com.hpe.application.automation.tools.octane.model.processors.scm;

import com.hp.octane.integrations.dto.scm.SCMData;
import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.List;

/**
 * Created by gullery on 31/03/2015.
 */

public interface SCMProcessor {
	SCMData getSCMData(AbstractBuild build);
	List<SCMData> getSCMData(WorkflowRun run);
}
