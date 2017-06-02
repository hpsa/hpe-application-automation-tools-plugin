/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hpe.application.automation.tools.sse.sdk.authenticator;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.rest.RESTConstants;
import com.hpe.application.automation.tools.sse.sdk.Base64Encoder;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.Logger;
import com.hpe.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.hpe.application.automation.tools.sse.sdk.Response;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class RestAuthenticator implements Authenticator {
    
    public static final String IS_AUTHENTICATED = "rest/is-authenticated";
    public static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    public static final String INVALID_ALM_SERVER_URL = "Invalid ALM Server URL";
    public static final String AUTHENTICATION_INFO = "AuthenticationInfo";
    public static final String USER_NAME = "Username";
    
    public boolean login(Client client, String username, String password, Logger logger) {
        logger.log("Start login to ALM server.");
        boolean ret = true;
        String authenticationPoint = isAuthenticated(client, logger);
        if (authenticationPoint != null) {
            Response response = login(client, authenticationPoint, username, password);
            if (response.isOk()) {
                logLoggedInSuccessfully(username, client.getServerUrl(), logger);
            } else {
                logger.log(String.format(
                        "Login to ALM Server at %s failed. Status Code: %s",
                        client.getServerUrl(),
                        response.getStatusCode()));
                ret = false;
            }
        }
        
        return ret;
    }
    
    /**
     * @param loginUrl
     *            to authenticate at
     * @return true on operation success, false otherwise Basic authentication (must store returned
     *         cookies for further use)
     */
    private Response login(Client client, String loginUrl, String username, String password) {
        
        // create a string that looks like:
        // "Basic ((username:password)<as bytes>)<64encoded>"
        byte[] credBytes = (username + ":" + password).getBytes();
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(RESTConstants.AUTHORIZATION, credEncodedString);
        
        return client.httpGet(loginUrl, null, headers, ResourceAccessLevel.PUBLIC);
    }
    
    /**
     * @return true if logout successful
     * @throws Exception
     *             close session on server and clean session cookies on client
     */
    public boolean logout(Client client, String username) {
        
        // note the get operation logs us out by setting authentication cookies to:
        // LWSSO_COOKIE_KEY="" via server response header Set-Cookie
        Response response =
                client.httpGet(
                        client.build("authentication-point/logout"),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);
        
        return response.isOk();
        
    }
    
    /**
     * @return null if authenticated.<br>
     *         a URL to authenticate against if not authenticated.
     * @throws Exception
     *             if error such as 404, or 500
     */
    private String isAuthenticated(Client client, Logger logger) {
        
        String ret;
        Response response =
                client.httpGet(
                        client.build(IS_AUTHENTICATED),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);
        int responseCode = response.getStatusCode();
        

        if (isAlreadyAuthenticated(response, client.getUsername())) {
            // already authenticated
            ret = null;
            logLoggedInSuccessfully(client.getUsername(), client.getServerUrl(), logger);

        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // if not authenticated - get the address where to authenticate via WWW-Authenticate
            String newUrl = response.getHeaders().get(AUTHENTICATE_HEADER).get(0).split("=")[1];
            newUrl = newUrl.replace("\"", "");
            newUrl += "/authenticate";
            ret = newUrl;

        } else {
            // error such as 404, or 500
            throw new SSEException(response.getFailure());
        }
        
        return ret;
    }
    
    private boolean isAlreadyAuthenticated(Response response, String authUser) {
        boolean ret = false;
        if (response.getStatusCode() == HttpURLConnection.HTTP_OK){
            if (response.getData() != null && containAuthenticatedInfo(new String(response.getData()), authUser)){
                ret = true;
            } else{
                throw new SSEException(INVALID_ALM_SERVER_URL);
            }
        }
        
        return ret;
    }

    //if it's authenticated, the response should look like that:
    //<?xml version="1.0" encoding="UTF-8" standalone="yes"?><AuthenticationInfo><Username>sa</Username></AuthenticationInfo>
    private boolean containAuthenticatedInfo(String authInfo, String authUser){
        return authInfo.contains(AUTHENTICATION_INFO) && authInfo.contains(USER_NAME) && authInfo.contains(authUser);
    }


    private void logLoggedInSuccessfully(String username, String loginServerUrl, Logger logger) {
        
        logger.log(String.format(
                "Logged in successfully to ALM Server %s using %s",
                loginServerUrl,
                username));
    }
}
