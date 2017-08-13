package com.hpe.application.automation.tools.sse;

import com.hpe.application.automation.tools.model.SseModel;
import com.hpe.application.automation.tools.sse.sdk.Args;

import hudson.Util;
import hudson.util.VariableResolver;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class ArgsFactory {
    
    public Args create(SseModel model) {
        
        return new Args(
                
                model.getAlmServerUrl(),
                model.getAlmDomain(),
                model.getAlmProject(),
                model.getAlmUserName(),
                model.getAlmPassword(),
                model.getRunType(),
                model.getAlmEntityId(),
                model.getTimeslotDuration(),
                model.getDescription(),
                model.getPostRunAction(),
                model.getEnvironmentConfigurationId(),
                model.getCdaDetails());
    }
    
    public Args createResolved(SseModel model, VariableResolver<String> buildResolver) {
        
        return new Args(
                
                model.getAlmServerUrl(),
                Util.replaceMacro(model.getAlmDomain(), buildResolver),
                Util.replaceMacro(model.getAlmProject(), buildResolver),
                Util.replaceMacro(model.getAlmUserName(), buildResolver),
                model.getAlmPassword(),
                model.getRunType(),
                Util.replaceMacro(model.getAlmEntityId(), buildResolver),
                Util.replaceMacro(model.getTimeslotDuration(), buildResolver),
                model.getDescription(),
                model.getPostRunAction(),
                Util.replaceMacro(model.getEnvironmentConfigurationId(), buildResolver),
                model.getCdaDetails());
    }
}
