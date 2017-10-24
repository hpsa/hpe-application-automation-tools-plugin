package com.hpe.application.automation.tools.sse.autenvironment.request.get;

import com.hpe.application.automation.tools.sse.autenvironment.request.AUTEnvironmentResources;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.GeneralGetRequest;

/**
 * Created by barush on 03/11/2014.
 */
public class GetAutEnvironmentConfigurationByIdRequest extends GeneralGetRequest {
    
    private String autEnvironmentConfigurationId;
    
    public GetAutEnvironmentConfigurationByIdRequest(
            Client client,
            String autEnvironmentConfigurationId) {
        
        super(client);
        this.autEnvironmentConfigurationId = autEnvironmentConfigurationId;
    }
    
    @Override
    protected String getSuffix() {
        
        return AUTEnvironmentResources.AUT_ENVIRONMENT_CONFIGURATIONS;
        
    }
    
    @Override
    protected String getQueryString() {
        
        return String.format("query={id[%s]}", autEnvironmentConfigurationId);
    }
}
