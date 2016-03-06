package com.hp.octane.plugins.jetbrains.teamcity.tests.events;

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

	private static final String TEAMCITY_BUILD_CHECKOUT_DIR = "teamcity.build.checkoutDir";

	public BuildFinishedListener(SBuildServer server) {
		server.addListener(this);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		String currPath = ((SecuredRunningBuild) build).getBuildFinishParameters().get(TEAMCITY_BUILD_CHECKOUT_DIR);
		File destPath = build.getArtifactsDirectory();
		long buildTime = build.getStartDate().getTime();

		BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
		List<STestRun> tests = stats.getTests(null, BuildStatistics.Order.NATURAL_ASC);

		BuildTestsService.handleTestResult(currPath, destPath, buildTime);
	}
}
