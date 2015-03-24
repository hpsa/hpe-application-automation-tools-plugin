package com.hp.mqm.client;

import com.hp.mqm.client.exception.InvalidCredentialsException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import java.io.IOException;
import java.io.InputStream;

public class MqmRestClientImpl extends AbstractMqmRestClient implements MqmRestClient {

    private static final String URI_PUSH_TEST_RESULT_PUSH = "tb/build-push";
    private static final String URI_DOMAIN_PROJECT_CHECK = "defects?query=%7Bid%5B0%5D%7D";

    MqmRestClientImpl(MqmConnectionConfig connectionConfig) {
        super(connectionConfig);
    }

    @Override
    public boolean checkCredentials() {
        try {
            login();
            return true;
        } catch (InvalidCredentialsException e) {
            return false;
        }
    }

    // the simplest implementation because we do not know if domain and project will be exists in future
    public boolean checkDomainAndProject() {
        RequestBuilder requestBuilder = RequestBuilder.get(createRestUri(URI_DOMAIN_PROJECT_CHECK));

        CloseableHttpResponse response = null;
        try {
            response = execute(requestBuilder.build());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RequestErrorException("Domain and project check failed", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void release() {
        logout();
    }

    @Override
    public void postTestResult(InputStream testResultReportStream) {
        RequestBuilder requestBuilder = RequestBuilder.post(createRestUri(URI_PUSH_TEST_RESULT_PUSH))
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
}
