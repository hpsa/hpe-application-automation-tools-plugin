package com.hp.octane.plugins.jetbrains.teamcity.tests.events;

import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.tests.model.BuildContext;
import com.hp.octane.plugins.jetbrains.teamcity.tests.services.BuildTestsService;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.impl.auth.SecuredRunningBuild;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
        File destPath = build.getArtifactsDirectory();
        long buildTime = build.getStartDate().getTime();
        BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
        List<STestRun> tests = stats.getTests(null, BuildStatistics.Order.NATURAL_ASC);

        BuildTestsService.handleTestResult(tests, destPath, buildTime, build);
    }
}
