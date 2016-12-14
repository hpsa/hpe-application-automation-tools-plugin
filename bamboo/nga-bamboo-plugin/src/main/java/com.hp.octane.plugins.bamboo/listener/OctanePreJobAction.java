package com.hp.octane.plugins.bamboo.listener;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.PhaseType;

import java.util.Arrays;

public class OctanePreJobAction extends BaseListener implements PreJobAction {

	public void execute(StageExecution paramStageExecution, BuildContext buildContext) {

		PlanResultKey resultKey = buildContext.getPlanResultKey();

		CIEventCause cause = CONVERTER.getCauseWithDetails(
				buildContext.getParentBuildIdentifier().getBuildResultKey(),
				buildContext.getParentBuildContext().getPlanResultKey().getPlanKey().getKey(), "admin");

		//create and send started event
		CIEvent event = CONVERTER.getEventWithDetails(
				resultKey.getPlanKey().getKey(),
				resultKey.getKey(),
				buildContext.getShortName(),
				CIEventType.STARTED,
				System.currentTimeMillis(),
				paramStageExecution.getChainExecution().getAverageDuration(),
				Arrays.asList(cause),
				String.valueOf(resultKey.getBuildNumber()),
				PhaseType.INTERNAL);

		OctaneSDK.getInstance().getEventsService().publishEvent(event);

		//create and send SCM event
		CIEvent scmEvent = CONVERTER.getEventWithDetails(
				resultKey.getPlanKey().getKey(),
				resultKey.getKey(),
				buildContext.getShortName(),
				CIEventType.SCM,
				System.currentTimeMillis(),
				paramStageExecution.getChainExecution().getAverageDuration(),
				Arrays.asList(cause),
				String.valueOf(resultKey.getBuildNumber()),
				CONVERTER.getScmData(buildContext),
				PhaseType.INTERNAL);

		OctaneSDK.getInstance().getEventsService().publishEvent(scmEvent);
	}
}
