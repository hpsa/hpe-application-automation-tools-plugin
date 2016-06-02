package com.hp.application.automation.tools.sse.sdk;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.common.SSEException;
import com.hp.application.automation.tools.model.SseModel;
import com.hp.application.automation.tools.sse.common.TestCase;
import com.hp.application.automation.tools.sse.sdk.handler.BvsRunHandler;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandler;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandlerFactory;
import com.hp.application.automation.tools.sse.sdk.handler.TestSetRunHandler;

public class TestRunHandlerFactory extends TestCase {
    
    @Test
    public void testRunHandlerBVS() {
        
        RunHandler runHandler =
                new RunHandlerFactory().create(new MockRestClientFunctional(
                        URL,
                        DOMAIN,
                        PROJECT,
                        USER), SseModel.BVS, "1001");
        Assert.assertNotNull(runHandler);
        Assert.assertTrue(runHandler.getClass() == BvsRunHandler.class);
    }
    
    @Test
    public void testRunHandlerTestSet() {
        
        RunHandler runHandler =
                new RunHandlerFactory().create(new MockRestClientFunctional(
                        URL,
                        DOMAIN,
                        PROJECT,
                        USER), SseModel.TEST_SET, "1001");
        Assert.assertNotNull(runHandler);
        Assert.assertTrue(runHandler.getClass() == TestSetRunHandler.class);
    }
    
    @Test(expected = SSEException.class)
    public void testRunHandlerUnrecognized() {
        
        new RunHandlerFactory().create(
                new MockRestClientFunctional(URL, DOMAIN, PROJECT, USER),
                "Custom",
                "1001");
    }
}
