package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.rest.HttpHeaders;
import com.hp.application.automation.tools.common.RestXmlUtils;
import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.ResourceAccessLevel;
import com.hp.application.automation.tools.common.sdk.Response;

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
        ret.put(HttpHeaders.CONTENT_TYPE, RestXmlUtils.APP_XML_BULK);
        ret.put(HttpHeaders.ACCEPT, RestXmlUtils.APP_XML);

        return ret;
    }
    
    @Override
    protected Response perform() {
        return client.httpPut(
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
