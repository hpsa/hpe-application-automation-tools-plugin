package com.hp.application.automation.tools.sse.sdk;

import com.hp.application.automation.tools.rest.RestClient;
import com.hp.application.automation.tools.sse.common.TestCase;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandler;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandlerFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * @author Effi Bar-She'an
 */
@Ignore
public class TestBvsRunHandler extends TestCase {

    @Test
    public void testStart() {

        Client client = new MockRestStartClient(URL, DOMAIN, PROJECT, USER);
        Response response =
                new RunHandlerFactory().create(client, "BVS", ENTITY_ID).start(
                        DURATION,
                        POST_RUN_ACTION,
                        ENVIRONMENT_CONFIGURATION_ID,
                        null);
        Assert.assertTrue(response.isOk());
    }

    private class MockRestStartClient extends RestClient {

        public MockRestStartClient(String url, String domain, String project, String username) {

            super(url, domain, project, username);
        }

        @Override
        public Response httpPost(String url, byte[] data, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {

            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }

    @Test
    public void testStop() {

        Client client = new MockRestStopClient(URL, DOMAIN, PROJECT, USER);
        Response response = new RunHandlerFactory().create(client, "BVS", "23").stop();
        Assert.assertTrue(response.isOk());
    }

    @Test
    public void testReportUrl() {

        RunHandler handler =
                new RunHandlerFactory().create(
                        new RestClient(URL, DOMAIN, PROJECT, USER),
                        "BVS",
                        "1001");
        handler.setRunId("1");
        Assert.assertTrue(String.format(
                "%s/webui/alm/%s/%s/lab/index.jsp?processRunId=1",
                URL,
                DOMAIN,
                PROJECT).equals(
                handler.getReportUrl(createArgs())));

    }

    private class MockRestStopClient extends RestClient {

        public MockRestStopClient(String url, String domain, String project, String username) {

            super(url, domain, project, username);
        }

        @Override
        public Response httpPost(String url, byte[] data, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {

            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }
}