package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import org.jetbrains.annotations.NotNull;
import java.util.Properties;
import java.lang.Runtime;
import java.lang.Process;
import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AlmLabEnvPrepare implements TaskType {
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private final static String HpToolsAborter_SCRIPT_NAME = "HpToolsAborter.exe";
	private String ResultFilename = "ApiResults.xml";
	private String ParamFileName = "ApiRun.txt";

	@NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
//        return TaskResultBuilder.create(taskContext).success().build();
		
		Properties mergedProperties = new Properties();

		Date now = new Date();
		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		String time = formatter.format(now);

		ParamFileName = "props" + time + ".txt";
		ResultFilename = "Results" + time + ".xml";

		mergedProperties.put("runType", RunType.FileSystem.toString());
		mergedProperties.put("resultsFilename", ResultFilename);

		String wd = TaskContext.getWorkingDirectory();
		
		buildLogger.addErrorLogEntry(wd);
		
		String launcherPath;
		String error = extractBinaryResource(wd, HpToolsLauncher_SCRIPT_NAME, launcherPath); 
		if (error != "")
		{
			buildLogger.addErrorLogEntry(error);
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		}

		String aborterPath;
		error = extractBinaryResource(wd, HpToolsAborter_SCRIPT_NAME, aborterPath); 
		if (error != "")
		{
			buildLogger.addErrorLogEntry(error);
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		}
	
		String propsSerialization = stream.toString();

		try {
			int retCode = run(launcherPath, ParamFileName);
			if (retCode == 3)
			{
				throw new InterruptedException();
			}
			else if (retCode > 0)
			{
				return TaskResultBuilder.create(taskContext).failed().build();
			}
		} catch (IOException ioe) {
			buildLogger.addErrorLogEntry(ioe.getMessage(), ioe);
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		} catch (InterruptedException e) {
			buildLogger.addErrorLogEntry("Abborted by user. Aborting process.");
			try {
				abort(aborterPath, ParamFileName);
			} catch (IOException ioe) {
				buildLogger.addErrorLogEntry(ioe.getMessage(), ioe);
				return TaskResultBuilder.create(taskContext).failedWithError().build();
			}
		} catch (InterruptedException e1) {
			buildLogger.addErrorLogEntry(ioe.getMessage(), e1);
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		}
		
		return TaskResultBuilder.create(taskContext).success().build();
    }
	
	private String extractBinaryResource(final pathToExtract, final String resourceName, String resourceName)
	{
		InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
        	stream = AlmLabEnvPrepare.class.getResourceAsStream(internalPath);
            if(stream == null) {
                return "Cannot get resource \"" + internalPath + "\" from Jar file.";
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            String jarFolder = new File(AlmLabEnvPrepare.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            stream resStreamOut = new FileOutputStream(jarFolder + "/" + "com/hp/application/automation/tbamboo/resources/" + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            return ex.getMessage();
        } finally {
            stream.close();
            resStreamOut.close();
        }
	}
	
	public static int run(String launcherPath)
	{
		String args = {launcherPath, "arg1", "-paramfile", }; 
	    Process p = Runtime.getRuntime().exec(args);
        
	    return p.waitFor();
	}
}
