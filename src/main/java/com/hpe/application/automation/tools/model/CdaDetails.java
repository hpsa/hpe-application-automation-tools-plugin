package com.hpe.application.automation.tools.model;

import java.util.Arrays;
import java.util.List;

import com.hpe.application.automation.tools.sse.common.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class CdaDetails {
    
    private final String _deploymentAction;
    private final String _deployedEnvironmentName;
    private final String _deprovisioningAction;
    
    private final static EnumDescription _deploymentActionUseDeployed = new EnumDescription(
            "",
            "Use Deployed");
    private final static EnumDescription _deploymentActionProvisionDeploy = new EnumDescription(
            "Provision;Deploy",
            "Provision and Deploy");
    private final static EnumDescription _deploymentActionRedeploy = new EnumDescription(
            "Deploy",
            "Redeploy");
    private final static List<EnumDescription> _deploymentActions = Arrays.asList(
            _deploymentActionUseDeployed,
            _deploymentActionProvisionDeploy,
            _deploymentActionRedeploy);
    
    private final static EnumDescription _deprovisioningActionLeaveDeployed = new EnumDescription(
            "",
            "Leave environment deployed");
    private final static EnumDescription _deprovisioningActionDeprovision = new EnumDescription(
            "Deprovision",
            "Deprovision at end");
    private final static List<EnumDescription> _deprovisioningActions = Arrays.asList(
            _deprovisioningActionLeaveDeployed,
            _deprovisioningActionDeprovision);
    
    @DataBoundConstructor
    public CdaDetails(
            String deploymentAction,
            String deployedEnvironmentName,
            String deprovisioningAction) {
        
        _deploymentAction = deploymentAction;
        _deployedEnvironmentName = deployedEnvironmentName;
        _deprovisioningAction = deprovisioningAction;
    }
    
    public static List<EnumDescription> getDeploymentActions() {
        
        return _deploymentActions;
    }
    
    public static List<EnumDescription> getDeprovisioningActions() {
        
        return _deprovisioningActions;
    }
    
    public String getTopologyAction() {
        
        String ret = _deploymentAction;
        if (!StringUtils.isNullOrEmpty(_deprovisioningAction)) {
            ret = String.format("%s;%s", ret, _deprovisioningAction);
        }
        
        return ret;
    }
    
    public String getDeploymentAction() {
        
        return _deploymentAction;
    }
    
    public String getDeployedEnvironmentName() {
        
        return _deployedEnvironmentName;
    }
    
    public String getDeprovisioningAction() {
        
        return _deprovisioningAction;
    }
}
