package com.hp.application.automation.tools.sse.autenvironment.request.put;

import java.util.*;

import com.hp.application.automation.tools.sse.autenvironment.AUTEnvironmnentParameter;
import com.hp.application.automation.tools.sse.autenvironment.request.AUTEnvironmentResources;
import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.request.GeneralPutBulkRequest;

/**
 * Created by barush on 03/11/2014.
 */
public class PutAutEnvironmentParametersRequest extends GeneralPutBulkRequest {
    
    Collection<AUTEnvironmnentParameter> parameters;
    
    public PutAutEnvironmentParametersRequest(
            Client client,
            Collection<AUTEnvironmnentParameter> parameters) {
        super(client);
        this.parameters = parameters;
    }
    
    @Override
    protected List<Map<String, String>> getFields() {
        
        List<Map<String, String>> fieldsToUpdate = new ArrayList<Map<String, String>>();
        for (AUTEnvironmnentParameter autEnvironmnentParameter : parameters) {
            Map<String, String> mapOfValues = new HashMap<String, String>();
            mapOfValues.put(
                    AUTEnvironmnentParameter.ALM_PARAMETER_ID_FIELD,
                    autEnvironmnentParameter.getId());
            mapOfValues.put(
                    AUTEnvironmnentParameter.ALM_PARAMETER_VALUE_FIELD,
                    autEnvironmnentParameter.getValue());
            fieldsToUpdate.add(mapOfValues);
        }
        
        return fieldsToUpdate;
    }
    
    @Override
    protected String getSuffix() {
        return AUTEnvironmentResources.AUT_ENVIRONMENT_PARAMETER_VALUES;
    }
}
