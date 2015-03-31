package com.hp.mqm.client;

import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.internal.InputStreamSourceEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.IOException;

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
    public void postTestResult(InputStreamSource inputStreamSource) {
        RequestBuilder requestBuilder = RequestBuilder.post(createProjectApiUri(URI_PUSH_TEST_RESULT_PUSH))
                .setEntity(new InputStreamSourceEntity(inputStreamSource, ContentType.APPLICATION_XML));
        postTestResult(requestBuilder.build());
    }

    @Override
    public void postTestResult(File testResultReport) {
        RequestBuilder requestBuilder = RequestBuilder.post(createProjectApiUri(URI_PUSH_TEST_RESULT_PUSH))
                .setEntity(new FileEntity(testResultReport, ContentType.APPLICATION_XML));
        postTestResult(requestBuilder.build());
    }

    private void postTestResult(HttpUriRequest request) {
        CloseableHttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new RequestException("Test result posting failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
            }
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException("Cannot find test result file.", e);
        } catch (IOException e) {
            throw new RequestErrorException("Cannot post test results to MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }
}
