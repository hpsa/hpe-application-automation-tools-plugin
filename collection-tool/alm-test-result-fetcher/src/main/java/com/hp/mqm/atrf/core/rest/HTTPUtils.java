package com.hp.mqm.atrf.core.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 */
public class HTTPUtils {
    public static String HEADER_ACCEPT = "Accept";
    public static String HEADER_APPLICATION_JSON = "application/json";
    public static String HEADER_APPLICATION_XML = "application/xml";
    public static String HEADER_CONTENT_TYPE = "Content-Type";

    public static String UTF8 = "UTF-8";


    public static String encodeParam(String param) {
        String ret;

        try {
            ret = URLEncoder.encode(param, HTTPUtils.UTF8);
        } catch (UnsupportedEncodingException e) {
            ret = "";
        }

        return ret;
    }
}
