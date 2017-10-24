package com.hpe.application.automation.tools.sse.sdk;

import com.hpe.application.automation.tools.common.ALMRESTVersionUtils;
import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.model.ALMVersion;
import com.hpe.application.automation.tools.sse.sdk.request.GetALMVersionRequest;

/**
 * @author Effi Bar-She'an
 */
public class ALMRunReportUrlBuilder {

    public String build(Client client, String serverUrl, String domain, String project, String runId) {

        String ret = "NA";
        try {
            if (isNewReport(client)) {
                ret = String.format("%sui/?redirected&p=%s/%s&execution-report#/test-set-report/%s",
                        serverUrl,
                        domain,
                        project,
                        runId);
            } else {
                ret = client.buildWebUIRequest(String.format("lab/index.jsp?processRunId=%s", runId));
            }
        } catch (Exception e) {
            // result url will be NA (in case of failure like getting ALM version, convert ALM version to number)
        }

        return ret;
    }

    public boolean isNewReport(Client client) {

        ALMVersion version = getALMVersion(client);

        return toInt(version.getMajorVersion()) >= 12 && toInt(version.getMinorVersion()) >= 2;
    }

    private int toInt(String str) {

        return Integer.parseInt(str);
    }

    private ALMVersion getALMVersion(Client client) {

        ALMVersion ret = null;
        Response response = new GetALMVersionRequest(client).execute();
        if(response.isOk()) {
            ret = ALMRESTVersionUtils.toModel(response.getData());
        } else {
            throw new SSEException(
                    String.format("Failed to get ALM version. HTTP status code: %d", response.getStatusCode()),
                    response.getFailure());
        }

        return ret;
    }
}
