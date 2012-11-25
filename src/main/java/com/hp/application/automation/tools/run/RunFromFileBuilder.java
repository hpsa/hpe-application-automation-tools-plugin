// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.run;

import org.apache.commons.lang.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import com.hp.application.automation.tools.model.RunFromFileSystemModel;
import com.hp.application.automation.tools.run.AlmRunTypes.RunType;
import com.hp.application.automation.tools.AlmToolsUtils;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.IOUtils;
import hudson.util.VariableResolver;

public class RunFromFileBuilder extends Builder {

	private RunFromFileSystemModel runFromFileModel;
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private String ResultFilename = "ApiResults.xml";
	private String KillFileName = "";
	private String ParamFileName = "ApiRun.txt";

	@DataBoundConstructor
	public RunFromFileBuilder(String fsTests, String fsTimeout) {

		runFromFileModel = new RunFromFileSystemModel(fsTests, fsTimeout);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

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

		// now merge them into one list
		Properties mergedProperties = new Properties();

		mergedProperties.putAll(runFromFileModel.getProperties(env, varResolver));

		Date now = new Date();
		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		String time = formatter.format(now);

		// get a unique filename for the params file
		ParamFileName = "props" + time + ".txt";
		ResultFilename = "Results" + time + ".xml";
		KillFileName = "stop" + time + ".txt";

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

		FilePath propsFileName = projectWS.child(ParamFileName);
		FilePath CmdLineExe = projectWS.child(HpToolsLauncher_SCRIPT_NAME);

		try {
			// create a file for the properties file, and save the properties
			propsFileName.copyFrom(propsStream);

			// Copy the script to the project workspace
			CmdLineExe.copyFrom(cmdExeUrl);
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
			String logFile = AlmToolsUtils.runOnBuildEnv(build, launcher, listener, CmdLineExe, ParamFileName);
			// Has the report been successfuly generated?
			if (!projectWS.child(logFile).exists()) {
				listener.fatalError("Report could not be generated");
				return false;
			}
		} catch (IOException ioe) {
			Util.displayIOException(ioe, listener);
			build.setResult(Result.FAILURE);
			return false;
		} catch (InterruptedException e) {
			build.setResult(Result.FAILURE);
			PrintStream out = listener.getLogger();
			// kill processes
			FilePath killFile = projectWS.child(KillFileName);
			try {
				killFile.write("\n", "UTF-8");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			out.println("Operation Was aborted by user.");
		}

		return true;

	}

	public RunFromFileSystemModel getRunFromFileModel() {
		return runFromFileModel;
	}

	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Execute HP tests from file system";
		}

		public FormValidation doCheckFsTests(@QueryParameter String value) 
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
		
	}

	public String getRunResultsFileName() {
		return ResultFilename;
	}
}
