package com.hpe.application.automation.tools.sse.autenvironment.request.get;

import com.hpe.application.automation.tools.sse.autenvironment.request.AUTEnvironmentResources;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.GeneralGetRequest;

/**
 * Created by barush on 30/10/2014.
 */
public class GetParametersByAutEnvConfIdRequest extends GeneralGetRequest {
    
    String configurationId;
    
    public GetParametersByAutEnvConfIdRequest(Client client, String configurationId) {
        
        super(client);
        this.configurationId = configurationId;
    }
    
    @Override
    protected String getSuffix() {
        
        return AUTEnvironmentResources.AUT_ENVIRONMENT_PARAMETER_VALUES;
    }
    
    @Override
    protected String getQueryString() {
        
        return String.format("query={app-param-value-set-id[%s]}&page-size=2000", configurationId);
    }
}
