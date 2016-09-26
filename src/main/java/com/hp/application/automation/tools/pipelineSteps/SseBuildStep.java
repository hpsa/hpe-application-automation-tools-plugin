package com.hp.application.automation.tools.pipelineSteps;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hp.application.automation.tools.model.AlmServerSettingsModel;
import com.hp.application.automation.tools.model.CdaDetails;
import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ProxySettings;
import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.run.SseBuilder;
import com.hp.application.automation.tools.settings.AlmServerSettingsBuilder;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.util.FormValidation;

public class SseBuildStep extends AbstractStepImpl {
	
	private final SseBuilder sseBuilder;
	
	@DataBoundConstructor
	public SseBuildStep (String almServerName,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String description,
            String runType,
            String almEntityId,
            String timeslotDuration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails,
            ProxySettings proxySettings) {

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
                proxySettings);
	}
	
	public SseBuilder getSseBuilder() {
		return sseBuilder;
	}
	
	public String getAlmServerName() {
        return sseBuilder.getSseModel().getAlmServerName();
    }
    
    public String getAlmServerUrl() {
        return sseBuilder.getSseModel().getAlmServerUrl();
    }
    
    public String getAlmUserName() {
        return sseBuilder.getSseModel().getAlmUserName();
    }
    
    public String getAlmPassword() {
        return sseBuilder.getSseModel().getAlmPassword();
    }
    
    public String getAlmDomain() {
        return sseBuilder.getSseModel().getAlmDomain();
    }
    
    public String getAlmProject() {
        return sseBuilder.getSseModel().getAlmProject();
    }
    
    public String getTimeslotDuration() {
        return sseBuilder.getSseModel().getTimeslotDuration();
    }
    
    public String getAlmEntityId() {
        return sseBuilder.getSseModel().getAlmEntityId();
    }
    
    public String getRunType() {
        return sseBuilder.getSseModel().getRunType();
    }
    
    public String getDescription() {
        return sseBuilder.getSseModel().getDescription();
    }
    
    public String getEnvironmentConfigurationId() {
        return sseBuilder.getSseModel().getEnvironmentConfigurationId();
    }
        
    public CdaDetails getCdaDetails() {
        
        return sseBuilder.getSseModel().getCdaDetails();
    }
    
    public boolean isCdaDetailsChecked() {
        return sseBuilder.getSseModel().isCdaDetailsChecked();
    }
    
    public String getPostRunAction() {
        return sseBuilder.getSseModel().getPostRunAction();
    }
    
    public ProxySettings getProxySettings() {
        return sseBuilder.getSseModel().getProxySettings();
    }

    public boolean isUseProxy() {
        return sseBuilder.getSseModel().isUseProxy();
    }

    public boolean isUseAuthentication() {
        return sseBuilder.getSseModel().isUseAuthentication();
    }
	
	@Extension @Symbol("SseBuildStep")
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {
	    public DescriptorImpl() {
	    	super(SseBuildStepExecution.class);
	    }
	    
	    @Override
	    public String getFunctionName() {
	        return "SseBuildStep";
	    }
	    
		@Nonnull
		@Override
	    public String getDisplayName() {
	        return "Sse Build Step";
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

}
