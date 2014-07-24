package com.hp.application.automation.tools.pc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

import com.hp.application.automation.tools.common.PcException;
import com.hp.application.automation.tools.run.PcBuilder;

import static com.hp.application.automation.tools.pc.PcTestBase.*;
import static com.hp.application.automation.tools.pc.RunState.*;

public class MockPcRestProxy extends PcRestProxy {
    
    private static Iterator<RunState> runState = initializeRunStateIterator();
    
    public MockPcRestProxy(String pcServerName, String almDomain, String almProject) {
        super(pcServerName, almDomain, almProject);
    }

    @Override
    protected HttpResponse executeRequest(HttpRequestBase request) throws PcException, ClientProtocolException,
            IOException {
        HttpResponse response = null;
        String requestUrl = request.getURI().toString();
        if (requestUrl.equals(String.format(AUTHENTICATION_LOGIN_URL, PC_SERVER_NAME))
                || requestUrl.equals(String.format(AUTHENTICATION_LOGOUT_URL, PC_SERVER_NAME))
                || requestUrl.equals(String.format(getBaseURL() + "/%s/%s/%s", RUNS_RESOURCE_NAME, RUN_ID, STOP_MODE))) {
            response = getOkResponse();
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s", RUNS_RESOURCE_NAME))
                || requestUrl.equals(String.format(getBaseURL() + "/%s/%s", RUNS_RESOURCE_NAME, RUN_ID))) {
            response = getOkResponse();
            response.setEntity(new StringEntity(PcTestBase.runResponseEntity));
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s/%s", RUNS_RESOURCE_NAME, RUN_ID_WAIT))) {
            response = getOkResponse();
            response.setEntity(new StringEntity(PcTestBase.runResponseEntity.replace("*", runState.next().value())));
            if (!runState.hasNext())
                runState = initializeRunStateIterator();
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s/%s/%s", RUNS_RESOURCE_NAME, RUN_ID,
            RESULTS_RESOURCE_NAME))) {
            response = getOkResponse();
            response.setEntity(new StringEntity(PcTestBase.runResultsEntity));
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s/%s/%s/%s/data", RUNS_RESOURCE_NAME, RUN_ID,
            RESULTS_RESOURCE_NAME, REPORT_ID))) {
            response = getOkResponse();
            response.setEntity(new FileEntity(
                new File(getClass().getResource(PcBuilder.pcReportArchiveName).getPath()), ContentType.DEFAULT_BINARY));
        }
        if (response == null)
            throw new PcException(String.format("%s %s is not recognized by PC Rest Proxy", request.getMethod(), requestUrl));
        return response;
    }
    
    private HttpResponse getOkResponse(){
        
        return new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
    }
    
    private static Iterator<RunState> initializeRunStateIterator(){
        
        return Arrays.asList(INITIALIZING,RUNNING,COLLATING_RESULTS,CREATING_ANALYSIS_DATA,FINISHED).iterator();
    }
}
