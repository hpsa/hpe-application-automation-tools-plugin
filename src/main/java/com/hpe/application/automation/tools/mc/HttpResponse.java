package com.hpe.application.automation.tools.mc;

import net.minidev.json.JSONObject;

import java.util.List;
import java.util.Map;

public class HttpResponse {

    private Map<String, List<String>> headers;
    private JSONObject jsonObject;

    public HttpResponse() {

    }

    public HttpResponse(Map<String, List<String>> headers, JSONObject jsonObject) {
        this.headers = headers;
        this.jsonObject = jsonObject;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
