package com.hp.application.automation.tools.pipelineSteps;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
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
public class SseBuildStep extends AbstractStepImpl {
	
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
	public SseBuildStep (String almServerName, String almUserName, String almPassword, String almProject, String almDomain, String runType, String almEntityId,
                         String timeslotDuration)
    {
		this.almServerName = almServerName;
        this.almUserName = almUserName;
        this.almPassword = almPassword;
        this.almProject = almProject;
        this.almDomain = almDomain;
        this.timeslotDuration = timeslotDuration;
        this.runType = runType;
        this.almEntityId = almEntityId;
    }
	
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
		
		sseBuilder = new SseBuilder(
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

	@Extension @Symbol("SseBuild")
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SseBuildExecutor.class);
        }

        @Override
	    public String getDisplayName() {
	    	return "Execute HP tests using HP ALM Lab Management";
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

        @Override
        public String getFunctionName() {
            return "sseBuild";
        }
    }
	
	/**
	 * Databound setters and getters.
	 * @return
	 */
    public String getAlmServerName() { return almServerName; }

    public String getAlmUserName() { return almUserName; }

    public String getAlmPassword() { return almPassword; }

    public String getAlmDomain() { return almDomain; }

    public String getAlmProject() { return almProject; }

    public String getDescription() { return description; }
    @DataBoundSetter
    public void setDescription(String description) { this.description = description; }

    public String getRunType() { return runType; }

    public String getAlmEntityId() { return almEntityId; }

    public String getTimeslotDuration() { return timeslotDuration; }

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

    private static final long serialVersionUID = 1L;

}
