// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.run;

<<<<<<< HEAD
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

=======
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
<<<<<<< HEAD

=======
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

<<<<<<< HEAD
import com.hp.application.automation.tools.AlmToolsUtils;
import com.hp.application.automation.tools.EncryptionUtils;
=======
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
import com.hp.application.automation.tools.model.AlmServerSettingsModel;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.RunFromAlmModel;
import com.hp.application.automation.tools.run.AlmRunTypes.RunType;
import com.hp.application.automation.tools.settings.AlmServerSettingsBuilder;
<<<<<<< HEAD

public class RunFromAlmBuilder extends Builder {
    
    private final RunFromAlmModel runFromAlmModel;
    private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
    private String ResultFilename = "ApiResults.xml";
    private String ParamFileName = "ApiRun.txt";
    //private String KillFileName = "";
    
    @DataBoundConstructor
    public RunFromAlmBuilder(
            String almServerName,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String almTestSets,
            String almRunResultsMode,
            String almTimeout,
            String almRunMode,
            String almRunHost) {
        
        runFromAlmModel =
                new RunFromAlmModel(
                        almServerName,
                        almUserName,
                        almPassword,
                        almDomain,
                        almProject,
                        almTestSets,
                        almRunResultsMode,
                        almTimeout,
                        almRunMode,
                        almRunHost);
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        
        // get the alm server settings
        AlmServerSettingsModel almServerSettingsModel = getAlmServerSettingsModel();
        
        if (almServerSettingsModel == null) {
            listener.fatalError("An ALM server is not defined. Go to Manage Jenkins->Configure System and define your ALM server under Application Lifecycle Management");
            return false;
        }
        
        EnvVars env = null;
        try {
            env = build.getEnvironment(listener);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        VariableResolver<String> varResolver = build.getBuildVariableResolver();
        
        // now merge them into one list
        Properties mergedProperties = new Properties();
        
        mergedProperties.putAll(almServerSettingsModel.getProperties());
        mergedProperties.putAll(runFromAlmModel.getProperties(env, varResolver));
        
        String encAlmPass = "";
        try {
            
            encAlmPass =
                    EncryptionUtils.Encrypt(
                            runFromAlmModel.getAlmPassword(),
                            EncryptionUtils.getSecretKey());
            
            mergedProperties.remove(RunFromAlmModel.ALM_PASSWORD_KEY);
            mergedProperties.put(RunFromAlmModel.ALM_PASSWORD_KEY, encAlmPass);
            
        } catch (Exception e) {
            build.setResult(Result.FAILURE);
            listener.fatalError("problem in qcPassword encription");
        }
        
        Date now = new Date();
        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String time = formatter.format(now);
        
        // get a unique filename for the params file
        ParamFileName = "props" + time + ".txt";
        ResultFilename = "Results" + time + ".xml";
        //KillFileName = "stop" + time + ".txt";
        
        mergedProperties.put("runType", RunType.Alm.toString());
        mergedProperties.put("resultsFilename", ResultFilename);
        
        // get properties serialized into a stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            mergedProperties.store(stream, "");
        } catch (IOException e) {
            build.setResult(Result.FAILURE);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String propsSerialization = stream.toString();
        InputStream propsStream = IOUtils.toInputStream(propsSerialization);
        
        // get the remote workspace filesys
        FilePath projectWS = build.getWorkspace();
        
        // Get the URL to the Script used to run the test, which is bundled
        // in the plugin
        URL cmdExeUrl =
                Hudson.getInstance().pluginManager.uberClassLoader.getResource(HpToolsLauncher_SCRIPT_NAME);
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
        }
        
        try {
            // Run the HpToolsLauncher.exe
            AlmToolsUtils.runOnBuildEnv(build, launcher, listener, CmdLineExe, ParamFileName);
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);
            build.setResult(Result.FAILURE);
            return false;
        } catch (InterruptedException e) {
            build.setResult(Result.ABORTED);
            PrintStream out = listener.getLogger();
            // kill processes
            //FilePath killFile = projectWS.child(KillFileName);
            /* try {
                out.println("Sending abort command");
                killFile.write("\n", "UTF-8");
                while (!killFile.exists())
                    Thread.sleep(1000);
                Thread.sleep(1500);
                
            } catch (IOException e1) {
                //build.setResult(Result.FAILURE);
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                //build.setResult(Result.FAILURE);
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }*/
            
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
            	
            out.println("Operation was aborted by user.");
            //build.setResult(Result.FAILURE);
        }
        return true;
        
    }
    
    public AlmServerSettingsModel getAlmServerSettingsModel() {
        for (AlmServerSettingsModel almServer : getDescriptor().getAlmServers()) {
            if (this.runFromAlmModel != null
                && runFromAlmModel.getAlmServerName().equals(almServer.getAlmServerName())) {
                return almServer;
            }
        }
        return null;
    }
    
    public RunFromAlmModel getRunFromAlmModel() {
        return runFromAlmModel;
    }
    
    @Extension
    // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        public DescriptorImpl() {
            load();
        }
        
        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }
        
        @Override
        public String getDisplayName() {
            return "Execute HP tests from HP ALM";
        }
        
        public boolean hasAlmServers() {
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).hasAlmServers();
        }
        
        public AlmServerSettingsModel[] getAlmServers() {
            return Hudson.getInstance().getDescriptorByType(
                    AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations();
        }
        
        public FormValidation doCheckAlmUserName(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("User name must be set");
            }
            
            return FormValidation.ok();
        }
        
        public FormValidation doCheckAlmTimeout(@QueryParameter String value) {
            
            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok();
            }
            
            String val1 = value.trim();
            
            if (val1.length() > 0 && val1.charAt(0) == '-')
                val1 = val1.substring(1);
            
            if (!StringUtils.isNumeric(val1) && val1 != "") {
                return FormValidation.error("Timeout name must be a number");
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckAlmPassword(@QueryParameter String value) {
            // if (StringUtils.isBlank(value)) {
            // return FormValidation.error("Password must be set");
            // }
            
            return FormValidation.ok();
        }
        
        public FormValidation doCheckAlmDomain(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Domain must be set");
            }
            
            return FormValidation.ok();
        }
        
        public FormValidation doCheckAlmProject(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Project must be set");
            }
            
            return FormValidation.ok();
        }
        
        public FormValidation doCheckAlmTestSets(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Testsets are missing");
            }
            
            return FormValidation.ok();
        }
        
        public List<EnumDescription> getAlmRunModes() {
            return RunFromAlmModel.runModes;
        }
    }
    
    public String getRunResultsFileName() {
        return ResultFilename;
    }
=======
import com.hp.application.automation.tools.AlmToolsUtils;
import com.hp.application.automation.tools.EncryptionUtils;

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

public class RunFromAlmBuilder extends Builder {

	private RunFromAlmModel runFromAlmModel;
	private final static String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
	private String ResultFilename = "ApiResults.xml";
	private String ParamFileName = "ApiRun.txt";
	private String KillFileName = "";

	@DataBoundConstructor
	public RunFromAlmBuilder(String almServerName, String almUserName, String almPassword, String almDomain,
			String almProject, String almTestSets, String almRunResultsMode, String almTimeout, String almRunMode,
			String almRunHost) {

		runFromAlmModel = new RunFromAlmModel(almServerName, almUserName, almPassword, almDomain, almProject,
				almTestSets, almRunResultsMode, almTimeout, almRunMode, almRunHost);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		// get the alm server settings
		AlmServerSettingsModel almServerSettingsModel = getAlmServerSettingsModel();

		if (almServerSettingsModel==null){
			listener.fatalError("An ALM server is not defined. Go to Manage Jenkins->Configure System and define your ALM server under Application Lifecycle Management");
			return false;
		}
		
		EnvVars env = null;
		try {
			env = build.getEnvironment(listener);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		VariableResolver<String> varResolver = build.getBuildVariableResolver();

		// now merge them into one list
		Properties mergedProperties = new Properties();

		mergedProperties.putAll(almServerSettingsModel.getProperties());
		mergedProperties.putAll(runFromAlmModel.getProperties(env, varResolver));

		String encAlmPass = "";
		try {

			encAlmPass = EncryptionUtils.Encrypt(runFromAlmModel.getAlmPassword(), EncryptionUtils.getSecretKey());

			mergedProperties.remove(RunFromAlmModel.ALM_PASSWORD_KEY);
			mergedProperties.put(RunFromAlmModel.ALM_PASSWORD_KEY, encAlmPass);

		} catch (Exception e) {
			build.setResult(Result.FAILURE);
			listener.fatalError("problem in qcPassword encription");
		}

		Date now = new Date();
		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		String time = formatter.format(now);

		// get a unique filename for the params file
		ParamFileName = "props" + time + ".txt";
		ResultFilename = "Results" + time + ".xml";
		KillFileName = "stop" + time + ".txt";

		mergedProperties.put("runType", RunType.Alm.toString());
		mergedProperties.put("resultsFilename", ResultFilename);

		// get properties serialized into a stream
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			mergedProperties.store(stream, "");
		} catch (IOException e) {
			build.setResult(Result.FAILURE);
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				build.setResult(Result.FAILURE);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				build.setResult(Result.FAILURE);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			out.println("Operation Was aborted by user.");
			build.setResult(Result.FAILURE);
		}
		return true;

	}

	public AlmServerSettingsModel getAlmServerSettingsModel() {
		for (AlmServerSettingsModel almServer : getDescriptor().getAlmServers()) {
			if (this.runFromAlmModel != null && runFromAlmModel.getAlmServerName().equals(almServer.getAlmServerName())) {
				return almServer;
			}
		}
		return null;
	}

	public RunFromAlmModel getRunFromAlmModel() {
		return runFromAlmModel;
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
			return "Execute HP tests from HP ALM";
		}

		public boolean hasAlmServers() {
			return Hudson.getInstance().getDescriptorByType(AlmServerSettingsBuilder.DescriptorImpl.class)
					.hasAlmServers();
		}

		public AlmServerSettingsModel[] getAlmServers() {
			return Hudson.getInstance().getDescriptorByType(AlmServerSettingsBuilder.DescriptorImpl.class)
					.getInstallations();
		}

		public FormValidation doCheckAlmUserName(@QueryParameter String value) {
			if (StringUtils.isBlank(value)) {
				return FormValidation.error("User name must be set");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckAlmTimeout(@QueryParameter String value) {

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
		
		public FormValidation doCheckAlmPassword(@QueryParameter String value) {
			// if (StringUtils.isBlank(value)) {
			// return FormValidation.error("Password must be set");
			// }

			return FormValidation.ok();
		}

		public FormValidation doCheckAlmDomain(@QueryParameter String value) {
			if (StringUtils.isBlank(value)) {
				return FormValidation.error("Domain must be set");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckAlmProject(@QueryParameter String value) {
			if (StringUtils.isBlank(value)) {
				return FormValidation.error("Project must be set");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckAlmTestSets(@QueryParameter String value) {
			if (StringUtils.isBlank(value)) {
				return FormValidation.error("Testsets are missing");
			}

			return FormValidation.ok();
		}

		public List<EnumDescription> getAlmRunModes() {
			return RunFromAlmModel.runModes;
		}
	}

	public String getRunResultsFileName() {
		return ResultFilename;
	}
>>>>>>> a70002b5448518e77174a13b68e98364fdd02033
}
