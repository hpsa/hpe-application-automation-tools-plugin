package com.hp.octane.plugins.jetbrains.teamcity;

import com.hp.octane.integrations.spi.CIPluginServices;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.pipelines.BuildHistory;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.tests.BuildContext;
import com.hp.octane.integrations.dto.tests.TestRun;
import com.hp.octane.integrations.dto.tests.TestRunResult;
import com.hp.octane.integrations.dto.tests.TestsResult;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.OctaneConfigStructure;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelCommonFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.SnapshotsFactory;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.serverSide.BuildStatistics;
import jetbrains.buildServer.serverSide.BuildStatisticsOptions;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.STestRun;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gullery on 21/01/2016.
 * <p/>
 * TeamCity CI Server oriented extension of CI Data Provider
 */

public class TeamCityPluginServicesImpl implements CIPluginServices {
	private static final Logger logger = LogManager.getLogger(TeamCityPluginServicesImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String pluginVersion = "9.1.5";

	@Autowired
	private OctaneTeamCityPlugin octaneTeamCityPlugin;
	@Autowired
	private SBuildServer buildServer;
	@Autowired
	private ModelCommonFactory modelCommonFactory;
	@Autowired
	private SnapshotsFactory snapshotsFactory;

	@Override
	public CIServerInfo getServerInfo() {
		return dtoFactory.newDTO(CIServerInfo.class)
				.setInstanceId(octaneTeamCityPlugin.getConfig().getIdentity())
				.setInstanceIdFrom(octaneTeamCityPlugin.getConfig().getIdentityFromAsLong())
				.setSendingTime(System.currentTimeMillis())
				.setType(CIServerTypes.TEAMCITY)
				.setUrl(buildServer.getRootUrl())
				.setVersion(pluginVersion);
	}

	@Override
	public CIPluginInfo getPluginInfo() {
		return dtoFactory.newDTO(CIPluginInfo.class)
				.setVersion(pluginVersion);
	}

	@Override
	public File getAllowedOctaneStorage() {
		return new File(buildServer.getServerRootPath(), "logs");
	}

	@Override
	public OctaneConfiguration getOctaneConfiguration() {
		OctaneConfiguration result = null;
		OctaneConfigStructure config = octaneTeamCityPlugin.getConfig();
		if (config != null && config.getLocation() != null && !config.getLocation().isEmpty() && config.getSharedSpace() != null) {
			result = dtoFactory.newDTO(OctaneConfiguration.class)
					.setUrl(config.getLocation())
					.setSharedSpace(config.getSharedSpace())
					.setApiKey(config.getUsername())
					.setSecret(config.getSecretPassword());
		}
		return result;
	}

	@Override
	public CIProxyConfiguration getProxyConfiguration(String targetHost) {
		CIProxyConfiguration result = null;
		if (isProxyNeeded(targetHost)) {
			Map<String, String> propertiesMap = parseProperties(System.getenv("TEAMCITY_SERVER_OPTS"));
			result = dtoFactory.newDTO(CIProxyConfiguration.class)
					.setHost(propertiesMap.get("Dhttps.proxyHost"))
					.setPort(Integer.parseInt(propertiesMap.get("Dhttps.proxyPort")))
					.setUsername("")
					.setPassword("");
		}
		return result;
	}

	@Override
	public CIJobsList getJobsList(boolean includeParameters) {
		return modelCommonFactory.CreateProjectList();
	}

	@Override
	public PipelineNode getPipeline(String rootJobCiId) {
		return modelCommonFactory.createStructure(rootJobCiId);
	}

	@Override
	public SnapshotNode getSnapshotLatest(String jobCiId, boolean subTree) {
		return snapshotsFactory.createSnapshot(jobCiId);
	}

	//  TODO: implement
	@Override
	public SnapshotNode getSnapshotByNumber(String jobCiId, String buildCiId, boolean subTree) {
		return null;
	}

	@Override
	public void runPipeline(String jobCiId, String originalBody) {
		SBuildType buildType = octaneTeamCityPlugin.getProjectManager().findBuildTypeByExternalId(jobCiId);
		if (buildType != null) {
			buildType.addToQueue("ngaRemoteExecution");
		}
	}

	//TODO: implement: fill build history
	@Override
	public BuildHistory getHistoryPipeline(String jobCiId, String originalBody) {
		return DTOFactory.getInstance().newDTO(BuildHistory.class);
	}

	@Override
	public TestsResult getTestsResult(String jobId, String buildNumber) {
		TestsResult result = null;
		if (jobId != null && buildNumber != null) {
			SBuildType buildType = octaneTeamCityPlugin.getProjectManager().findBuildTypeByExternalId(jobId);
			if (buildType != null) {
				Build build = buildType.getBuildByBuildNumber(buildNumber);
				if (build != null && build instanceof SFinishedBuild) {
					List<TestRun> tests = createTestList((SFinishedBuild) build);
					if (tests != null && !tests.isEmpty()) {
						BuildContext buildContext = dtoFactory.newDTO(BuildContext.class)
								.setJobId(build.getBuildTypeExternalId())
								.setJobName(build.getBuildTypeName())
								.setBuildId(String.valueOf(build.getBuildId()))
								.setBuildName(build.getBuildNumber())
								.setServerId(octaneTeamCityPlugin.getConfig().getIdentity());
						result = dtoFactory.newDTO(TestsResult.class)
								.setBuildContext(buildContext)
								.setTestRuns(tests);
					}
				}
			}
		}
		return result;
	}

	private List<TestRun> createTestList(SFinishedBuild build) {
		List<TestRun> result = new ArrayList<TestRun>();
		BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
		for (STestRun testRun : stats.getTests(null, BuildStatistics.Order.NATURAL_ASC)) {
			TestRunResult testResultStatus = null;
			if (testRun.isIgnored()) {
				testResultStatus = TestRunResult.SKIPPED;
			} else if (testRun.getStatus().isFailed()) {
				testResultStatus = TestRunResult.FAILED;
			} else if (testRun.getStatus().isSuccessful()) {
				testResultStatus = TestRunResult.PASSED;
			}

			TestRun tr = dtoFactory.newDTO(TestRun.class)
					.setModuleName("")
					.setPackageName(testRun.getTest().getName().getPackageName())
					.setClassName(testRun.getTest().getName().getClassName())
					.setTestName(testRun.getTest().getName().getTestMethodName())
					.setResult(testResultStatus)
					.setStarted(build.getStartDate().getTime())
					.setDuration((long) testRun.getDuration());
			result.add(tr);
		}

		return result;
	}

	private boolean isProxyNeeded(String targetHost) {
		boolean result = false;
		Map<String, String> propertiesMap = parseProperties(System.getenv("TEAMCITY_SERVER_OPTS"));
		if (propertiesMap.get("Dhttps.proxyHost") != null) {
			result = true;
			if (targetHost != null) {
				for (String noProxyHost : getNoProxyHosts()) {
					if (targetHost.contains(noProxyHost)) {
						result = false;
						break;
					}
				}
			}
		}
		return result;
	}

	private Map<String, String> parseProperties(String internalProperties) {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		if (internalProperties != null) {
			String[] properties = internalProperties.split(" -");
			for (String str : Arrays.asList(properties)) {
				String[] split = str.split("=");
				if (split.length == 2) {
					propertiesMap.put(split[0], split[1]);
				}
			}
		}
		return propertiesMap;
	}

	//  TODO: when no proxy locations management will be available - use that list here
	private List<String> getNoProxyHosts() {
		return Arrays.asList("localhost.emea.hpqcorp.net");
	}
}
