package com.hp.octane.plugins.jetbrains.teamcity.utils;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;

/**
 * Created by lazara on 28/12/2015.
 */
public class Utils {

    public static String jacksonRendering(Object obj){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
            String str = mapper.writeValueAsString(obj);
            return str;
        }catch (JsonParseException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
