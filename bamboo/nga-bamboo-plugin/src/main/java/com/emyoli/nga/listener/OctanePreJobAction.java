package com.emyoli.nga.listener;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;

import java.util.Arrays;

public class OctanePreJobAction extends BaseListener implements PreJobAction {

    public void execute(StageExecution paramStageExecution,
            com.atlassian.bamboo.v2.build.BuildContext paramBuildContext) {
        PlanResultKey resultKey = paramBuildContext.getPlanResultKey();
        // List<CIEventCause> causes = new ArrayList<>(1);
        CIEventCause cause = CONVERTER.getCauseWithDetails(
                paramBuildContext.getParentBuildIdentifier().getBuildResultKey(),
                paramBuildContext.getParentBuildContext().getPlanResultKey().getPlanKey().getKey(), "admin");

        CIEvent event = CONVERTER.getEventWithDetails(resultKey.getPlanKey().getKey(),
                resultKey.getKey(), paramBuildContext.getDisplayName(), CIEventType.STARTED, System.currentTimeMillis(),
                paramStageExecution.getChainExecution().getAverageDuration(), Arrays.asList(cause),
                String.valueOf(resultKey.getBuildNumber()));
        OctaneSDK.getInstance().getEventsService().publishEvent(event);
    }
}
