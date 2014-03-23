package com.hp.application.automation.tools.sse.sdk;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.rest.RestClient;
import com.hp.application.automation.tools.sse.common.TestCase;

public class TestRestClient implements TestCase {
    
    @Test
    public void testBuild() {
        
        final String SUFFIX = "mysuffix";
        String url = new RestClient(URL, null, null, USER).build(SUFFIX);
        
        Assert.assertEquals(String.format("%s/%s", URL, SUFFIX), url);
    }
    
    @Test
    public void testBuildRequest() {
        
        final String SUFFIX = "mysuffix";
        String url = new RestClient(URL, DOMAIN, PROJECT, USER).buildRestRequest(SUFFIX);
        
        Assert.assertEquals(
                String.format("%s/rest/domains/%s/projects/%s/%s", URL, DOMAIN, PROJECT, SUFFIX),
                url);
    }
    
    @Test
    public void testBuildRequestWithSlash() {
        final String URL = "http://16.55.245.168:8081/qcbin/";
        final String SUFFIX = "mysuffix";
        String url = new RestClient(URL, DOMAIN, PROJECT, USER).buildRestRequest(SUFFIX);
        
        Assert.assertEquals(
                String.format("%srest/domains/%s/projects/%s/%s", URL, DOMAIN, PROJECT, SUFFIX),
                url);
    }
}
