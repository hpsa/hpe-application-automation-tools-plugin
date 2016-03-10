package com.hp.octane.plugins.jetbrains.teamcity.tests.events;


import com.hp.octane.plugins.jetbrains.teamcity.tests.services.BuildTestsService;
import jetbrains.buildServer.serverSide.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by lev on 06/01/2016.
 */
public class BuildFinishedListener extends BuildServerAdapter {


	public BuildFinishedListener(SBuildServer server) {
		server.addListener(this);
	}

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        long buildTime = build.getStartDate().getTime();
        BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
        List<STestRun> tests = stats.getTests(null, BuildStatistics.Order.NATURAL_ASC);

        BuildTestsService.handleTestResult(tests,  buildTime, build);
    }
}
