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

import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import java.util.*;
import com.atlassian.struts.TextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunFromFileSystemTask extends AbstractLauncherTask {

	private final ArtifactManager artifactManager;
	private final TextProvider _textProvider;

	public RunFromFileSystemTask(@NotNull final TestCollationService testCollationService, @NotNull ArtifactManager artifactManager, @NotNull TextProvider textProvider)
	{
		super(testCollationService);

		this.artifactManager = artifactManager;
		_textProvider = textProvider;
	}

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
    	final ConfigurationMap map = taskContext.getConfigurationMap();        
    	LauncherParamsBuilder builder = new LauncherParamsBuilder(); 
   	
    	builder.setRunType(RunType.FileSystem);
        String timeout = map.get(RunFromFileSystemTaskConfigurator.TIMEOUT);
        builder.setPerScenarioTimeOut(timeout);

    	String splitMarker = "\n";
    	String tests = map.get(RunFromFileSystemTaskConfigurator.TESTS_PATH);
    	String[] testNames;
    	if(tests == null)
    	{
    		testNames = new String[0];
    	}
    	else
    	{
    		testNames = tests.split(splitMarker);
    	}
        
        for(int i=0; i < testNames.length; i++)
        {
        	builder.setTest(i+1, testNames[i]);
        }
    	return builder.getProperties();
	}

	@Override
	protected void uploadArtifacts(final TaskContext taskContext)
	{
		TestResultHelper.ResultTypeFilter resultsFilter = getResultTypeFilter(taskContext);

		if(resultsFilter != null)
		{
			final BuildLogger buildLogger = taskContext.getBuildLogger();
			//TODO: Use format from resources. TextProvider returns null on remote agents.
			//final String resultNameFormat = _textProvider.getText(RunFromFileSystemTaskConfigurator.ARTIFACT_NAME_FORMAT_STRING);
			final String resultNameFormat = "%s Result";

			Collection<ResultInfoItem> resultsPathes = TestResultHelper.getTestResults(getResultsFile(), resultsFilter, resultNameFormat, taskContext, buildLogger);
			TestResultHelper.zipResults(resultsPathes, buildLogger);
		}
	}

	@Nullable
	private TestResultHelper.ResultTypeFilter getResultTypeFilter(final TaskContext taskContext)
	{
		String publishMode = taskContext.getConfigurationMap().get(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_PARAM);

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_FAILED_VALUE))
		{
			return TestResultHelper.ResultTypeFilter.FAILED;
		}

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_ALWAYS_VALUE))
		{
			return TestResultHelper.ResultTypeFilter.All;
		}

		return null;
	}
}
