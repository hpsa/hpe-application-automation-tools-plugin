package com.hp.application.automation.tools.pipelineSteps;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.hp.application.automation.tools.model.AlmServerSettingsModel;
import com.hp.application.automation.tools.model.CdaDetails;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ProxySettings;
import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.run.SseBuilder;
import com.hp.application.automation.tools.settings.AlmServerSettingsBuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

/**
 * This class is for expose the ssebuilder as a pipeline step while not change too much on the ssebuilder itself.
 * @author llu4
 *
 */
public class SseBuildStep extends Builder implements SimpleBuildStep {
	
	private SseBuilder sseBuilder;
	
    private String almServerName;
    private String almUserName;
    private String almPassword;
    private String almDomain;
    private String almProject;
    private String description;
    private String runType;
    private String almEntityId;
    private String timeslotDuration;
    private String postRunAction;
    private String environmentConfigurationId;
    private CdaDetails cdaDetails;
    private PlainProxySettings proxySettings;
	
	@DataBoundConstructor
	public SseBuildStep (String almServerName) {
		this.almServerName = almServerName;
	}
	
	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		
		//Recieve the plain password then convert a encrypted password.
		ProxySettings ps = null;
		if (proxySettings != null) {
			ps = new ProxySettings(
					proxySettings.isFsUseAuthentication(),
					proxySettings.getFsProxyAddress(),
					proxySettings.getFsProxyUserName(),
					Secret.fromString(proxySettings.getFsProxyPassword())
				);
		}
		
		SseBuilder sseBuilder = new SseBuilder(
	            almServerName,
	            almUserName,
	            almPassword,
	            almDomain,
	            almProject,
	            description,
	            runType,
	            almEntityId,
	            timeslotDuration,
	            postRunAction,
	            environmentConfigurationId,
	            cdaDetails,
	            ps);
		sseBuilder.perform(run, workspace, launcher, listener);
	}
	
	public SseBuilder getSseBuilder() {
		return sseBuilder;
	}
	
	@Extension @Symbol("SseBuildStep")
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
	    @Override
	    public String getDisplayName() {
	    	return "Execute HP tests using HP ALM Lab Management";
	    }
	    
	    @Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
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
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("User name must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckTimeslotDuration(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Timeslot duration must be set");
            } else if (Integer.valueOf(value) < 30) {
                ret = FormValidation.error("Timeslot duration must be higher than 30");
            }
            
            return ret;
        }
        
        public FormValidation doCheckAlmDomain(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Domain must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckAlmProject(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Project must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckAlmEntityId(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Entity ID must be set.");
            }
            
            return ret;
        }
        
        public List<EnumDescription> getRunTypes() {
            
            return SseModel.getRunTypes();
        }
        
        public List<EnumDescription> getPostRunActions() {
            
            return SseModel.getPostRunActions();
        }
        
        public List<EnumDescription> getDeploymentActions() {
            
            return CdaDetails.getDeploymentActions();
        }
        
        public static List<EnumDescription> getDeprovisioningActions() {
            
            return CdaDetails.getDeprovisioningActions();
        }
	}
	
	/**
	 * Databound setters and getters.
	 * @return
	 */
    public String getAlmServerName() { return almServerName; }
    @DataBoundSetter
    public void setAlmServerName(String almServerName) { this.almServerName = almServerName; }

    public String getAlmUserName() { return almUserName; }
    @DataBoundSetter
    public void setAlmUserName(String almUserName) { this.almUserName = almUserName; }

    public String getAlmPassword() { return almPassword; }
    @DataBoundSetter
    public void setAlmPassword(String almPassword) { this.almPassword = almPassword; }

    public String getAlmDomain() { return almDomain; }
    @DataBoundSetter
    public void setAlmDomain(String almDomain) { this.almDomain = almDomain; }

    public String getAlmProject() { return almProject; }
    @DataBoundSetter
    public void setAlmProject(String almProject) { this.almProject = almProject; }

    public String getDescription() { return description; }
    @DataBoundSetter
    public void setDescription(String description) { this.description = description; }

    public String getRunType() { return runType; }
    @DataBoundSetter
    public void setRunType(String runType) { this.runType = runType; }

    public String getAlmEntityId() { return almEntityId; }
    @DataBoundSetter
    public void setAlmEntityId(String almEntityId) { this.almEntityId = almEntityId; }

    public String getTimeslotDuration() { return timeslotDuration; }
    @DataBoundSetter
    public void setTimeslotDuration(String timeslotDuration) { this.timeslotDuration = timeslotDuration; }

    public String getPostRunAction() { return postRunAction; }
    @DataBoundSetter
    public void setPostRunAction(String postRunAction) { this.postRunAction = postRunAction; }

    public String getEnvironmentConfigurationId() { return environmentConfigurationId; }
    @DataBoundSetter
    public void setEnvironmentConfigurationId(String environmentConfigurationId) {
        this.environmentConfigurationId = environmentConfigurationId;
    }

    public CdaDetails getCdaDetails() { return cdaDetails; }
    @DataBoundSetter
    public void setCdaDetails(CdaDetails cdaDetails) { this.cdaDetails = cdaDetails; }

    public PlainProxySettings getProxySettings() { return proxySettings; }
    @DataBoundSetter
    public void setProxySettings(PlainProxySettings proxySettings) { this.proxySettings = proxySettings; }
    
    public boolean isUseProxy() {
        return proxySettings != null;
    }
    
    public boolean isUseAuthentication() {
        return proxySettings != null && StringUtils.isNotBlank(proxySettings.getFsProxyUserName());
    }
    
    public boolean isCdaDetailsChecked() {
        return cdaDetails != null;
    }
}
