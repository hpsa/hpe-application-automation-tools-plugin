package com.hp.octane.plugins.jetbrains.teamcity.events;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.plugins.jetbrains.teamcity.OctaneTeamCityPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelCommonFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ParametersFactory;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 13/03/2016.
 * <p/>
 * Team City Events listener for the need of publishing CI Server events to NGA server
 */

public class ProgressEventsListener extends BuildServerAdapter {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String TRIGGER_BUILD_TYPE_KEY = "buildTypeId";

	@Autowired
	private OctaneTeamCityPlugin octaneTeamCityPlugin;
	@Autowired
	private ModelCommonFactory modelCommonFactory;
	@Autowired
	private ParametersFactory parametersFactory;

	private ProgressEventsListener(EventDispatcher<BuildServerListener> dispatcher) {
		dispatcher.addListener(this);
	}

	@Override
	public void buildTypeAddedToQueue(@NotNull SQueuedBuild queuedBuild) {
		TriggeredBy triggeredBy = queuedBuild.getTriggeredBy();
		if (!triggeredBy.getParameters().containsKey(TRIGGER_BUILD_TYPE_KEY)) {

			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setBuildCiId(queuedBuild.getItemId())
					.setProject(queuedBuild.getBuildType().getExternalId())
					.setProjectDisplayName(queuedBuild.getBuildType().getName())
					.setCauses(new ArrayList<CIEventCause>());
			octaneTeamCityPlugin.getOctaneSDK().getEventsService().publishEvent(event);
		}
	}

	@Override
	public void buildStarted(@NotNull SRunningBuild build) {
		TriggeredBy triggeredBy = build.getTriggeredBy();
		List<CIEventCause> causes = new ArrayList<CIEventCause>();

		if (triggeredBy.getParameters().containsKey(TRIGGER_BUILD_TYPE_KEY)) {
			String rootBuildTypeId = triggeredBy.getParameters().get(TRIGGER_BUILD_TYPE_KEY);
			SQueuedBuild rootBuild = getTriggerBuild(rootBuildTypeId);
			if (rootBuild != null) {
				causes.add(causeFromBuild(rootBuild));
			}
		}

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.STARTED)
				.setProject(build.getBuildTypeExternalId())
				.setProjectDisplayName(build.getBuildTypeName())
				.setBuildCiId(String.valueOf(build.getBuildId()))
				.setNumber(build.getBuildNumber())
				.setParameters(parametersFactory.obtainFromBuild(build))
				.setCauses(causes)
				.setStartTime(build.getStartDate().getTime())
				.setEstimatedDuration(build.getDurationEstimate() * 1000);
		octaneTeamCityPlugin.getOctaneSDK().getEventsService().publishEvent(event);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		TriggeredBy triggeredBy = build.getTriggeredBy();
		List<CIEventCause> causes = new ArrayList<CIEventCause>();

		if (triggeredBy.getParameters().containsKey(TRIGGER_BUILD_TYPE_KEY)) {
			String rootBuildTypeId = triggeredBy.getParameters().get(TRIGGER_BUILD_TYPE_KEY);
			SQueuedBuild rootBuild = getTriggerBuild(rootBuildTypeId);
			if (rootBuild != null) {
				causes.add(causeFromBuild(rootBuild));
			}
		}

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setProject(build.getBuildTypeExternalId())
				.setProjectDisplayName(build.getBuildTypeName())
				.setBuildCiId(String.valueOf(build.getBuildId()))
				.setNumber(build.getBuildNumber())
				.setParameters(parametersFactory.obtainFromBuild(build))
				.setCauses(causes)
				.setStartTime(build.getStartDate().getTime())
				.setEstimatedDuration(build.getDurationEstimate() * 1000)
				.setDuration(build.getDuration() * 1000)
				.setResult(modelCommonFactory.resultFromNativeStatus(build.getBuildStatus()));
		octaneTeamCityPlugin.getOctaneSDK().getEventsService().publishEvent(event);
	}

	private SQueuedBuild getTriggerBuild(String triggerBuildTypeId) {
		SQueuedBuild result = null;
		SBuildType triggerBuildType = octaneTeamCityPlugin.getProjectManager().findBuildTypeById(triggerBuildTypeId);
		if (triggerBuildType != null) {
			List<SQueuedBuild> queuedBuildsOfType = triggerBuildType.getQueuedBuilds(null);
			if (!queuedBuildsOfType.isEmpty()) {
				result = queuedBuildsOfType.get(0);
			}
		}
		return result;
	}

	private CIEventCause causeFromBuild(SQueuedBuild build) {
		return dtoFactory.newDTO(CIEventCause.class)
				.setType(CIEventCauseType.UPSTREAM)
				.setProject(build.getBuildType().getExternalId())
				.setBuildCiId(String.valueOf(build.getItemId()));
	}
}
