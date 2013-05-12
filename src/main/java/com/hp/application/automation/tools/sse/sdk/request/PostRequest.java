package com.hp.application.automation.tools.sse.sdk.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.application.automation.tools.common.Pair;
import com.hp.application.automation.tools.sse.common.RestXmlUtils;
import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.Response;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public abstract class PostRequest extends Request {
    
    protected PostRequest(Client client, String runId) {
        
        super(client, runId);
    }
    
    @Override
    protected Map<String, String> getHeaders() {
        
        return RestXmlUtils.getAppXmlHeaders();
    }
    
    @Override
    public Response execute() {
        
        Response ret = new Response();
        try {
            ret = _client.httpPost(getUrl(), getDataBytes(), getHeaders());
        } catch (Throwable cause) {
            ret.setFailure(cause);
        }
        
        return ret;
    }
    
    private byte[] getDataBytes() {
        
        StringBuilder builder = new StringBuilder("<Entity><Fields>");
        for (Pair<String, String> currPair : getDataFields()) {
            builder.append(RestXmlUtils.fieldXml(currPair.getFirst(), currPair.getSecond()));
        }
        
        return builder.append("</Fields></Entity>").toString().getBytes();
    }
    
    protected List<Pair<String, String>> getDataFields() {
        
        return new ArrayList<Pair<String, String>>(0);
    }
}
