package com.hp.application.automation.bamboo.tasks;

import java.util.Properties;

import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.core.util.StringUtils;

public class RunFromAlmTask extends AbstractLauncherTask {

	private final CapabilityContext _capabilityContext;

	public RunFromAlmTask(CapabilityContext capabilityContext){
		_capabilityContext = capabilityContext;
	}

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
		final ConfigurationMap map = taskContext.getConfigurationMap();
		LauncherParamsBuilder builder = new LauncherParamsBuilder();

		builder.setRunType(RunType.Alm);

		final String almServer = map.get(RunFromAlmTaskConfigurator.ALM_SERVER);
		final String almServerPath = _capabilityContext.getCapabilityValue(AlmServerCapabilityHelper.GetCapabilityKey(almServer));
		builder.setAlmServerUrl(almServerPath);

		builder.setAlmUserName(map.get(RunFromAlmTaskConfigurator.USER_NAME));
		builder.setAlmPassword(map.get(RunFromAlmTaskConfigurator.PASSWORD));
		builder.setAlmDomain(map.get(RunFromAlmTaskConfigurator.DOMAIN));
		builder.setAlmProject(map.get(RunFromAlmTaskConfigurator.PROJECT));
		builder.setAlmRunMode(AlmRunMode.RUN_LOCAL);
		builder.setAlmRunHost("");

		String timeout = map.get(RunFromAlmTaskConfigurator.TIMEOUT);
		if (org.apache.commons.lang.StringUtils.isEmpty(timeout)) {
			builder.setAlmTimeout(RunFromAlmTaskConfigurator.DEFAULT_TIMEOUT);
		} else {
			builder.setAlmTimeout(timeout);
		}

		String splitMarker = "\n";
		String almTestSets = map.get(RunFromAlmTaskConfigurator.TESTS_PATH);
		if (!org.apache.commons.lang.StringUtils.isEmpty(almTestSets)) {

			String[] testSetsArr = almTestSets.replaceAll("\r", "").split(
					"\n");

			int i = 1;

			for (String testSet : testSetsArr) {
				builder.setTestSet(i, testSet);
				i++;
			}
		} else {
			builder.setAlmTestSet("");
		}
		return builder.getProperties();
	}
    
}
