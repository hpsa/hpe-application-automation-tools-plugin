package com.hp.application.automation.tools.sse;

import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.sse.common.EnvironmnetVariablesUtils;
import com.hp.application.automation.tools.sse.sdk.Args;
import com.hp.application.automation.tools.sse.sdk.Logger;
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
    
    public Args createResolved(
            SseModel model,
            VariableResolver<String> buildVariableResolver,
            Logger logger) {
        
        return new Args(
                
                model.getAlmServerUrl(),
                EnvironmnetVariablesUtils.reolveVariable(
                        model.getAlmDomain(),
                        buildVariableResolver,
                        logger),
                EnvironmnetVariablesUtils.reolveVariable(
                        model.getAlmProject(),
                        buildVariableResolver,
                        logger),
                EnvironmnetVariablesUtils.reolveVariable(
                        model.getAlmUserName(),
                        buildVariableResolver,
                        logger),
                model.getAlmPassword(),
                model.getRunType(),
                EnvironmnetVariablesUtils.reolveVariable(
                        model.getAlmEntityId(),
                        buildVariableResolver,
                        logger),
                EnvironmnetVariablesUtils.reolveVariable(
                        model.getTimeslotDuration(),
                        buildVariableResolver,
                        logger),
                model.getDescription(),
                model.getPostRunAction(),
                EnvironmnetVariablesUtils.reolveVariable(
                        model.getEnvironmentConfigurationId(),
                        buildVariableResolver,
                        logger),
                model.getCdaDetails());
    }
}
