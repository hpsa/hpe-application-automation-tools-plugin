package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
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

public class AlmLabEnvPrepare implements TaskType {
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private final static String HpToolsAborter_SCRIPT_NAME = "HpToolsAborter.exe";
	private String ParamFileName = "ApiRun.txt";

	@NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
		
		Properties mergedProperties = new Properties();

		Date now = new Date();
		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		String time = formatter.format(now);

		this.ParamFileName = "props" + time + ".txt";

		mergedProperties.put("runType", RunType.FileSystem.toString());
		mergedProperties.put("resultsFilename", "Results" + time + ".xml");

		File wd = taskContext.getWorkingDirectory();

		buildLogger.addErrorLogEntry(wd.getAbsolutePath());
		
		File paramsFile = new File(wd, this.ParamFileName);
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
			int retCode = run(launcherPath, this.ParamFileName);
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
				run(aborterPath, this.ParamFileName);
			}
			catch (IOException ioe) {
				buildLogger.addErrorLogEntry(ioe.getMessage(), ioe);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
			catch (InterruptedException e1) {
				buildLogger.addErrorLogEntry(e1.getMessage(), e1);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
		}
		
		return TaskResultBuilder.create(taskContext).success().build();
    }
	
	private String extractBinaryResource(final File pathToExtract, final String resourceName, String resourcePath) throws IOException	{
		InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
            String jarFolder = new File(AlmLabEnvPrepare.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            resourcePath = jarFolder + "/" + "com/hp/application/automation/tbamboo/resources/" + resourceName; 

            stream = AlmLabEnvPrepare.class.getResourceAsStream(resourcePath);
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
		String args[] = {launcherPath, "arg1", "-paramfile", paramFile}; 
	    Process p = Runtime.getRuntime().exec(args);
        
	    return p.waitFor();
	}
}
