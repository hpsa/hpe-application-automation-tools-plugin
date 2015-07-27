package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.ctc.wstx.util.StringUtil;

import org.jetbrains.annotations.NotNull;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.Date;
import java.lang.Runtime;
import java.lang.Process;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractLauncherTask implements TaskType {
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private final static String HpToolsAborter_SCRIPT_NAME = "HpToolsAborter.exe";

	protected abstract Properties getTaskProperties(final TaskContext taskContext) throws Exception;
	
	@NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
//		return TaskResultBuilder.create(taskContext).success().build();
		
		Properties mergedProperties = new Properties();
		try
		{
			Properties customTaskProperties = getTaskProperties(taskContext);
			if (customTaskProperties != null)
			{
				mergedProperties.putAll(customTaskProperties);
			}
		}
		catch (Exception e) {
			buildLogger.addErrorLogEntry(e.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		}

		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		Date time = new Date();
		String paramFileName = "props" + formatter.format(time) + ".txt";
		String resultsFileName = "Results" + time + ".xml";

		mergedProperties.put("runType", RunType.FileSystem.toString());
		mergedProperties.put("resultsFilename", resultsFileName);

		final ConfigurationMap map = taskContext.getConfigurationMap();
		
		File wd = taskContext.getWorkingDirectory();
//		buildLogger.addErrorLogEntry("||||||" + wd.getAbsolutePath());
//    	String v1 = map.get(AbstractLauncherTaskConfigurator.BUILD_WORKING_DIR);
//	    buildLogger.addBuildLogEntry("||||||" + v1);
//		return TaskResultBuilder.create(taskContext).success().build();
		
		File paramsFile = new File(wd, paramFileName);
		if (paramsFile.exists()){
			paramsFile.delete();
		}
		try {
			paramsFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(paramsFile);
			mergedProperties.store(fos, "");
			fos.close();
		}
		catch (Exception e) {
			buildLogger.addErrorLogEntry(e.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		}
		
		String launcherPath = "";
		String aborterPath = "";
		try {
			String error = extractBinaryResource(wd, HpToolsLauncher_SCRIPT_NAME, launcherPath); 
			if (error != "")
			{
				buildLogger.addErrorLogEntry(error);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
	
			error = extractBinaryResource(wd, HpToolsAborter_SCRIPT_NAME, aborterPath); 
			if (error != "")
			{
				buildLogger.addErrorLogEntry(error);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
		}
		catch (IOException ioe){
			buildLogger.addErrorLogEntry(ioe.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		}
		try {
			int retCode = run(launcherPath, paramFileName);
			if (retCode == 3)
			{
				throw new InterruptedException();
			}
			else if (retCode > 0)
			{
				return TaskResultBuilder.create(taskContext).failed().build();
			}
		} 
		catch (IOException ioe) {
			buildLogger.addErrorLogEntry(ioe.getMessage(), ioe);
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		} 
		catch (InterruptedException e) {
			buildLogger.addErrorLogEntry("Abborted by user. Aborting process.");
			try {
				run(aborterPath, paramFileName);
			}
			catch (IOException ioe) {
				buildLogger.addErrorLogEntry(ioe.getMessage(), ioe);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
			catch (InterruptedException ie) {
				buildLogger.addErrorLogEntry(ie.getMessage(), ie);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
		}
		
		return TaskResultBuilder.create(taskContext).success().build();
    }
	
	private String extractBinaryResource(final File pathToExtract, final String resourceName, String resourcePath) throws IOException	{
		InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
            String jarFolder = new File(AlmLabEnvPrepareTask.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            resourcePath = jarFolder + "/" + "com/hp/application/automation/tbamboo/resources/" + resourceName; 

            stream = AlmLabEnvPrepareTask.class.getResourceAsStream(resourcePath);
            if(stream == null) {
                return "Cannot get resource \"" + resourcePath + "\" from Jar file.";
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            File resultPath = new File(pathToExtract, resourceName);
            resourcePath = resultPath.getAbsolutePath();
            resStreamOut = new FileOutputStream(resultPath);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
        	resourcePath = "";
            return ex.getMessage();
        } finally {
            stream.close();
            resStreamOut.close();
        }
        return "";
	}
	
	private int run(String launcherPath, String paramFile) throws IOException, InterruptedException {
		String args[] = {launcherPath, "-paramfile", paramFile}; 
	    Process p = Runtime.getRuntime().exec(args);

	    return p.waitFor();
	}
}
