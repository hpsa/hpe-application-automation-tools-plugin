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

import com.hpe.application.automation.tools.common.EncryptionUtils;
import com.hpe.application.automation.tools.common.StringUtils;

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

    public void setFsAppParamName(String mcAppIDKey)
    {
        setParamValue("fsAppParamName", mcAppIDKey);
    }

    public void setIdentifierName(String mcAppIDKey, String identifierName)
    {
        setParamValue(mcAppIDKey, identifierName);
    }

	public Properties getProperties()
	{
		return properties;
	}
	
}
