package com.hp.octane.plugins.jetbrains.teamcity.utils;

import com.hp.nga.integrations.services.serialization.SerializationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created by lazara on 28/12/2015.
 */
public class Utils {

    public static void updateResponse( Object result, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        StringBuilder data = new StringBuilder();

        //BuildConfigurationHolder state = (BuildConfigurationHolder)map.get("ViewState");

        if (result != null) {
            data.append(SerializationService.toJSON(result));
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
