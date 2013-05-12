package com.hp.application.automation.tools.sse.sdk;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.hp.application.automation.tools.common.SSEException;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class RestAuthenticator {
    
    public static final String IS_AUTHENTICATED = "rest/is-authenticated";
    public static String AUTHENTICATE_HEADER = "WWW-Authenticate";
    
    public boolean login(Client client, String username, String password, Logger logger) {
        
        boolean ret = true;
        String authenticationPoint = isAuthenticated(client, username, logger);
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
        Map<String, String> map = new HashMap<String, String>();
        map.put("Authorization", credEncodedString);
        
        return client.httpGet(loginUrl, null, map);
    }
    
    /**
     * @return true if logout successful
     * @throws Exception
     *             close session on server and clean session cookies on client
     */
    public boolean logout(RestClient client) {
        
        // note the get operation logs us out by setting authentication cookies to:
        // LWSSO_COOKIE_KEY="" via server response header Set-Cookie
        Response response = client.httpGet(client.build("authentication-point/logout"), null, null);
        
        return response.isOk();
        
    }
    
    /**
     * @return null if authenticated.<br>
     *         a URL to authenticate against if not authenticated.
     * @throws Exception
     *             if error such as 404, or 500
     */
    public String isAuthenticated(Client client, String username, Logger logger) {
        
        String ret;
        Response response = client.httpGet(client.build(IS_AUTHENTICATED), null, null);
        int responseCode = response.getStatusCode();
        
        // already authenticated
        if (responseCode == HttpURLConnection.HTTP_OK) {
            ret = null;
            logLoggedInSuccessfully(username, client.getServerUrl(), logger);
        }
        // if not authenticated - get the address where to authenticate via WWW-Authenticate
        else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            String newUrl = response.getHeaders().get(AUTHENTICATE_HEADER).get(0).split("=")[1];
            newUrl = newUrl.replace("\"", "");
            newUrl += "/authenticate";
            ret = newUrl;
        }
        // error such as 404, or 500
        else {
            try {
                throw response.getFailure();
            } catch (Throwable cause) {
                throw new SSEException(cause);
            }
        }
        
        return ret;
    }
    
    private void logLoggedInSuccessfully(String username, String loginServerUrl, Logger logger) {
        
        logger.log(String.format(
                "Logged in successfully to ALM Server %s using %s",
                loginServerUrl,
                username));
    }
}
