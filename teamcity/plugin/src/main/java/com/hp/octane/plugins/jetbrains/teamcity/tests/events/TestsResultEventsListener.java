package com.hp.octane.plugins.jetbrains.teamcity.tests.events;

import com.hp.octane.plugins.jetbrains.teamcity.tests.services.TestsResultsService;
import jetbrains.buildServer.serverSide.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by lev on 06/01/2016.
 */

public class TestsResultEventsListener extends BuildServerAdapter {

	@Autowired
	private TestsResultsService testsService;

	private TestsResultEventsListener(SBuildServer server) {
		server.addListener(this);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
		List<STestRun> tests = stats.getTests(null, BuildStatistics.Order.NATURAL_ASC);
		if (tests != null && !tests.isEmpty()) {
			testsService.handleTestResult(tests, build);
		}
	}
}
