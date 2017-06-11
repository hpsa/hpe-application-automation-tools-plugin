package com.hpe.application.automation.tools.sse.autenvironment.request.get;

import com.hpe.application.automation.tools.sse.autenvironment.request.AUTEnvironmentResources;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.GeneralGetRequest;

/**
 * Created by barush on 02/11/2014.
 */
public class GetAutEnvFoldersByIdRequest extends GeneralGetRequest {
    
    private String folderId;
    
    public GetAutEnvFoldersByIdRequest(Client client, String folderId) {
        
        super(client);
        this.folderId = folderId;
    }
    
    @Override
    protected String getSuffix() {
        
        return AUTEnvironmentResources.AUT_ENVIRONMENT_PARAMETER_FOLDERS;
    }
    
    @Override
    protected String getQueryString() {
        
        return String.format("query={id[%s]}&page-size=2000", folderId);
    }
}
