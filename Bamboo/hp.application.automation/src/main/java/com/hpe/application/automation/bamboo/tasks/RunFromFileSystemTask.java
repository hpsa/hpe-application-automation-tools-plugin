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

import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;

import java.io.File;
import java.io.IOException;
import java.util.*;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;
import com.hpe.application.automation.tools.common.sdk.DirectoryZipHelper;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hpe.application.automation.tools.common.UploadApplication;
import com.hpe.application.automation.tools.common.StringUtils;
import org.apache.commons.lang.BooleanUtils;

public class RunFromFileSystemTask extends AbstractLauncherTask {

	private final String RESULT_HTML_REPORT_FILE_NAME = "run_results.html";
	private final String HTML_REPORT_FILE_NAME = "Report.html";
	private I18nBean i18nBean;

	public RunFromFileSystemTask(@NotNull final TestCollationService testCollationService, @NotNull I18nBeanFactory i18nBeanFactory)
	{
		super(testCollationService);
		i18nBean = i18nBeanFactory.getI18nBean();
	}

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
    	final ConfigurationMap map = taskContext.getConfigurationMap();        
    	LauncherParamsBuilder builder = new LauncherParamsBuilder(); 
   	
    	builder.setRunType(RunType.FileSystem);
        String timeout = map.get(RunFromFileSystemTaskConfigurator.TIMEOUT);
        builder.setPerScenarioTimeOut(timeout);

		String mcAppIdentifierName = "";
		boolean useMC = BooleanUtils.toBoolean(map.get(RunFromFileSystemTaskConfigurator.USE_MC_SETTINGS));
		if(useMC){
			String mcServerUrl = map.get(RunFromFileSystemTaskConfigurator.MCSERVERURL);
			String mcUserName = map.get(RunFromFileSystemTaskConfigurator.MCUSERNAME);
			String mcPassword = map.get(RunFromFileSystemTaskConfigurator.MCPASSWORD);
			String mcAppPath = map.get(RunFromFileSystemTaskConfigurator.MCAPPLICATIONPATH);
			String mcAppIdKey = map.get(RunFromFileSystemTaskConfigurator.MCAPPLICATIONIDKEY);
			if(!mcInfoCheck(mcServerUrl,mcUserName,mcAppPath,mcAppIdKey)){
				UploadApplication app = new UploadApplication(mcServerUrl,mcUserName,mcPassword,mcAppPath);
				mcAppIdentifierName = app.getAppIndentifier();
			}
			if(!StringUtils.isNullOrEmpty(mcAppIdentifierName) && !StringUtils.isNullOrEmpty(mcAppIdKey))
			{
				builder.setFsAppParamName(mcAppIdKey);
				builder.setIdentifierName(mcAppIdKey, mcAppIdentifierName);
			}
		}
		
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
	protected void PrepareArtifacts(final TaskContext taskContext)
	{
		TestRusultHelperFileSystem.ResultTypeFilter resultsFilter = getResultTypeFilter(taskContext);

		if(resultsFilter == null)
		{
			return;
		}
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String resultNameFormat = i18nBean.getText(RunFromFileSystemTaskConfigurator.ARTIFACT_NAME_FORMAT_STRING);

		Collection<ResultInfoItem> resultsPathes = TestRusultHelperFileSystem.getTestResults(getResultsFile(), resultsFilter, resultNameFormat, taskContext, buildLogger);

		for(ResultInfoItem resultItem : resultsPathes)
		{
			String dir = resultItem.getSourceDir().getPath();
			File f = new File(dir, RESULT_HTML_REPORT_FILE_NAME);
			if (f.exists())
			{
				prepareHtmlArtifact(resultItem, taskContext, buildLogger);
			}
			else {
				zipResult(resultItem, buildLogger);
			}
		}
		//TestRusultsHelperFileSystem.zipResults(resultsPathes, buildLogger);
	}

	private void zipResult(final ResultInfoItem resultItem, final BuildLogger logger)
	{
		try {
			DirectoryZipHelper.zipFolder(resultItem.getSourceDir().getPath(), resultItem.getZipFile().getPath());
		} catch (IOException ex) {
			logger.addBuildLogEntry(ex.getMessage());
		} catch (Exception ex) {
			logger.addBuildLogEntry(ex.getMessage());
		}
	}

	private void prepareHtmlArtifact(ResultInfoItem resultItem, final TaskContext taskContext, BuildLogger logger)
	{
		File contentDir = resultItem.getSourceDir();
		if (contentDir == null || !contentDir.isDirectory()) {
			return;
		}
		File destPath = new File(TestResultHelper.getOutputFilePath(taskContext), resultItem.getTestName());
		if (!destPath.exists() && !destPath.isDirectory()){
			destPath.mkdirs();
		}

		try {
			FileUtils.copyDirectoryToDirectory(contentDir, destPath);
		}
		catch (Exception e){
			logger.addBuildLogEntry(e.getMessage());
			return;
		}

		String content =
			"<!DOCTYPE html>\n" +
					"<html>\n" +
					"    <head>\n" +
					"        <title>Test</title>\n" +
					"        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
					"        <script type=\"text/javascript\">\n" +
					"        function codeAddress() {\n" +
					"		 	var currentUrl = window.location.toString();\n" +
					"			var replaceString = '" + contentDir.getName() + "/" + RESULT_HTML_REPORT_FILE_NAME + "';\n" +
					"		 	currentUrl = currentUrl.replace('" + HTML_REPORT_FILE_NAME + "', replaceString);\n" +
					"        	window.location = currentUrl;\n" +
					"        }\n" +
					"        window.onload = codeAddress;\n" +
					"        </script>\n" +
					"    </head>\n" +
					"    <body>\n" +
					"   \n" +
					"    </body>\n" +
					"</html>";

		try {
			FileUtils.writeStringToFile(new File(destPath, HTML_REPORT_FILE_NAME), content);
		} catch (IOException e) {
		}
	}

	@Nullable
	private TestRusultHelperFileSystem.ResultTypeFilter getResultTypeFilter(final TaskContext taskContext)
	{
		String publishMode = taskContext.getConfigurationMap().get(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_PARAM);

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_FAILED_VALUE))
		{
			return TestRusultHelperFileSystem.ResultTypeFilter.FAILED;
		}

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_ALWAYS_VALUE))
		{
			return TestRusultHelperFileSystem.ResultTypeFilter.All;
		}

		return null;
	}

    private boolean mcInfoCheck(String mcUrl, String mcUserName, String mcAppPath, String mcAppIdKey){
        return StringUtils.isNullOrEmpty(mcUrl) || StringUtils.isNullOrEmpty(mcUrl) || StringUtils.isNullOrEmpty(mcUserName) || StringUtils.isNullOrEmpty(mcAppPath) || StringUtils.isNullOrEmpty(mcAppIdKey);
    }
}
