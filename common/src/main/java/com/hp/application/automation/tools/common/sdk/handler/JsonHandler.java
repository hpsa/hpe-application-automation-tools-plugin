/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package com.hp.application.automation.tools.common.sdk.handler;

import com.hp.application.automation.tools.common.SSEException;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import net.minidev.json.JSONArray;

import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

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
