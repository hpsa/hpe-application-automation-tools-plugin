package com.hp.mqm.client;

import java.io.InputStream;

/**
 * Client for connection to MQM public API. It wraps whole http communication with MQM server.
 *
 * <p>
 * All methods can throw {@link com.hp.mqm.client.exception.RequestException} when unexpected result is returned from
 * MQM server and {@link com.hp.mqm.client.exception.RequestErrorException} in case of IO error or error in the HTTP protocol.
 * <p/>
 *
 * <p>
 * Because client cares about login all methods except {@link #release()} can throw
 * {@link com.hp.mqm.client.exception.AuthenticationException} in case authentication failed and
 * {@link com.hp.mqm.client.exception.AuthenticationErrorException} in case of IO error or error in the HTTP protocol
 * during authentication.
 * </p>
 *
 */
public interface MqmRestClient {

    /**
     * Checks credentials and returns true if credentials are valid and vice versa.
     * @return true if credentials are valid and vice versa
     */
    boolean checkCredentials();

    /**
     * Checks if domain and project exists.
     * @return true if domain and project exist
     */
    boolean checkDomainAndProject();

    /**
     * Posts test results to MQM. Test result can be large data and therefore be aware to keep it in memory.
     * @param testResultReportStream input stream with test results in MQM XML format.
     */
    void postTestResult(InputStream testResultReportStream);

    /**
     * This method should be called when client is not needed. It performs logout and releases all system resources if it is necessary.
     */
    void release();
}
