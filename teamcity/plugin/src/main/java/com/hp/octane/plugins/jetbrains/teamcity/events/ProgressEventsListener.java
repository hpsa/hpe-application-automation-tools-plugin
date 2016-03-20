package com.hp.octane.plugins.jetbrains.teamcity.events;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.EventsService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.causes.CIEventCause;
import com.hp.nga.integrations.dto.causes.CIEventCauseType;
import com.hp.nga.integrations.dto.events.CIEvent;
import com.hp.nga.integrations.dto.events.CIEventType;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

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
	private NGAPlugin ngaPlugin;

	@Autowired
	private ModelFactory modelFactory;

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
					.setCauses(new CIEventCause[0]);
			SDKManager.getService(EventsService.class).publishEvent(event);
		}
	}

	@Override
	public void buildStarted(@NotNull SRunningBuild build) {
		TriggeredBy triggeredBy = build.getTriggeredBy();
		CIEventCause[] causes = new CIEventCause[0];

		if (triggeredBy.getParameters().containsKey(TRIGGER_BUILD_TYPE_KEY)) {
			String rootBuildTypeId = triggeredBy.getParameters().get(TRIGGER_BUILD_TYPE_KEY);
			SQueuedBuild rootBuild = getTriggerBuild(rootBuildTypeId);
			if (rootBuild != null) {
				causes = new CIEventCause[]{causeFromBuild(rootBuild)};
			}

			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setBuildCiId(String.valueOf(build.getBuildId()))
					.setProject(build.getBuildTypeExternalId())
					.setNumber(build.getBuildNumber())
					.setCauses(causes)
					.setStartTime(build.getStartDate().getTime())
					.setEstimatedDuration(build.getDurationEstimate());
			SDKManager.getService(EventsService.class).publishEvent(event);
		}
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		TriggeredBy triggeredBy = build.getTriggeredBy();
		CIEventCause[] causes = new CIEventCause[0];

		if (triggeredBy.getParameters().containsKey(TRIGGER_BUILD_TYPE_KEY)) {
			String rootBuildTypeId = triggeredBy.getParameters().get(TRIGGER_BUILD_TYPE_KEY);
			SQueuedBuild rootBuild = getTriggerBuild(rootBuildTypeId);
			if (rootBuild != null) {
				causes = new CIEventCause[]{causeFromBuild(rootBuild)};
			}
		}

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setBuildCiId(String.valueOf(build.getBuildId()))
				.setProject(build.getBuildTypeExternalId())
				.setNumber(build.getBuildNumber())
				.setCauses(causes)
				.setStartTime(build.getStartDate().getTime())
				.setEstimatedDuration(build.getDurationEstimate())
				.setDuration(build.getDuration())
				.setResult(modelFactory.resultFromNativeStatus(build.getBuildStatus()));
		SDKManager.getService(EventsService.class).publishEvent(event);
	}

	private SQueuedBuild getTriggerBuild(String triggerBuildTypeId) {
		SQueuedBuild result = null;
		SBuildType triggerBuildType = ngaPlugin.getProjectManager().findBuildTypeByExternalId(triggerBuildTypeId);
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
