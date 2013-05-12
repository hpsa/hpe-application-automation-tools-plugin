package com.hp.application.automation.tools.sse.sdk;

import org.junit.Assert;
import org.junit.Test;

public class TestRestClient {
    
    @Test
    public void testBuild() {
        
        final String URL = "http://16.55.245.168:8081/qcbin";
        final String SUFFIX = "mysuffix";
        String url = new RestClient(URL, null, null).build(SUFFIX);
        
        Assert.assertEquals(String.format("%s/%s", URL, SUFFIX), url);
    }
    
    @Test
    public void testBuildRequest() {
        
        final String URL = "http://16.55.245.168:8081/qcbin";
        final String DOMAIN = "demo";
        final String PROJECT = "prj1";
        final String SUFFIX = "mysuffix";
        String url = new RestClient(URL, DOMAIN, PROJECT).buildRestRequest(SUFFIX);
        
        Assert.assertEquals(
                String.format("%s/rest/domains/%s/projects/%s/%s", URL, DOMAIN, PROJECT, SUFFIX),
                url);
    }
    
    @Test
    public void testBuildRequestWithSlash() {
        final String URL = "http://16.55.245.168:8081/qcbin/";
        final String DOMAIN = "demo";
        final String PROJECT = "prj1";
        final String SUFFIX = "mysuffix";
        String url = new RestClient(URL, DOMAIN, PROJECT).buildRestRequest(SUFFIX);
        
        Assert.assertEquals(
                String.format("%srest/domains/%s/projects/%s/%s", URL, DOMAIN, PROJECT, SUFFIX),
                url);
    }
}
