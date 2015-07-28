package com.hp.application.automation.bamboo.tasks;

import java.util.Properties;

/*
 * 	        runType=<Alm/FileSystem/LoadRunner>
	        almServerUrl=http://<server>:<port>/qcbin
	        almUserName=<user>
	        almPassword=<password>
	        almDomain=<domain>
	        almProject=<project>
	        almRunMode=<RUN_LOCAL/RUN_REMOTE/RUN_PLANNED_HOST>
	        almTimeout=<-1>/<numberOfSeconds>
	        almRunHost=<hostname>
	        TestSet<number starting at 1>=<testSet>/<AlmFolder>
	        Test<number starting at 1>=<testFolderPath>/<a Path ContainingTestFolders>/<mtbFilePath>

 */
public class LauncherParamsBuilder {

	private Properties properties;
	
	public LauncherParamsBuilder()
	{
		properties = new Properties();
	}
	
	public void setRunType(RunType runType)
	{
		properties.put("runType", runType.toString());
	}

	public void setAlmServerUrl(String almServerUrl)
	{
		properties.put("almServerUrl", almServerUrl);
	}
	
	public void setAlmUserName(String almUserName)
	{
		properties.put("almUserName", almUserName);
	}

	//TODO: encryption
	public void setAlmPassword(String almPassword)
	{
		properties.put("almPassword", almPassword);
	}
	
	public void setAlmDomain(String almDomain)
	{
		properties.put("almDomain", almDomain);
	}
	
	public void setAlmProject(String almProject)
	{
		properties.put("almProject", almProject);
	}
	
	public void setAlmRunMode(AlmRunMode almRunMode)
	{
		properties.put("almRunMode", almRunMode.toString());
	}
	
	public void setAlmTimeout(String almTimeout)
	{
		properties.put("almTimeout", almTimeout);
	}
	
	public void setTestSet(int index, String testSet)
	{
		properties.put("TestSet"+index, testSet);
	}
	
	public void setTest(int index, String test)
	{
		properties.put("Test"+index, test);
	}

	public void setPerScenarioTimeOut(String perScenarioTimeOut)
	{
		properties.put("PerScenarioTimeOut", perScenarioTimeOut);
	}
	
	public Properties getProperties()
	{
		return properties;
	}
	
}
