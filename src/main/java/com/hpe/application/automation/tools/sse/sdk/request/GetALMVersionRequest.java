package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.rest.RESTConstants;
import com.hpe.application.automation.tools.sse.common.RestXmlUtils;
import com.hpe.application.automation.tools.sse.sdk.Client;

import java.util.Map;

/**
 * @author Effi Bar-She'an
 */
public class GetALMVersionRequest extends GetRequest {

    public GetALMVersionRequest(Client client) {

        super(client, null);
    }

    @Override
    protected String getSuffix() {

        return String.format("%s/sa/version", RESTConstants.REST_PROTOCOL);
    }

    @Override
    protected String getUrl() {

        return String.format("%s%s", _client.getServerUrl(), getSuffix());
    }

    @Override
    protected Map<String, String> getHeaders() {

        return RestXmlUtils.getAppXmlHeaders();
    }
}