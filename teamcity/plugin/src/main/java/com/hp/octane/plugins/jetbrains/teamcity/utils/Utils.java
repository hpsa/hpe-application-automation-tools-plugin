package com.hp.octane.plugins.jetbrains.teamcity.utils;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
    public static void updateResponse( Object state, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        StringBuilder data = new StringBuilder();
        if (state != null) {
            data.append(Utils.jacksonRendering(state));
        }

        String[] jsonp = request.getParameterValues("jsonp");

        if (jsonp != null) {
            data.insert(0, jsonp[0] + "(");
            data.append(")\n");
        } else {
            data.append("\n");
        }
        PrintWriter writer = response.getWriter();
        writer.write(data.toString());
    }
}
