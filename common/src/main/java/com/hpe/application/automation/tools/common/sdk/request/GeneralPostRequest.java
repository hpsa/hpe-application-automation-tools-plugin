package com.hpe.application.automation.tools.common.sdk.request;

import com.hpe.application.automation.tools.common.Pair;
import com.hpe.application.automation.tools.common.rest.HttpHeaders;
import com.hpe.application.automation.tools.common.RestXmlUtils;
import com.hpe.application.automation.tools.common.sdk.Client;
import com.hpe.application.automation.tools.common.sdk.ResourceAccessLevel;
import com.hpe.application.automation.tools.common.sdk.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by barush on 29/10/2014.
 */
public abstract class GeneralPostRequest extends GeneralRequest {
    
    protected GeneralPostRequest(Client client) {
        super(client);
    }
    
    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(HttpHeaders.CONTENT_TYPE, RestXmlUtils.APP_XML);
        ret.put(HttpHeaders.ACCEPT, RestXmlUtils.APP_XML);

        return ret;
    }
    
    @Override
    public Response perform() {
        
        return client.httpPost(
                getUrl(),
                getDataBytes(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
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
