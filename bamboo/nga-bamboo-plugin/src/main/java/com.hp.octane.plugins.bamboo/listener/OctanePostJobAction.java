package com.hp.octane.plugins.bamboo.listener;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PostJobAction;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class OctanePostJobAction extends BaseListener implements PostJobAction {

	//  [YG] TODO: review the original intent for this listener and remove if not needed
	public void execute(StageExecution paramStageExecution, Job paramJob, BuildResultsSummary paramBuildResultsSummary) {
		PlanResultKey resultKey = paramBuildResultsSummary.getPlanResultKey();
	}
}
