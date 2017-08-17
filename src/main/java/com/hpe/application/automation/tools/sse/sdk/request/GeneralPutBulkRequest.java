package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.rest.RESTConstants;
import com.hpe.application.automation.tools.sse.common.RestXmlUtils;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.hpe.application.automation.tools.sse.sdk.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by barush on 03/11/2014.
 */
public abstract class GeneralPutBulkRequest extends GeneralRequest {
    
    protected GeneralPutBulkRequest(Client client) {
        super(client);
    }
    
    protected abstract List<Map<String, String>> getFields();
    
    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML_BULK);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        return ret;
    }
    
    @Override
    protected Response perform() {
        return _client.httpPut(
                getUrl(),
                getDataBytes(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }
    
    private byte[] getDataBytes() {
        
        StringBuilder builder = new StringBuilder("<Entities>");
        for (Map<String, String> values : getFields()) {
            builder.append("<Entity><Fields>");
            for (String key : values.keySet()) {
                builder.append(RestXmlUtils.fieldXml(key, values.get(key)));
            }
            builder.append("</Fields></Entity>");
        }
        
        return builder.append("</Entities>").toString().getBytes();
        
    }
}
