package com.hpe.application.automation.tools.sse.sdk.request;

import java.util.ArrayList;
import java.util.List;

import com.hpe.application.automation.tools.common.Pair;
import com.hpe.application.automation.tools.model.CdaDetails;
import com.hpe.application.automation.tools.sse.common.StringUtils;
import com.hpe.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class StartRunEntityRequest extends PostRequest {
    
    private final String _duration;
    private final String _suffix;
    private final String _environmentConfigurationId;
    private final CdaDetails _cdaDetails;
    
    public StartRunEntityRequest(
            Client client,
            String suffix,
            String runId,
            String duration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {
        
        super(client, runId);
        _duration = duration;
        _suffix = suffix;
        _environmentConfigurationId = environmentConfigurationId;
        _cdaDetails = cdaDetails;
    }
    
    @Override
    protected List<Pair<String, String>> getDataFields() {
        
        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        ret.add(new Pair<String, String>("duration", _duration));
        ret.add(new Pair<String, String>("vudsMode", "false"));
        ret.add(new Pair<String, String>("reservationId", "-1"));
        if (!StringUtils.isNullOrEmpty(_environmentConfigurationId)) {
            ret.add(new Pair<String, String>("valueSetId", _environmentConfigurationId));
            if (_cdaDetails != null) {
                ret.add(new Pair<String, String>("topologyAction", _cdaDetails.getTopologyAction()));
                ret.add(new Pair<String, String>(
                        "realizedTopologyName",
                        _cdaDetails.getDeployedEnvironmentName()));
                
            }
        }
        
        return ret;
    }
    
    @Override
    protected String getSuffix() {
        
        return _suffix;
    }
    
}
