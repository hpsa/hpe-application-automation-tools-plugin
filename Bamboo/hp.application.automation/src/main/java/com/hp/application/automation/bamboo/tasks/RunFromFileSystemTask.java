package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;

import java.io.File;
import java.io.IOException;
import java.util.*;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;
import com.atlassian.struts.TextProvider;
import com.hpe.application.automation.tools.common.sdk.DirectoryZipHelper;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.texen.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.io.FileUtilities;

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
		TestRusultsHelperFileSystem.ResultTypeFilter resultsFilter = getResultTypeFilter(taskContext);

		if(resultsFilter == null)
		{
			return;
		}
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String resultNameFormat = ResourceManager.getText(RunFromFileSystemTaskConfigurator.ARTIFACT_NAME_FORMAT_STRING);

		Collection<ResultInfoItem> resultsPathes = TestRusultsHelperFileSystem.getTestResults(getResultsFile(), resultsFilter, resultNameFormat, taskContext, buildLogger);

		for(ResultInfoItem resultItem : resultsPathes)
		{
			String dir = resultItem.getSourceDir().getPath();
			File f = new File(dir, RESULT_HTML_REPORT_FILE_NAME);
			if (f.exists())
			{
				createReportHtml(resultItem, buildLogger);
			}
			else {
				zipResult(resultItem, buildLogger);
			}
		}
		//TestRusultsHelperFileSystem.zipResults(resultsPathes, buildLogger);
	}

	private void zipResult(ResultInfoItem resultItem, BuildLogger logger)
	{
		try {
			DirectoryZipHelper.zipFolder(resultItem.getSourceDir().getPath(), resultItem.getZipFile().getPath());
		} catch (IOException ex) {
			logger.addBuildLogEntry(ex.getMessage());
		} catch (Exception ex) {
			logger.addBuildLogEntry(ex.getMessage());
		}
	}

	private void createReportHtml(ResultInfoItem resultItem, BuildLogger logger)
	{
		File contentDir = resultItem.getSourceDir();
		if (contentDir == null || !contentDir.isDirectory()) {
			return;
		}
		File parentDir = contentDir.getParentFile();
		if (parentDir == null || !parentDir.isDirectory()){
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
					"            window.location = \"file:./" + contentDir.getName() + "/" + RESULT_HTML_REPORT_FILE_NAME + "\";\n" +
					"        }\n" +
					"        window.onload = codeAddress;\n" +
					"        </script>\n" +
					"    </head>\n" +
					"    <body>\n" +
					"   \n" +
					"    </body>\n" +
					"</html>";

		try {
			FileUtils.writeStringToFile(new File(parentDir, HTML_REPORT_FILE_NAME), content);
		} catch (IOException e) {
		}
	}

	@Nullable
	private TestRusultsHelperFileSystem.ResultTypeFilter getResultTypeFilter(final TaskContext taskContext)
	{
		String publishMode = taskContext.getConfigurationMap().get(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_PARAM);

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_FAILED_VALUE))
		{
			return TestRusultsHelperFileSystem.ResultTypeFilter.FAILED;
		}

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_ALWAYS_VALUE))
		{
			return TestRusultsHelperFileSystem.ResultTypeFilter.All;
		}

		return null;
	}
}
