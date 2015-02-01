package com.hp.application.automation.tools.sse.sdk;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.rest.RestClient;
import com.hp.application.automation.tools.sse.common.StringUtils;
import com.hp.application.automation.tools.sse.common.TestCase;
import com.hp.application.automation.tools.sse.sdk.request.PostRequest;

public class TestPostRequest implements TestCase {
    
    @Test
    public void testPostRequestException() {
        
        Response response =
                new MockPostRequest(new MockRestClientPostRequestException(
                        URL,
                        DOMAIN,
                        PROJECT,
                        USER), RUN_ID).execute();
        Assert.assertTrue(PostRequestException.class.equals(response.getFailure().getClass()));
    }
    
    private class MockRestClientPostRequestException extends RestClient {
        
        public MockRestClientPostRequestException(
                String url,
                String domain,
                String project,
                String username) {
            
            super(url, domain, project, username);
        }
        
        @Override
        public Response httpPost(
                String url,
                byte[] data,
                Map<String, String> headers,
                ResourceAccessLevel resourceAccessLevel) {
            
            throw new PostRequestException();
        }
    }
    
    private class PostRequestException extends NullPointerException {
        
        private static final long serialVersionUID = 1L;
        
    }
    
    private class MockPostRequest extends PostRequest {
        
        protected MockPostRequest(Client client, String runId) {

            super(client, runId);
        }
        
        @Override
        protected String getSuffix() {
            
            return StringUtils.EMPTY_STRING;
        }
    }
}
