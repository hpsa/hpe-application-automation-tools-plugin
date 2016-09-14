package com.emyoli.nga.listener;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PostJobAction;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class OctanePostJobAction extends BaseListener implements PostJobAction {

    public void execute(StageExecution paramStageExecution, Job paramJob,
            BuildResultsSummary paramBuildResultsSummary) {
        PlanResultKey resultKey = paramBuildResultsSummary.getPlanResultKey();
        System.out.println(resultKey);
        // List<CIEventCause> causes = new ArrayList<>(1);
        // CIEventCause cause = CONVERTER.getCauseWithDetails(
        // paramBuildContext.getParentBuildIdentifier().getBuildResultKey(),
        // paramBuildContext.getParentBuildContext().getPlanResultKey().getPlanKey().getKey(),
        // "admin");
        //
        // CIEvent event =
        // CONVERTER.getEventWithDetails(PlanKeys.getChainKeyIfJobKey(resultKey.getPlanKey()).getKey(),
        // resultKey.getKey(), paramBuildContext.getDisplayName(),
        // CIEventType.STARTED, System.currentTimeMillis(),
        // paramBuildResultsSummary.getav, Arrays.asList(cause),
        // String.valueOf(resultKey.getBuildNumber()));
        // OctaneSDK.getInstance().getEventsService().publishEvent(event);

    }
}
