package com.hp.application.automation.tools.sse;

import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.sse.sdk.Args;

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
}
