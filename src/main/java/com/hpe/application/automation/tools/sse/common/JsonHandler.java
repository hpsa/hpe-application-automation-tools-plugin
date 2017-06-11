package com.hpe.application.automation.tools.sse.common;

import java.io.FileInputStream;
import java.io.InputStream;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.sse.sdk.Logger;
import net.minidev.json.JSONArray;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import hudson.util.IOUtils;

/**
 * Created by barush on 06/11/2014.
 */
public class JsonHandler {
    
    private Logger logger;
    
    public JsonHandler(Logger logger) {
        this.logger = logger;
    }
    
    public Object load(String path) {
        
        logger.log(String.format("Loading JSON file from: [%s]", path));
        Object parsedJson;
        try {
            InputStream is = new FileInputStream(path);
            String jsonTxt;
            jsonTxt = IOUtils.toString(is, "UTF-8");
            parsedJson =
                    Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST).jsonProvider().parse(
                            jsonTxt);
        } catch (Throwable e) {
            throw new SSEException(String.format("Failed to load JSON from: [%s]", path), e);
        }
        
        return parsedJson;
    }
    
    public String getValueFromJsonAsString(
            Object jsonObject,
            String pathToRead,
            boolean shouldGetSingleValueOnly) {
        
        String value = "";
        try {
            Object extractedObject = JsonPath.read(jsonObject, pathToRead);
            while (extractedObject instanceof JSONArray && shouldGetSingleValueOnly) {
                extractedObject = ((JSONArray) extractedObject).get(0);
            }
            value = extractedObject.toString();
        } catch (Throwable e) {
            logger.log(String.format(
                    "Failed to get the value of [%s] from the JSON file.\n\tError was: %s",
                    pathToRead,
                    e.getMessage()));
        }
        return value;
        
    }
}
