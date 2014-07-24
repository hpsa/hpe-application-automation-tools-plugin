// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.model;

import hudson.EnvVars;
import hudson.util.VariableResolver;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class RunFromFileSystemModel {

	private String fsTests;
	private String fsTimeout;
<<<<<<< HEAD
	private String controllerPollingInterval = "30";
	private String perScenarioTimeOut = "10";
	private String ignoreErrorStrings;
	
	
	

	@DataBoundConstructor
	public RunFromFileSystemModel(String fsTests, String fsTimeout, String controllerPollingInterval,String perScenarioTimeOut, String ignoreErrorStrings) {
=======

	@DataBoundConstructor
	public RunFromFileSystemModel(String fsTests, String fsTimeout) {
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033

		this.fsTests = fsTests;

		if (!this.fsTests.contains("\n")) {
			this.fsTests += "\n";
		}

		this.fsTimeout = fsTimeout;
<<<<<<< HEAD
		
		
		this.perScenarioTimeOut = perScenarioTimeOut;
		this.controllerPollingInterval = controllerPollingInterval;
		this.ignoreErrorStrings = ignoreErrorStrings;
		
	}
	
=======
	}
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033

	public String getFsTests() {
		return fsTests;
	}

	public String getFsTimeout() {
		return fsTimeout;
	}

<<<<<<< HEAD
	

	
	/**
	 * @return the controllerPollingInterval
	 */
	public String getControllerPollingInterval() {
		return controllerPollingInterval;
	}

	/**
	 * @param controllerPollingInterval the controllerPollingInterval to set
	 */
	public void setControllerPollingInterval(String controllerPollingInterval) {
		this.controllerPollingInterval = controllerPollingInterval;
	}

	/**
	 * @return the ignoreErrorStrings
	 */
	public String getIgnoreErrorStrings() {
		return ignoreErrorStrings;
	}


	/**
	 * @param ignoreErrorStrings the ignoreErrorStrings to set
	 */
	public void setIgnoreErrorStrings(String ignoreErrorStrings) {
		this.ignoreErrorStrings = ignoreErrorStrings;
	}



	/**
	 * @return the perScenarioTimeOut
	 */
	public String getPerScenarioTimeOut() {
		return perScenarioTimeOut;
	}

	/**
	 * @param perScenarioTimeOut the perScenarioTimeOut to set
	 */
	public void setPerScenarioTimeOut(String perScenarioTimeOut) {
		this.perScenarioTimeOut = perScenarioTimeOut;
	}

=======
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
	public Properties getProperties(EnvVars envVars,
			VariableResolver<String> varResolver) {
		return CreateProperties(envVars, varResolver);
	}

	public Properties getProperties() {
		return CreateProperties(null, null);
	}

	private Properties CreateProperties(EnvVars envVars,
			VariableResolver<String> varResolver) {
		Properties props = new Properties();
<<<<<<< HEAD
		
=======

>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
		if (!StringUtils.isEmpty(this.fsTests)) {

			String[] testsArr = this.fsTests.replaceAll("\r", "").split("\n");

			int i = 1;

			for (String test : testsArr) {
				props.put("Test" + i, test);
				i++;
			}
		} else {
			props.put("fsTests", "");
		}

		
		if (StringUtils.isEmpty(fsTimeout)){
			props.put("fsTimeout", "-1");	
		}
		else{
			props.put("fsTimeout", "" + fsTimeout);
		}
<<<<<<< HEAD
		
		
		if (StringUtils.isEmpty(controllerPollingInterval)){
			props.put("controllerPollingInterval", "30");
		}
		else{
			props.put("controllerPollingInterval", "" + controllerPollingInterval);
		}
		
		if (StringUtils.isEmpty(perScenarioTimeOut)){
			props.put("PerScenarioTimeOut", "10");
		}
		else{
			props.put("PerScenarioTimeOut", ""+ perScenarioTimeOut);
		}
		
		if (!StringUtils.isEmpty(ignoreErrorStrings.replaceAll("\\r|\\n", ""))){
			props.put("ignoreErrorStrings", ""+ignoreErrorStrings.replaceAll("\r", ""));
		}

=======
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033

		return props;
	}
}
