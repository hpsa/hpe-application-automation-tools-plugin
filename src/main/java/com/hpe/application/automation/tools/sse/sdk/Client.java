package com.hpe.application.automation.tools.sse.sdk;

import java.util.Map;

/***
 *
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 *
 */
public interface Client {

    Response httpGet(
            String url,
            String queryString,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel);

    Response httpPost(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel);

    Response httpPut(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel);

    String build(String suffix);

    String buildRestRequest(String suffix);

    String buildWebUIRequest(String suffix);

    String getServerUrl();

    String getUsername();

    Map<String, String> getCookies();
}
