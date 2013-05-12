package com.hp.application.automation.tools.sse.sdk;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.sse.common.TestCase;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandler;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandlerFactory;

/**
 * 
 * @author Effi Bar-Shean and Dani Schreiber
 * 
 */
public class TestPCRunHandler implements TestCase {
    
    @Test
    public void testStart() {
        
        Client client = new MockRestStartClient(URL, DOMAIN, PROJECT);
        Response response =
                new RunHandlerFactory().create(client, "PC", ENTITY_ID).start(
                        DURATION,
                        POST_RUN_ACTION,
                        "",
                        null);
        Assert.assertTrue(response.isOk());
    }
    
    private class MockRestStartClient extends RestClient {
        
        public MockRestStartClient(String url, String domain, String project) {
            
            super(url, domain, project);
        }
        
        @Override
        public Response httpPost(String url, byte[] data, Map<String, String> headers) {
            
            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }
    
    @Test
    public void testStop() {
        
        Client client = new MockRestStopClient(URL, DOMAIN, PROJECT);
        Response response = new RunHandlerFactory().create(client, "PC", "23").stop();
        Assert.assertTrue(response.isOk());
    }
    
    @Test
    public void testReportUrl() {
        RunHandler handler =
                new RunHandlerFactory().create(new RestClient(URL, DOMAIN, PROJECT), "PC", "1001");
        handler.setRunId("1");
        
        try {
            Assert.assertTrue(String.format(
                    "td://%s.%s.%s:8080/qcbin/[TestRuns]?EntityLogicalName=run&EntityID=%s",
                    PROJECT,
                    DOMAIN,
                    new URL(URL).getHost(),
                    "1").equals(
                    handler.getReportUrl(new Args(
                            URL,
                            DOMAIN,
                            PROJECT,
                            USER,
                            PASS,
                            ENTITY_ID,
                            DESCRIPTION,
                            POST_RUN_ACTION,
                            "",
                            null,
                            null,
                            null))));
        } catch (MalformedURLException e) {
            Assert.fail();
        }
        
    }
    
    private class MockRestStopClient extends RestClient {
        
        public MockRestStopClient(String url, String domain, String project) {
            
            super(url, domain, project);
        }
        
        @Override
        public Response httpPost(String url, byte[] data, Map<String, String> headers) {
            
            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }
    
}
