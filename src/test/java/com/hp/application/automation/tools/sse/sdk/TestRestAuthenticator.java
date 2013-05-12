package com.hp.application.automation.tools.sse.sdk;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.sse.common.ConsoleLogger;

public class TestRestAuthenticator {
    
    @Test
    public void testLogin_alreadyAuthenticated() {
        
        final String URL = "http://16.55.245.168:8081/qcbin";
        final String DOMAIN = "demo";
        final String PROJECT = "prj1";
        Client client = new MockRestClientAlreadyAuthenticated(URL, DOMAIN, PROJECT);
        boolean ok =
                new RestAuthenticator().login(client, "tester", "blabla", new ConsoleLogger());
        Assert.assertTrue(ok);
    }
    
    public class MockRestClientAlreadyAuthenticated extends RestClient {
        
        public MockRestClientAlreadyAuthenticated(String url, String domain, String project) {
            
            super(url, domain, project);
        }
        
        @Override
        public Response httpGet(String url, String queryString, Map<String, String> headers) {
            
            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }
    
    @Test
    public void testLogin_notAuthenticated() {
        
        final String URL = "http://16.55.245.168:8081/qcbin";
        final String DOMAIN = "demo";
        final String PROJECT = "prj1";
        Client client = new MockRestClientNotAuthenticated(URL, DOMAIN, PROJECT);
        boolean ok =
                new RestAuthenticator().login(client, "tester", "blabla", new ConsoleLogger());
        Assert.assertTrue(ok);
    }
    
    public class MockRestClientNotAuthenticated extends RestClient {
        
        private int _time = 0;
        private final String _isAuthenticatedUrl;
        private final String _authenticationUrl;
        
        private MockRestClientNotAuthenticated(String url, String domain, String project) {
            
            super(url, domain, project);
            _isAuthenticatedUrl = build(RestAuthenticator.IS_AUTHENTICATED);
            _authenticationUrl = build("authentication-point/authenticate");
        }
        
        @Override
        public Response httpGet(String url, String queryString, Map<String, String> headers) {
            
            Response ret = null;
            if (++_time == 1) {
                Assert.assertEquals(_isAuthenticatedUrl, url);
                ret =
                        new Response(
                                getAuthenticationHeaders(),
                                null,
                                null,
                                HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (_time == 2) {
                Assert.assertEquals(_authenticationUrl, url);
                ret = new Response(null, null, null, HttpURLConnection.HTTP_OK);
            }
            Assert.assertTrue("More than 2 calls to httpGet", _time < 3);
            
            return ret;
        }
        
        private Map<String, List<String>> getAuthenticationHeaders() {
            
            Map<String, List<String>> ret = new HashMap<String, List<String>>(1);
            List<String> values = new ArrayList<String>();
            values.add(String.format("LWSSO realm=\"%s\"", build("authentication-point")));
            ret.put(RestAuthenticator.AUTHENTICATE_HEADER, values);
            
            return ret;
        }
    }
    
    @Test
    public void testLogin_failedToLogin() {
        
        final String URL = "http://16.55.245.168:8081/qcbin";
        final String DOMAIN = "demo";
        final String PROJECT = "prj1";
        Client client = new MockRestClientFailedToLogin(URL, DOMAIN, PROJECT);
        boolean ok =
                new RestAuthenticator().login(client, "tester", "blabla", new ConsoleLogger());
        Assert.assertFalse(ok);
    }
    
    public class MockRestClientFailedToLogin extends RestClient {
        
        private int _time = 0;
        private final String _isAuthenticatedUrl;
        private final String _authenticationUrl;
        
        private MockRestClientFailedToLogin(String url, String domain, String project) {
            
            super(url, domain, project);
            _isAuthenticatedUrl = build(RestAuthenticator.IS_AUTHENTICATED);
            _authenticationUrl = build("authentication-point/authenticate");
        }
        
        @Override
        public Response httpGet(String url, String queryString, Map<String, String> headers) {
            
            Response ret = null;
            if (++_time == 1) {
                Assert.assertEquals(_isAuthenticatedUrl, url);
                ret =
                        new Response(
                                getAuthenticationHeaders(),
                                null,
                                null,
                                HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (_time == 2) {
                Assert.assertEquals(_authenticationUrl, url);
                ret = new Response(null, null, null, HttpURLConnection.HTTP_UNAUTHORIZED);
            }
            Assert.assertTrue("More than 2 calls to httpGet", _time < 3);
            
            return ret;
        }
        
        private Map<String, List<String>> getAuthenticationHeaders() {
            
            Map<String, List<String>> ret = new HashMap<String, List<String>>(1);
            List<String> values = new ArrayList<String>();
            values.add(String.format("LWSSO realm=\"%s\"", build("authentication-point")));
            ret.put(RestAuthenticator.AUTHENTICATE_HEADER, values);
            
            return ret;
        }
    }
}
