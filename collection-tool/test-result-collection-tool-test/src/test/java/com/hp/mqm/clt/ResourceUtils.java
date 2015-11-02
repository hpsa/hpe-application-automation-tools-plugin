// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt;

import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class ResourceUtils {

    public static String readContent(String fileName) throws IOException {
        return IOUtils.toString(RestClientTest.class.getResourceAsStream(fileName), "UTF-8");
    }

    public static JSONObject readJson(String fileName) throws IOException {
        return JSONObject.fromObject(readContent(fileName));
    }
}
