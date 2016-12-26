/**
 *
 */
package com.hp.mqm.atrf.core.rest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;


public class RestConnector {


    static final Logger logger = LogManager.getLogger();
    protected Map<String, String> cookies = new HashMap<>();

    private String baseUrl;
    private static String proxyHost;
    private static int proxyPort;

    private String csrfHeaderName;
    private String csrfCookieName;


    public static void setProxy(String host, int port) {
        proxyHost = host;
        proxyPort = port;
    }

    public void setCSRF(String csrfHeader, String csrfCookieName) {
        this.csrfHeaderName = csrfHeader;
        this.csrfCookieName = csrfCookieName;
    }

    /**
     * @return the cookies
     */
    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * @param cookies the cookies to set
     */
    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Response httpPut(String url, String data, Map<String, String> headers) {

        return doHttp("PUT", url, null, data, headers);
    }

    public Response httpPost(String url, String data, Map<String, String> headers) {

        return doHttp("POST", url, null, data, headers);
    }

    public Response httpDelete(String url, Map<String, String> headers) {

        return doHttp("DELETE", url, null, null, headers);
    }


    public Response httpGet(String url, List<String> queryParams) {

        return httpGet(url, queryParams, null);
    }

    public Response httpGet(String url, List<String> queryParams, Map<String, String> headers) {

        return doHttp("GET", url, queryParams, null, headers);
    }

    /**
     * @param type        of the http operation: get post put delete
     * @param url         to work on
     * @param queryParams
     * @param data        to write, if a writable operation
     * @param headers     to use in the request
     * @return http response
     */
    private Response doHttp(
            String type,
            String url,
            List<String> queryParams,
            String data,
            Map<String, String> headers) {


        //add query params
        if ((queryParams != null) && !queryParams.isEmpty()) {

            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            url += StringUtils.join(queryParams, "&");
        }


        try {
            String fullUrl = baseUrl + url;

            HttpURLConnection con;
            if (StringUtils.isEmpty(proxyHost)) {
                con = (HttpURLConnection) new URL(fullUrl).openConnection();
            } else {
                try {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    con = (HttpURLConnection) new URL(fullUrl).openConnection(proxy);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to define connection with proxy parameters");
                }
            }

            con.setRequestMethod(type);
            String cookieString = getCookieString();

            prepareHttpRequest(con, headers, data, cookieString);
            long start = System.currentTimeMillis();
            con.connect();
            Response ret = retrieveHtmlResponse(con);
            long end = System.currentTimeMillis();
            String msg = String.format("%s %s:%s , total time %s ms", ret.getStatusCode(), type, fullUrl, end - start);
            logger.info(msg);

            updateCookies(ret);

            if (ret.getStatusCode() != HttpStatus.SC_OK && ret.getStatusCode() != HttpStatus.SC_CREATED && ret.getStatusCode() != HttpStatus.SC_ACCEPTED) {
                throw new RestStatusException(ret);
            }

            return ret;
        } catch (RestStatusException e) {
            throw e;//rethrow
        } catch (Exception e) {
            throw new RuntimeException("Failed to doHttp : " + e.getMessage(), e);
        }
    }

    /**
     * @param con          to set the headers and bytes in
     * @param headers      to use in the request, such as content-TYPE
     * @param data         the actual data to post in the connection.
     * @param cookieString the cookies data from clientside, such as lwsso, qcsession, jsession etc..
     */
    private void prepareHttpRequest(
            HttpURLConnection con,
            Map<String, String> headers,
            String data,
            String cookieString) throws IOException {

        String contentType = null;

        //attach cookie information if such exists
        if ((cookieString != null) && !cookieString.isEmpty()) {

            con.setRequestProperty("Cookie", cookieString);
        }

        //send data from headers
        if (headers != null) {

            //skip the content-TYPE header - should only be sent if you actually have any content to send. see below.
            contentType = headers.remove("Content-Type");

            Iterator<Entry<String, String>> headersIterator = headers.entrySet().iterator();
            while (headersIterator.hasNext()) {
                Entry<String, String> header = headersIterator.next();
                con.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        //set CSRF
        if (StringUtils.isNotEmpty(csrfHeaderName) && StringUtils.isNotEmpty(csrfCookieName) && cookies.containsKey(csrfCookieName)) {
            con.setRequestProperty(csrfHeaderName, cookies.get(csrfCookieName));
        }

        //if there's data to attach to the request, it's handled here. note that if data exists, we take into account previously removed content-TYPE.
        if ((data != null) && (!data.isEmpty())) {

            con.setDoOutput(true);

            //warning: if you add content-TYPE header then you MUST send information.. or receive error. so only do so if you're writing information...
            if (contentType != null) {
                con.setRequestProperty("Content-Type", contentType);
            }

            OutputStream out = con.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();
        }
    }

    /**
     * @param con that already connected to it's url with an http request, and that should contain a
     *            response for us to retrieve
     * @return a response from the server to the previously submitted http request
     * @throws Exception
     */
    private Response retrieveHtmlResponse(HttpURLConnection con) throws IOException {

        Response ret = new Response();

        ret.setStatusCode(con.getResponseCode());
        ret.setResponseHeaders(con.getHeaderFields());

        InputStream inputStream;
        //select the source of the input bytes, first try "regular" input
        try {
            inputStream = con.getInputStream();
        }

        /*
         if the connection to the server somehow failed, for example 404 or 500, con.getInputStream() will throw an exception, which we'll keep. 
         we'll also store the body of the exception page, in the response data.
         */ catch (Exception e) {

            inputStream = con.getErrorStream();
            ret.setFailure(e);
        }

        //this actually takes the data from the previously decided stream (error or input) and stores it in a byte[] inside the response
        ByteArrayOutputStream container = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int read;
        while ((read = inputStream.read(buf, 0, 1024)) > 0) {
            container.write(buf, 0, read);
        }

        ret.setResponseData(container.toString());

        return ret;
    }

    private void updateCookies(Response response) {

        Iterable<String> newCookies = response.getResponseHeaders().get("Set-Cookie");
        if (newCookies != null) {

            for (String cookie : newCookies) {
                int equalIndex = cookie.indexOf('=');
                int semicolonIndex = cookie.indexOf(';');

                String cookieKey = cookie.substring(0, equalIndex);
                String cookieValue = cookie.substring(equalIndex + 1);
                if (semicolonIndex != -1) {
                    cookieValue = cookie.substring(equalIndex + 1, semicolonIndex);
                }
                cookies.put(cookieKey, cookieValue);
            }
        }
    }

    private String getCookieString() {

        StringBuilder sb = new StringBuilder();

        if (cookies != null && !cookies.isEmpty()) {

            Set<Entry<String, String>> cookieEntries = cookies.entrySet();
            for (Entry<String, String> entry : cookieEntries) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }

        String ret = sb.toString();

        return ret;
    }

    public void clearAll() {
        cookies = new HashMap<>();
    }

    /**
     * This is the url to the qc application. It will be something like http://myhost:8080/qcbin .
     * Make sure that there is no slash at the end
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String url) {

        this.baseUrl = url;
        if (StringUtils.isNotEmpty(baseUrl) && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 2);
        }
    }
}
