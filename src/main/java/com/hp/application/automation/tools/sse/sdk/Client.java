package com.hp.application.automation.tools.sse.sdk;

import java.util.Map;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public interface Client {
    
    Response httpGet(String url, String queryString, Map<String, String> headers);
    
    Response httpPost(String url, byte[] data, Map<String, String> headers);
    
    String build(String suffix);
    
    String buildRestRequest(String suffix);
    
    String buildWebUIRequest(String suffix);
    
    String getServerUrl();
}
