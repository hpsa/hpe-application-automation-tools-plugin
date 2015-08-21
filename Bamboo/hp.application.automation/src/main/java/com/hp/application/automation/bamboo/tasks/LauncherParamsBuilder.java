package com.hp.application.automation.bamboo.tasks;

import com.hp.application.automation.tools.common.EncryptionUtils;
import com.hp.application.automation.tools.common.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

	private final List<String> requiredParameters = Arrays.asList("almRunHost");

	private Properties properties;

	public LauncherParamsBuilder()
	{
		properties = new Properties();
	}
	
	public void setRunType(RunType runType)
	{
		setParamValue("runType", runType.toString());
	}

	public void setAlmServerUrl(String almServerUrl)
	{
		setParamValue("almServerUrl", almServerUrl);
	}

	private void setParamValue(String paramName, String paramValue) {

		if(StringUtils.isNullOrEmpty(paramValue)) {
			if(!requiredParameters.contains(paramName))
				properties.remove(paramName);
			else
				properties.put(paramName, "");
		}
		else {
			properties.put(paramName, paramValue);
		}
	}

	public void setAlmUserName(String almUserName)
	{
		setParamValue("almUserName", almUserName);
	}

	public void setAlmPassword(String almPassword)
	{
		String encAlmPass;
		try {

			encAlmPass =
					EncryptionUtils.Encrypt(
							almPassword,
							EncryptionUtils.getSecretKey());

			properties.put("almPassword", encAlmPass);

		} catch (Exception e) {

		}
	}
	
	public void setAlmDomain(String almDomain)
	{
		setParamValue("almDomain", almDomain);
	}
	
	public void setAlmProject(String almProject)
	{
		setParamValue("almProject", almProject);
	}
	
	public void setAlmRunMode(AlmRunMode almRunMode)
	{
		properties.put("almRunMode", almRunMode != null ? almRunMode.toString() : "");
	}
	
	public void setAlmTimeout(String almTimeout)
	{
		setParamValue("almTimeout", almTimeout);
	}
	
	public void setTestSet(int index, String testSet)
	{
		setParamValue("TestSet" + index, testSet);
	}

	public void setAlmTestSet(String testSets)
	{
		setParamValue("almTestSets", testSets);
	}

	public void setAlmRunHost(String host)
	{
		setParamValue("almRunHost", host);
	}

	public void setTest(int index, String test)
	{
		setParamValue("Test" + index, test);
	}

	public void setPerScenarioTimeOut(String perScenarioTimeOut)
	{
		setParamValue("PerScenarioTimeOut", perScenarioTimeOut);
	}

	public Properties getProperties()
	{
		return properties;
	}
	
}
