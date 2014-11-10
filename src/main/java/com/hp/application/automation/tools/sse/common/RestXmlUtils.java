package com.hp.application.automation.tools.sse.common;

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
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APP_XML_BULK = "application/xml;type=collection";
    
    public static String fieldXml(String field, String value) {
        
        return String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", field, value);
    }
}
