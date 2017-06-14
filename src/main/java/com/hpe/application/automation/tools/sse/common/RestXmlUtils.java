package com.hpe.application.automation.tools.sse.common;

import com.hpe.application.automation.tools.rest.RESTConstants;

import java.util.HashMap;
import java.util.Map;


/***
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 */
public class RestXmlUtils {

    public static String fieldXml(String field, String value) {

        return String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", field, value);
    }

    public static Map<String, String> getAppXmlHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        return ret;
    }
}