package com.hp.application.automation.tools.sse.sdk.request;

import java.util.HashMap;
import java.util.Map;

import com.hp.application.automation.tools.rest.HttpHeaders;
import com.hp.application.automation.tools.sse.common.RestXmlUtils;
import com.hp.application.automation.tools.sse.sdk.Client;

public class CreateSiteSessionRequest extends GeneralPostRequest {

    public CreateSiteSessionRequest(Client client) {

        super(client);
    }

    @Override
    protected String getUrl() {

        return client.build("rest/site-session");
    }

    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(HttpHeaders.CONTENT_TYPE, RestXmlUtils.TEXT_PLAIN);

        return ret;
    }
}