package com.hp.octane.plugins.jetbrains.teamcity.tests;

import com.hp.octane.integrations.OctaneSDK;
import jetbrains.buildServer.serverSide.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by lev on 06/01/2016.
 */

public class TestsResultEventsListener extends BuildServerAdapter {
	private static final OctaneSDK octaneSDK = OctaneSDK.getInstance();

	private TestsResultEventsListener(SBuildServer server) {
		server.addListener(this);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
		if (!stats.getTests(null, BuildStatistics.Order.NATURAL_ASC).isEmpty()) {
			octaneSDK.getTestsService().enqueuePushTestsResult(build.getBuildTypeExternalId(), build.getBuildNumber());
		}
	}
}
