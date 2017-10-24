package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.rest.RESTConstants;
import com.hpe.application.automation.tools.sse.sdk.Client;

import java.util.HashMap;
import java.util.Map;

public class CreateSiteSessionRequest extends GeneralPostRequest {

    public CreateSiteSessionRequest(Client client) {

        super(client);
    }

    @Override
    protected String getUrl() {

        return _client.build("rest/site-session");
    }

    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.TEXT_PLAIN);

        return ret;
    }
}