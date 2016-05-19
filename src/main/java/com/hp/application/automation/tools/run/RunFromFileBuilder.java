// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.run;

import com.hp.application.automation.tools.EncryptionUtils;
import com.hp.application.automation.tools.model.MCServerSettingsModel;
import com.hp.application.automation.tools.mc.JobConfigurationProxy;
import com.hp.application.automation.tools.model.ProxySettings;
import com.hp.application.automation.tools.settings.MCServerSettingsBuilder;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.IOUtils;
import hudson.util.VariableResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import net.minidev.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hp.application.automation.tools.AlmToolsUtils;
import com.hp.application.automation.tools.model.RunFromFileSystemModel;
import com.hp.application.automation.tools.run.AlmRunTypes.RunType;
import org.kohsuke.stapler.bind.JavaScriptMethod;

public class RunFromFileBuilder extends Builder {

	private final RunFromFileSystemModel runFromFileModel;
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private final static String LRAnalysisLauncher_EXE = "LRAnalysisLauncher.exe";
	private String ResultFilename = "ApiResults.xml";
	//private String KillFileName = "";
	private String ParamFileName = "ApiRun.txt";

	@DataBoundConstructor
	public RunFromFileBuilder(String fsTests, String fsTimeout, String controllerPollingInterval,
			String perScenarioTimeOut, String ignoreErrorStrings, String mcServerName, String fsUserName, String fsPassword, String fsDeviceId, String fsTargetLab, String fsManufacturerAndModel, String fsOs, String fsAutActions, String fsLaunchAppName, String fsDevicesMetrics, String fsInstrumented, String fsExtraApps, String fsJobId, ProxySettings proxySettings, boolean useSSL) {

		runFromFileModel = new RunFromFileSystemModel(fsTests, fsTimeout, controllerPollingInterval,
				perScenarioTimeOut, ignoreErrorStrings, mcServerName, fsUserName, fsPassword, fsDeviceId, fsTargetLab, fsManufacturerAndModel, fsOs, fsAutActions, fsLaunchAppName, fsDevicesMetrics, fsInstrumented, fsExtraApps, fsJobId, proxySettings, useSSL);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

        // get the mc server settings
        MCServerSettingsModel mcServerSettingsModel = getMCServerSettingsModel();

		EnvVars env = null;
		try {
			env = build.getEnvironment(listener);

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		VariableResolver<String> varResolver = build.getBuildVariableResolver();
        JSONObject jobDetails = null;
        String mcServerUrl = "";
		// now merge them into one list
        Properties mergedProperties = new Properties();

        if(mcServerSettingsModel != null){
            mcServerUrl = mcServerSettingsModel.getProperties().getProperty("MobileHostAddress");
            if(runFromFileModel.getProxySettings() == null){
                jobDetails = runFromFileModel.getJobDetails(mcServerUrl, null, null, null);
            }else{
                jobDetails = runFromFileModel.getJobDetails(mcServerUrl, runFromFileModel.getProxySettings().getFsProxyAddress(),runFromFileModel.getProxySettings().getFsProxyUserName(), runFromFileModel.getProxySettings().getFsProxyPassword());
            }
            mergedProperties.setProperty("mobileinfo", jobDetails.toJSONString());
        }
        if(runFromFileModel != null && StringUtils.isNotBlank(runFromFileModel.getFsPassword())){
            String encPassword = "";
            try {
                encPassword = EncryptionUtils.Encrypt(runFromFileModel.getFsPassword(),EncryptionUtils.getSecretKey());
                mergedProperties.put("MobilePassword", encPassword);
            } catch (Exception e) {
                build.setResult(Result.FAILURE);
                listener.fatalError("problem in mobile center password encription");
            }
        }

        mergedProperties.putAll(mcServerSettingsModel.getProperties());
		mergedProperties.putAll(runFromFileModel.getProperties(env, varResolver));
		int idx = 0;
		for (String key : env.keySet()) {
			idx++;
			mergedProperties.put("JenkinsEnv" + idx, key+";"+env.get(key));
		}

		Date now = new Date();
		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		String time = formatter.format(now);

		// get a unique filename for the params file
		ParamFileName = "props" + time + ".txt";
		ResultFilename = "Results" + time + ".xml";
		//KillFileName = "stop" + time + ".txt";

		mergedProperties.put("runType", RunType.FileSystem.toString());
		mergedProperties.put("resultsFilename", ResultFilename);

		// get properties serialized into a stream
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			mergedProperties.store(stream, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			build.setResult(Result.FAILURE);
		}
		String propsSerialization = stream.toString();
		InputStream propsStream = IOUtils.toInputStream(propsSerialization);

		// get the remote workspace filesys
		FilePath projectWS = build.getWorkspace();

		// Get the URL to the Script used to run the test, which is bundled
		// in the plugin
		URL cmdExeUrl = Hudson.getInstance().pluginManager.uberClassLoader.getResource(HpToolsLauncher_SCRIPT_NAME);
		if (cmdExeUrl == null) {
			listener.fatalError(HpToolsLauncher_SCRIPT_NAME + " not found in resources");
			return false;
		}
		
		URL cmdExe2Url = Hudson.getInstance().pluginManager.uberClassLoader.getResource(LRAnalysisLauncher_EXE);
		if (cmdExe2Url == null){
			listener.fatalError(LRAnalysisLauncher_EXE+ "not found in resources");
			return false;
		}

		FilePath propsFileName = projectWS.child(ParamFileName);
		FilePath CmdLineExe = projectWS.child(HpToolsLauncher_SCRIPT_NAME);
		FilePath CmdLineExe2 = projectWS.child(LRAnalysisLauncher_EXE);


		try {
			// create a file for the properties file, and save the properties
			propsFileName.copyFrom(propsStream);

			
			// Copy the script to the project workspace
			CmdLineExe.copyFrom(cmdExeUrl);
			
			CmdLineExe2.copyFrom(cmdExe2Url);


		} catch (IOException e1) {
			build.setResult(Result.FAILURE);
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			build.setResult(Result.FAILURE);
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// Run the HpToolsLauncher.exe
            AlmToolsUtils.runOnBuildEnv(build, launcher, listener, CmdLineExe, ParamFileName);
			// Has the report been successfuly generated?
		} catch (IOException ioe) {
			Util.displayIOException(ioe, listener);
			build.setResult(Result.FAILURE);
			return false;
		} catch (InterruptedException e) {
			build.setResult(Result.ABORTED);
			PrintStream out = listener.getLogger();
			
			try {
				AlmToolsUtils.runHpToolsAborterOnBuildEnv(build, launcher, listener, ParamFileName);
			} catch (IOException e1) {
				Util.displayIOException(e1, listener);
				build.setResult(Result.FAILURE);
				return false;
		} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// kill processes
			/*FilePath killFile = projectWS.child(KillFileName);
			try {
				killFile.write("\n", "UTF-8");
                while (!killFile.exists())
                    Thread.sleep(1000);
                Thread.sleep(1500);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/

			out.println("Operation Was aborted by user.");
		}

		return true;

	}

    public MCServerSettingsModel getMCServerSettingsModel() {
        for (MCServerSettingsModel mcServer : getDescriptor().getMcServers()) {
            if (this.runFromFileModel != null
                    && runFromFileModel.getMcServerName().equals(mcServer.getMcServerName())) {
                return mcServer;
            }
        }
        return null;
    }

	public RunFromFileSystemModel getRunFromFileModel() {
		return runFromFileModel;
	}

	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        JobConfigurationProxy instance = JobConfigurationProxy.getInstance();
		public DescriptorImpl() {
			load();
		}

		@Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return true;
		}

        @JavaScriptMethod
        public String getJobId(String mcUrl, String mcUserName, String mcPassword, String proxyAddress, String proxyUserName, String proxyPassword) {
            return instance.createTempJob(mcUrl, mcUserName, mcPassword, proxyAddress, proxyUserName, proxyPassword);
        }

        @JavaScriptMethod
        public JSONObject populateAppAndDevice(String mcUrl, String mcUserName, String mcPassword,  String proxyAddress, String proxyUserName, String proxyPassword, String jobId) {
            return instance.getJobJSONData(mcUrl, mcUserName, mcPassword, proxyAddress, proxyUserName, proxyPassword, jobId);
        }

        @JavaScriptMethod
        public String getMcServerUrl(String serverName) {
            String serverUrl = "";
            MCServerSettingsModel[] servers = Hudson.getInstance().getDescriptorByType(
                    MCServerSettingsBuilder.MCDescriptorImpl.class).getInstallations();
            for (MCServerSettingsModel mcServer : servers) {
                if (mcServer.getMcServerName().equals(serverName)) {
                    serverUrl = mcServer.getMcServerUrl();
                }
            }
            return serverUrl;
        }

        @Override
		public String getDisplayName() {
			return "Execute HP tests from file system";
		}

		public FormValidation doCheckFsTests(@QueryParameter String value) 
		{
			return FormValidation.ok();
		}
		
		public FormValidation doCheckIgnoreErrorStrings(@QueryParameter String value)
		{
			return FormValidation.ok();
		}
		
						
		public FormValidation doCheckFsTimeout(@QueryParameter String value) 
		{
			if (StringUtils.isEmpty(value)){
				return FormValidation.ok();
			}
			
			String val1 = value.trim();  
			if (val1.length()>0 && val1.charAt(0) == '-')
				val1=val1.substring(1);
						
			if (!StringUtils.isNumeric(val1) && val1 !="") 
			{
				return FormValidation.error("Timeout name must be a number");
			}
			return FormValidation.ok();
		}

        public boolean hasMCServers() {
            return Hudson.getInstance().getDescriptorByType(
                    MCServerSettingsBuilder.MCDescriptorImpl.class).hasMCServers();
        }

        public MCServerSettingsModel[] getMcServers() {
            return Hudson.getInstance().getDescriptorByType(
                    MCServerSettingsBuilder.MCDescriptorImpl.class).getInstallations();
        }

		public FormValidation doCheckControllerPollingInterval(@QueryParameter String value){
			if (StringUtils.isEmpty(value)){
				return FormValidation.ok();
			}
			
			if (!StringUtils.isNumeric(value)){
				return FormValidation.error("Controller Polling Interval must be a number");
			}
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckPerScenarioTimeOut(@QueryParameter String value){
			if (StringUtils.isEmpty(value)){
				return FormValidation.ok();
			}
			
			if (!StringUtils.isNumeric(value)){
				return FormValidation.error("Per Scenario Timeout must be a number");
			}
			
			return FormValidation.ok();
		}
		
	}

	public String getRunResultsFileName() {
		return ResultFilename;
	}
}
