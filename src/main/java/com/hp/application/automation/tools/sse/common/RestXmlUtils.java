package com.hp.application.automation.tools.sse.common;

import java.util.HashMap;
import java.util.Map;

import com.hp.application.automation.tools.rest.HttpHeaders;


/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class RestXmlUtils {
    
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String COOKIE = "Cookie";
    
    public static final String APP_XML = "application/xml";
    public static final String APP_XML_BULK = "application/xml;type=collection";
    
    public static String fieldXml(String field, String value) {
        
        return String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", field, value);
    }
    
    public static Map<String, String> getAppXmlHeaders() {
        
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(HttpHeaders.CONTENT_TYPE, APP_XML);
        ret.put(HttpHeaders.ACCEPT, APP_XML);
        
        return ret;
    }
    
    public static Map<String, String> getAppXmlBulkHeaders() {
        
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(HttpHeaders.CONTENT_TYPE, APP_XML_BULK);
        ret.put(HttpHeaders.ACCEPT, APP_XML);
        
        return ret;
    }
}
