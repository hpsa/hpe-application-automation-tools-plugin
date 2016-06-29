/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

import java.util.Properties;

import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;
import org.jetbrains.annotations.NotNull;

public class RunFromAlmTask extends AbstractLauncherTask {

	private final String LINK_SEARCH_FILTER = "EntityID=";
	private static I18nBean i18nBean;

	public RunFromAlmTask(@NotNull final TestCollationService testCollationService, @NotNull I18nBeanFactory i18nBeanFactory)
	{
		super(testCollationService);
		i18nBean = i18nBeanFactory.getI18nBean();
	}

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
		final ConfigurationMap map = taskContext.getConfigurationMap();
		LauncherParamsBuilder builder = new LauncherParamsBuilder();

		builder.setRunType(RunType.Alm);

		final String almServerPath = map.get(RunFromAlmTaskConfigurator.ALM_SERVER);
		builder.setAlmServerUrl(almServerPath);

		builder.setAlmUserName(map.get(RunFromAlmTaskConfigurator.USER_NAME));
		builder.setAlmPassword(map.get(RunFromAlmTaskConfigurator.PASSWORD));
		builder.setAlmDomain(map.get(RunFromAlmTaskConfigurator.DOMAIN));
		builder.setAlmProject(map.get(RunFromAlmTaskConfigurator.PROJECT));

		String runMode = map.get(RunFromAlmTaskConfigurator.RUN_MODE);
		if(runMode.equals(RunFromAlmTaskConfigurator.RUN_LOCALLY_PARAMETER))
		{
			builder.setAlmRunMode(AlmRunMode.RUN_LOCAL);
		}
		else if(runMode.equals(RunFromAlmTaskConfigurator.RUN_ON_PLANNED_HOST_PARAMETER))
		{
			builder.setAlmRunMode(AlmRunMode.RUN_PLANNED_HOST);
		}
		else if(runMode.equals(RunFromAlmTaskConfigurator.RUN_REMOTELY_PARAMETER))
		{
			builder.setAlmRunMode(AlmRunMode.RUN_REMOTE);
		}

		builder.setAlmRunHost(map.get(RunFromAlmTaskConfigurator.TESTING_TOOL_HOST));

		String timeout = map.get(RunFromAlmTaskConfigurator.TIMEOUT);
		if (org.apache.commons.lang.StringUtils.isEmpty(timeout)) {
			builder.setAlmTimeout(RunFromAlmTaskConfigurator.DEFAULT_TIMEOUT);
		} else {
			builder.setAlmTimeout(timeout);
		}

		String splitMarker = "\n";
		String almTestSets = map.get(RunFromAlmTaskConfigurator.TESTS_PATH);
		if (!org.apache.commons.lang.StringUtils.isEmpty(almTestSets)) {

			String[] testSetsArr = almTestSets.replaceAll("\r", "").split(splitMarker);

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

	@NotNull
	@java.lang.Override
	public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
	{
		super.execute(taskContext);
		TestResultHelperAlm.AddALMArtifacts(taskContext, this.getResultsFile(), LINK_SEARCH_FILTER, i18nBean);
		return TaskResultBuilder.create(taskContext).checkTestFailures().build();
	}
}
