package com.hpe.application.automation.tools.sse.autenvironment.request.post;

import java.util.ArrayList;
import java.util.List;

import com.hpe.application.automation.tools.common.Pair;
import com.hpe.application.automation.tools.sse.autenvironment.request.AUTEnvironmentResources;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.GeneralPostRequest;

/**
 * Created by barush on 29/10/2014.
 */
public class CreateAutEnvConfRequest extends GeneralPostRequest {
    
    private String autEnvironmentId;
    private String name;
    
    public CreateAutEnvConfRequest(Client client, String autEnvironmentId, String name) {
        
        super(client);
        this.autEnvironmentId = autEnvironmentId;
        this.name = name;
    }
    
    @Override
    protected String getSuffix() {
        return AUTEnvironmentResources.AUT_ENVIRONMENT_CONFIGURATIONS;
    }
    
    @Override
    protected List<Pair<String, String>> getDataFields() {
        
        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        ret.add(new Pair<String, String>("app-param-set-id", autEnvironmentId));
        ret.add(new Pair<String, String>("name", name));
        
        return ret;
    }
}
