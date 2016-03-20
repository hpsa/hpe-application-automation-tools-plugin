package com.hp.octane.plugins.jetbrains.teamcity.events;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.EventsService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.causes.CIEventCause;
import com.hp.nga.integrations.dto.events.CIEvent;
import com.hp.nga.integrations.dto.events.CIEventType;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by gullery on 13/03/2016.
 * <p/>
 * Team City Events listener for the need of publishing CI Server events to NGA server
 */

public class ProgressEventsListener extends BuildServerAdapter {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Autowired
	private ModelFactory modelFactory;

	private ProgressEventsListener(EventDispatcher<BuildServerListener> dispatcher) {
		dispatcher.addListener(this);
	}

	@Override
	public void buildTypeAddedToQueue(@NotNull SQueuedBuild queuedBuild) {
		TriggeredBy triggeredBy = queuedBuild.getTriggeredBy();
		if (!triggeredBy.getParameters().containsKey("buildTypeId")) {
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
		if (triggeredBy.getParameters().containsKey("buildTypeId")) {
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setBuildCiId(String.valueOf(build.getBuildId()))
					.setProject(build.getBuildTypeExternalId())
					.setNumber(build.getBuildNumber())
					.setCauses(new CIEventCause[0])
					.setStartTime(build.getStartDate().getTime())
					.setEstimatedDuration(build.getDurationEstimate());
			SDKManager.getService(EventsService.class).publishEvent(event);
		}
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setBuildCiId(String.valueOf(build.getBuildId()))
				.setProject(build.getBuildTypeExternalId())
				.setNumber(build.getBuildNumber())
				.setCauses(new CIEventCause[0])
				.setStartTime(build.getStartDate().getTime())
				.setEstimatedDuration(build.getDurationEstimate())
				.setDuration(build.getDuration())
				.setResult(modelFactory.resultFromNativeStatus(build.getBuildStatus()));
		SDKManager.getService(EventsService.class).publishEvent(event);
	}
}
