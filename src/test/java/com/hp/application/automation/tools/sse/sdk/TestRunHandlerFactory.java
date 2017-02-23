/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

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
