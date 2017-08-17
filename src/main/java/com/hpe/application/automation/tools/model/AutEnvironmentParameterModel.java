package com.hpe.application.automation.tools.model;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

/**
 * Created by barush on 21/10/2014.
 */
public class AutEnvironmentParameterModel extends
        AbstractDescribableImpl<AutEnvironmentParameterModel> {
    
    private final String name;
    private final String value;
    private final String paramType;
    private final boolean shouldGetOnlyFirstValueFromJson;
    private String resolvedValue;
    
    public static final List<String> parametersTypes = Arrays.asList(
            AutEnvironmentParameterType.USER_DEFINED.value(),
            AutEnvironmentParameterType.ENVIRONMENT.value(),
            AutEnvironmentParameterType.EXTERNAL.value());
    
    @DataBoundConstructor
    public AutEnvironmentParameterModel(
            String name,
            String value,
            String paramType,
            boolean shouldGetOnlyFirstValueFromJson) {
        
        this.name = name;
        this.value = value;
        this.paramType = paramType;
        this.shouldGetOnlyFirstValueFromJson = shouldGetOnlyFirstValueFromJson;
    }
    
    public boolean isShouldGetOnlyFirstValueFromJson() {
        return shouldGetOnlyFirstValueFromJson;
    }
    
    public String getParamType() {
        return paramType;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getName() {
        return name;
    }
    
    public String getResolvedValue() {
        return resolvedValue;
    }
    
    public void setResolvedValue(String resolvedValue) {
        this.resolvedValue = resolvedValue;
    }
    
    public enum AutEnvironmentParameterType {
        
        UNDEFINED(""), ENVIRONMENT("Environment"), EXTERNAL("From JSON"), USER_DEFINED("Manual");
        
        private String value;
        
        private AutEnvironmentParameterType(String value) {
            
            this.value = value;
        }
        
        public String value() {
            
            return value;
        }
        
        public static AutEnvironmentParameterType get(String val) {
            for (AutEnvironmentParameterType parameterType : AutEnvironmentParameterType.values()) {
                if (val.equals(parameterType.value()))
                    return parameterType;
            }
            return UNDEFINED;
        }
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<AutEnvironmentParameterModel> {
        
        public String getDisplayName() {
            return "AUT Parameter";
        }
        
        public List<String> getParametersTypes() {
            return AutEnvironmentParameterModel.parametersTypes;
        }
        
        public FormValidation doCheckName(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.error("Parameter name must be set");
            }
            
            return ret;
        }
        
        public FormValidation doCheckValue(@QueryParameter String value) {
            
            FormValidation ret = FormValidation.ok();
            if (StringUtils.isBlank(value)) {
                ret = FormValidation.warning("You didn't assign any value to this parameter");
            }
            
            return ret;
        }
        
        public String getRandomName(@QueryParameter String prefix) {
            return prefix + UUID.randomUUID();
        }
        
    }
    
}
