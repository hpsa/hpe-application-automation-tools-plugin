// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.results;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hp.application.automation.tools.model.AlmServerSettingsModel;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.UploadTestResultToAlmModel;
import com.hp.application.automation.tools.results.service.AlmRestInfo;
import com.hp.application.automation.tools.results.service.AlmRestTool;
import com.hp.application.automation.tools.results.service.DefaultExternalEntityUploadServiceImpl;
import com.hp.application.automation.tools.results.service.ExternalEntityUploadLogger;
import com.hp.application.automation.tools.results.service.IExternalEntityUploadService;
import com.hp.application.automation.tools.settings.AlmServerSettingsBuilder;

/**

 * 
 * @author Jacky Zhu
 */
public class TestResultToALMUploader extends Recorder implements Serializable, MatrixAggregatable {
    
    private static final long serialVersionUID = 1L;
    private final UploadTestResultToAlmModel uploadTestResultToAlmModel;
    
    @DataBoundConstructor
    public TestResultToALMUploader(
            String almServerName,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String testingFramework,
            String testingTool,
            String almTestFolder,
            String almTestSetFolder,
            String almTimeout,
            String testingResultFile,
            String jenkinsServerUrl) {
        
    	uploadTestResultToAlmModel = new UploadTestResultToAlmModel( almServerName,  almUserName,
    			 almPassword,  almDomain,  almProject, testingFramework, testingTool,
    			 almTestFolder,  almTestSetFolder,  almTimeout, testingResultFile, jenkinsServerUrl);
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        
        return (DescriptorImpl) super.getDescriptor();
    }
    
    public AlmServerSettingsModel getAlmServerSettingsModel() {
        for (AlmServerSettingsModel almServer : getDescriptor().getAlmServers()) {
            if (this.uploadTestResultToAlmModel != null
                && uploadTestResultToAlmModel.getAlmServerName().equals(almServer.getAlmServerName())) {
                return almServer;
            }
        }
        return null;
    }
    
    public UploadTestResultToAlmModel getUploadTestResultToAlmModel() {
        return uploadTestResultToAlmModel;
    }
    
    private String getAlmServerUrl(String almServerName) {
    	AlmServerSettingsModel[] almServers = Hudson.getInstance().getDescriptorByType(
                AlmServerSettingsBuilder.DescriptorImpl.class).getInstallations();
    	if(almServers != null && almServers.length >0) {
    		for(AlmServerSettingsModel almServerModel: almServers) {
    			if(almServerName.equalsIgnoreCase(almServerModel.getAlmServerName())) {
    				return almServerModel.getAlmServerUrl();
    			}
    		}
    	}
    	
    	return "";
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        
    	ExternalEntityUploadLogger logger = new ExternalEntityUploadLogger(listener.getLogger());

    	logger.log("INFO: 'Upload test result to ALM' Post Build Step is being invoked.");
        if(uploadTestResultToAlmModel == null){
        	logger.log("ERROR: No configuration for 'Upload test reslt to ALM' step");
        }
        
        
        String serverUrl = getAlmServerUrl(uploadTestResultToAlmModel.getAlmServerName());
        String runUrl = "";
        String tempUrl = uploadTestResultToAlmModel.getJenkinsServerUrl();
        if(tempUrl != null && tempUrl.length() >0 ) {
        	if(tempUrl.charAt(tempUrl.length() -1) != '/') {
        		runUrl= tempUrl + "/" + build.getUrl();
        	} else  {
        		runUrl = tempUrl + build.getUrl();
        	}
        }
        
        File root = build.getRootDir();
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(root);
        ds.setIncludes( new String[] {uploadTestResultToAlmModel.getTestingResultFile()});
        
        ds.scan();
        if (ds.getIncludedFilesCount() == 0) {
        	logger.log("INFO: No Test Report found.");
        } else {
        	logger.log("INFO: "+ ds.getIncludedFilesCount() +" test result file found.");
        	String[] files = ds.getIncludedFiles();
        	for(String fileName : files) {
        		String fullpath = root.getAbsolutePath() + File.separator + fileName;
    			AlmRestInfo loginInfo = new AlmRestInfo(
    					serverUrl,
    					uploadTestResultToAlmModel.getAlmDomain(),
    					uploadTestResultToAlmModel.getAlmProject(),
    					uploadTestResultToAlmModel.getAlmUserName(),
    					uploadTestResultToAlmModel.getAlmPassword(),
    					uploadTestResultToAlmModel.getAlmTestSetFolder()
    					);
    			AlmRestTool u = new AlmRestTool(loginInfo, logger);
    			logger.log("INFO: Start to upload "+fullpath);
    			IExternalEntityUploadService service = new DefaultExternalEntityUploadServiceImpl(u, logger );
    			service.UploadExternalTestSet(loginInfo, 
    											fullpath, 
    											uploadTestResultToAlmModel.getAlmTestSetFolder(), 
    											uploadTestResultToAlmModel.getAlmTestFolder(), 
    											uploadTestResultToAlmModel.getTestingFramework(), 
    											uploadTestResultToAlmModel.getTestingTool(), 
    											String.valueOf(build.getNumber()),
    											build.getParent().getDisplayName(),
    											runUrl
    											);
    			logger.log("INFO: Uploaded "+fullpath + ".");

        	}
        }

        logger.log("INFO: 'Upload test result to ALM' Completed.");
        
        return true;
    }
    


    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        
        return new TestResultProjectAction(project);
    }
    
    @Override
    public MatrixAggregator createAggregator(
            MatrixBuild build,
            Launcher launcher,
            BuildListener listener) {
        
        return new TestResultAggregator(build, launcher, listener);
    }
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        
        return BuildStepMonitor.BUILD;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        
        public DescriptorImpl() {
            
            load();
        }
        
        @Override
        public String getDisplayName() {
            
            return "Upload test result to ALM";
        }
        
        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            
            return true;
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
        
        public FormValidation doCheckAlmTestFolder(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("TestFolder are missing");
            }
            
            return FormValidation.ok();
        }  
        
        public FormValidation doCheckAlmTestSetFolder(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("TestSetFolder are missing");
            }
            
            return FormValidation.ok();
        }        
        
        public FormValidation doCheckTestingResultFile(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Testing result file must be set");
            }
            
            return FormValidation.ok();
        }  
        
        public List<EnumDescription> getTestingFrameworks() {
            return UploadTestResultToAlmModel.testingFrameworks;
        }
        
    }
}
