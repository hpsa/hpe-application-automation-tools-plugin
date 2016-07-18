package com.hp.octane.plugins.jetbrains.teamcity.tests;

import com.hp.octane.plugins.jetbrains.teamcity.OctaneTeamCityPlugin;
import jetbrains.buildServer.serverSide.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lev on 06/01/2016.
 */

public class TestsResultEventsListener extends BuildServerAdapter {

	@Autowired
	private OctaneTeamCityPlugin octaneTeamCityPlugin;

	private TestsResultEventsListener(SBuildServer server) {
		server.addListener(this);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
		if (!stats.getTests(null, BuildStatistics.Order.NATURAL_ASC).isEmpty()) {
			octaneTeamCityPlugin.getOctaneSDK().getTestsService().enqueuePushTestsResult(build.getBuildTypeExternalId(), build.getBuildNumber());
		}
	}
}
