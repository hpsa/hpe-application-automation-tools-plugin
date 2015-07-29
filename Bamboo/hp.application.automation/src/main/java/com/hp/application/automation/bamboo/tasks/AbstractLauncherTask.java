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

import java.io.*;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.Date;
import java.lang.Runtime;
import java.net.URI;
import java.net.URL;
import java.lang.Process;
import java.text.Format;
import java.text.SimpleDateFormat;

public abstract class AbstractLauncherTask implements TaskType {
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private final static String HpToolsAborter_SCRIPT_NAME = "HpToolsAborter.exe";

	protected abstract Properties getTaskProperties(final TaskContext taskContext) throws Exception;
	
	@NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
		
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
		String resultsFileName = "Results" + formatter.format(time) + ".xml";

		mergedProperties.put("resultsFilename", resultsFileName);

		final ConfigurationMap map = taskContext.getConfigurationMap();
		
		File wd = taskContext.getWorkingDirectory();
		
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
			String error = extractBinaryResource(wd, HpToolsLauncher_SCRIPT_NAME);
			launcherPath = (new File(wd, HpToolsLauncher_SCRIPT_NAME)).getAbsolutePath();
			buildLogger.addBuildLogEntry("********** " + launcherPath);
			
			if (!error.isEmpty())
			{
				buildLogger.addErrorLogEntry(error);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
	
			error = extractBinaryResource(wd, HpToolsAborter_SCRIPT_NAME); 
			aborterPath = (new File(wd, HpToolsAborter_SCRIPT_NAME)).getAbsolutePath();			
			buildLogger.addBuildLogEntry("********** " + aborterPath);
			buildLogger.addBuildLogEntry("********** " + error);
			if (!error.isEmpty())
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
			int retCode = run(wd, launcherPath, paramsFile.getAbsolutePath(), buildLogger);
			buildLogger.addBuildLogEntry("********** " + Integer.toString(retCode));
			if (retCode == 3)
			{
				throw new InterruptedException();
			}
			else if (retCode != 0)
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
				run(wd, aborterPath, paramsFile.getAbsolutePath(), buildLogger);
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
	
	private String extractBinaryResource(final File pathToExtract, final String resourceName) throws IOException	{
		InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
        	
        	String resourcePath = "/" + resourceName;
        	stream = this.getClass().getResourceAsStream(resourcePath);

        	if(stream == null) {
                return "Cannot get resource \"" + resourcePath + "\" from Jar file.";
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            File resultPath = new File(pathToExtract, resourceName);
            resStreamOut = new FileOutputStream(resultPath);
            
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            
        } catch (Exception ex) {
            return ex.getMessage();
        }
        
        finally {
			if (stream != null) {
				stream.close();
				resStreamOut.close();
			}
        }
        
        return "";
	}
	
	private int run(File workingDirectory, String launcherPath, String paramFile, BuildLogger logger) throws IOException, InterruptedException {
		try
		{
			ProcessBuilder builder = new ProcessBuilder(launcherPath, "-paramfile", paramFile);
			builder.directory(workingDirectory);

			Process p = builder.start();
			String line;
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = output.readLine()) != null) {
				logger.addBuildLogEntry(line);
			}
			output.close();
			return p.waitFor();
		}
		catch (Throwable t) {
			logger.addBuildLogEntry(t.getMessage());
			return -1;
		}
	}
}