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
    public static final String COOKIE = "Cookie";
    
    public static final String APP_XML = "application/xml";
    public static final String TEXT_PLAIN = "text/plain";
    
    public static String fieldXml(String field, String value) {
        
        return String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", field, value);
    }
}
