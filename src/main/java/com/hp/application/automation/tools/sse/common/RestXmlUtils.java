package com.hp.application.automation.tools.sse.common;

import java.util.HashMap;
import java.util.Map;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class RestXmlUtils {
    
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String COOKIE = "Cookie";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APP_XML = "application/xml";
    
    public static String fieldXml(String field, String value) {
        
        return String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", field, value);
    }
    
    public static Map<String, String> getAppXmlHeaders() {
        
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(CONTENT_TYPE, APP_XML);
        ret.put("Accept", APP_XML);
        
        return ret;
    }
}
