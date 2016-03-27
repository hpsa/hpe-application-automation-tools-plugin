package com.hp.octane.plugins.jetbrains.teamcity;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.general.CIPluginInfo;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfig;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p/>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class TeamCityPluginServicesImpl implements CIPluginServices {
	private static final Logger logger = Logger.getLogger(TeamCityPluginServicesImpl.class.getName());
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String pluginVersion = "9.1.5";

	@Autowired
	private NGAPlugin ngaPlugin;
	@Autowired
	private SBuildServer buildServer;
	@Autowired
	private ModelFactory modelFactory;

	@Override
	public CIServerInfo getServerInfo() {
		return dtoFactory.newDTO(CIServerInfo.class)
				.setInstanceId(ngaPlugin.getConfig().getIdentity())
				.setInstanceIdFrom(ngaPlugin.getConfig().getIdentityFromAsLong())
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
	public File getAllowedNGAStorage() {
		return null;
	}

	@Override
	public NGAConfiguration getNGAConfiguration() {
		NGAConfigStructure config = ngaPlugin.getConfig();
		return dtoFactory.newDTO(NGAConfiguration.class)
				.setUrl(config.getLocation())
				.setSharedSpace(config.getSharedSpace())
				.setApiKey(config.getUsername())
				.setSecret(config.getSecretPassword());
	}

	@Override
	public CIProxyConfiguration getProxyConfiguration() {
		CIProxyConfiguration result = null;
		if (isProxyNeeded()) {
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
		return modelFactory.CreateProjectList();
	}

	@Override
	public PipelineNode getPipeline(String rootJobCiId) {
		return modelFactory.createStructure(rootJobCiId);
	}

	@Override
	public SnapshotNode getSnapshotLatest(String jobCiId, boolean subTree) {
		return modelFactory.createSnapshot(jobCiId);
	}

	//  TODO: implement
	@Override
	public SnapshotNode getSnapshotByNumber(String jobCiId, String buildCiId, boolean subTree) {
		return null;
	}

	@Override
	public void runPipeline(String jobCiId, String originalBody) {
		SBuildType buildType = ngaPlugin.getProjectManager().findBuildTypeByExternalId(jobCiId);
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
	public TestResult getTestResults(String ciJobRefId, String ciBuildRefId) {
		TestResult result = null;
		if(ciJobRefId != null && ciBuildRefId != null) {
			Build build = NGAPlugin.getInstance().getProjectManager().findBuildTypeByExternalId(ciJobRefId).getBuildByBuildNumber(ciBuildRefId);
			BuildStatistics stats = ((SFinishedBuild) build).getBuildStatistics(new BuildStatisticsOptions());
			List<STestRun> tests = stats.getTests(null, BuildStatistics.Order.NATURAL_ASC);
			BuildContext buildContext = dtoFactory.newDTO(BuildContext.class)
					.setBuildId(build.getBuildId())
					.setBuildType(build.getBuildType().getName())
					.setServer(NGAPlugin.getInstance().getConfig().getIdentity());
			TestRun[] testArr = createTestList(tests, build.getStartDate().getTime());
			result = dtoFactory.newDTO(TestResult.class)
					//.setBuildContext(buildContext)
					.setTestRuns(testArr);
		}
		return result;

	}

	private TestRun[] createTestList(List<STestRun> tests, long startingTime) {

		List<TestRun> testList = new ArrayList<TestRun>();
		for (STestRun testRun : tests) {
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
					.setStarted(startingTime)
					.setDuration(testRun.getDuration());


			testList.add(tr);
		}
		TestRun[] testArr = testList.toArray(new TestRun[testList.size()]);

		return testArr;
	}

	private static boolean isProxyNeeded() {
		boolean result = false;
		Map<String, String> propertiesMap = parseProperties(System.getenv("TEAMCITY_SERVER_OPTS"));
		if (propertiesMap.get("Dhttps.proxyHost") != null) {
			String proxyHost = propertiesMap.get("Dhttps.proxyHost");
			//  TODO: when no proxy locations management will be available - check against that list here and make a decision
		}
		return result;
	}

	private static Map<String, String> parseProperties(String internalProperties) {
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
}
