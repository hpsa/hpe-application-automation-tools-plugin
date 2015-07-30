package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.rest.HttpHeaders;
import com.hp.application.automation.tools.common.RestXmlUtils;
import com.hp.application.automation.tools.common.sdk.Client;

import java.util.HashMap;
import java.util.Map;

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