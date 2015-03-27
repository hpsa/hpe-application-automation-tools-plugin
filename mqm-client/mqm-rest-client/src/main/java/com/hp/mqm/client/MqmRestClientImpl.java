package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MqmRestClientImpl extends AbstractMqmRestClient implements MqmRestClient {

    private static final String URI_PUSH_TEST_RESULT_PUSH = "test-results/v1";

    /**
     * Constructor for AbstractMqmRestClient.
     * @param connectionConfig MQM connection configuration, Fields 'location', 'domain', 'project' and 'clientType' must not be null or empty.
     */
    MqmRestClientImpl(MqmConnectionConfig connectionConfig) {
        super(connectionConfig);
    }

    @Override
    public boolean checkLogin() {
        try {
            login();
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @Override
    public void release() {
        logout();
    }

    @Override
    public void postTestResult(InputStream testResultReportStream) {
        RequestBuilder requestBuilder = RequestBuilder.post(createProjectApiUri(URI_PUSH_TEST_RESULT_PUSH))
                .setEntity(new InputStreamEntity(testResultReportStream, ContentType.APPLICATION_XML));

        CloseableHttpResponse response = null;
        try {
            response = execute(requestBuilder.build());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new RequestException("Test result posting failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new RequestErrorException("Cannot post test results to MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            IOUtils.closeQuietly(testResultReportStream);
        }
    }

    @Override
    public void postTestResult(File testResultReport) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(testResultReport);
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException("Cannot find test result file " + testResultReport.getPath() + ".",e);
        }
        postTestResult(inputStream);
    }
}
