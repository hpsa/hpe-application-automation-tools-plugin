package com.hp.octane.plugins.bamboo.octane;

import com.atlassian.bamboo.agent.classserver.AgentServerManager;
import com.atlassian.bamboo.applinks.ImpersonationService;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.plan.PlanExecutionManager;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.plan.cache.ImmutableTopLevelPlan;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.pipelines.BuildHistory;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.tests.TestsResult;
import com.hp.octane.integrations.spi.CIPluginServices;

import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class BambooPluginServices implements CIPluginServices {
	private static final Logger log = LoggerFactory.getLogger(BambooPluginServices.class);
	private static final String PLUGIN_VERSION = "1.0.0-SNAPSHOT";

	private CachedPlanManager planMan;

	private ImpersonationService impService;
	private PlanExecutionManager planExecMan;

	private static DTOConverter CONVERTER = DefaultOctaneConverter.getInstance();
	private PluginSettingsFactory settingsFactory;
	public static Map<String, TestsResult> TEST_RESULTS = Collections.synchronizedMap(new HashMap<String, TestsResult>());

	public BambooPluginServices(PluginSettingsFactory settingsFactory) {
		super();
		this.settingsFactory = settingsFactory;
		this.planExecMan = ComponentLocator.getComponent(PlanExecutionManager.class);
		this.planMan = ComponentLocator.getComponent(CachedPlanManager.class);
		this.impService = ComponentLocator.getComponent(ImpersonationService.class);
	}

	// return null as we don't have file storage available
	public File getAllowedOctaneStorage() {
		return null;
	}

	public BuildHistory getHistoryPipeline(String arg0, String arg1) {
		log.info("Get build history pipeline " + arg0 + " , " + arg1);
		return null;
	}

	public CIJobsList getJobsList(boolean arg0) {
		log.info("Get jobs list");
		Callable<List<ImmutableTopLevelPlan>> plansGetter = impService.runAsUser(getRunAsUser(), new Callable<List<ImmutableTopLevelPlan>>() {

			public List<ImmutableTopLevelPlan> call() throws Exception {
				return planMan.getPlans();
			}
		});
		try {
			List<ImmutableTopLevelPlan> plans = plansGetter.call();
			return CONVERTER.getRootJobsList(plans);
		} catch (Exception e) {
			log.error("Error while retrieving top level plans", e);
		}
		return CONVERTER.getRootJobsList(Collections.<ImmutableTopLevelPlan>emptyList());
	}

	public OctaneConfiguration getOctaneConfiguration() {
		log.info("getOctaneConfiguration");
		PluginSettings settings = settingsFactory.createGlobalSettings();
		String url = String.valueOf(settings.get(OctaneConfigurationKeys.NGA_URL));
		String accessKey = String.valueOf(settings.get(OctaneConfigurationKeys.API_KEY));
		String secret = String.valueOf(settings.get(OctaneConfigurationKeys.API_SECRET));
		return OctaneSDK.getInstance().getConfigurationService().buildConfiguration(url, accessKey, secret);
	}

	public PipelineNode getPipeline(String pipelineId) {
		log.info("get pipeline " + pipelineId);
		ImmutableTopLevelPlan plan = planMan.getPlanByKey(PlanKeys.getPlanKey(pipelineId), ImmutableTopLevelPlan.class);
		return CONVERTER.getRootPipelineNodeFromTopLevelPlan(plan);
	}

	public CIPluginInfo getPluginInfo() {
		log.info("get plugin info");
		return DTOFactory.getInstance().newDTO(CIPluginInfo.class).setVersion(PLUGIN_VERSION);
	}

	public CIProxyConfiguration getProxyConfiguration(String targetHost) {
		log.info("get proxy configuration");
		CIProxyConfiguration result = null;
		if (isProxyNeeded(targetHost)) {
			log.info("proxy is required for host " + targetHost);
			return CONVERTER.getProxyCconfiguration(System.getProperty("https.proxyHost"),
					Integer.parseInt(System.getProperty("https.proxyPort")), System.getProperty("https.proxyUser", ""),
					System.getProperty("https.proxyPassword", ""));
		}
		return result;
	}

	private boolean isProxyNeeded(String targetHost) {
		String[] nonProxyHosts = System.getProperty("http.nonProxyHosts", "").split("\\|");
		return Arrays.asList(nonProxyHosts).contains(targetHost);
	}

	public CIServerInfo getServerInfo() {
		log.info("get ci server info");
		String instanceId = String.valueOf(
				ComponentLocator.getComponent(AgentServerManager.class).getFingerprint().getServerFingerprint());
		// String instanceId = "0";
		String baseUrl = ComponentLocator.getComponent(AdministrationConfigurationAccessor.class)
				.getAdministrationConfiguration().getBaseUrl();

		return CONVERTER.getServerInfo(baseUrl, instanceId);
	}

	public SnapshotNode getSnapshotByNumber(String pipeline, String snapshot, boolean arg2) {
		// TODO implement get snapshot
		log.info("get snapshot by number " + pipeline + " , " + snapshot);
		return null;
	}

	public SnapshotNode getSnapshotLatest(String pipeline, boolean arg1) {
		log.info("get latest snapshot  for pipeline " + pipeline);
		ImmutableTopLevelPlan plan = planMan.getPlanByKey(PlanKeys.getPlanKey(pipeline), ImmutableTopLevelPlan.class);
		return CONVERTER.getSnapshot(plan, plan.getLatestResultsSummary());
	}

	//  [YG] TODO: implement this one for queued push tests results implementation
	public TestsResult getTestsResult(String pipeline, String build) {
		return null;
	}

	public void runPipeline(final String pipeline, String parameters) {
		// TODO implement parameters conversion
		// only execute runnable plans
		log.info("starting pipeline run");

		Callable<String> impersonated = impService.runAsUser(getRunAsUser(), new Callable<String>() {

			public String call() throws Exception {
				BambooUserManager um = ComponentLocator.getComponent(BambooUserManager.class);
				BambooUser user = um.getBambooUser(getRunAsUser());
				ImmutableChain chain = planMan.getPlanByKey(PlanKeys.getPlanKey(pipeline), ImmutableChain.class);
				log.info("plan key is " + chain.getPlanKey().getKey());
				log.info("build key is " + chain.getBuildKey());
				log.info("chain key is " + chain.getKey());
				planExecMan.startManualExecution(chain, user, new HashMap<String, String>(),
						new HashMap<String, String>());
				return null;
			}
		});
		try {
			impersonated.call();
		} catch (Exception e) {
			log.info("Error impersonating for plan execution", e);
		}

	}

	private String getRunAsUser() {
		PluginSettings settings = settingsFactory.createGlobalSettings();
		return String.valueOf(settings.get(OctaneConfigurationKeys.USER_TO_USE));
	}

}
