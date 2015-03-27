package com.hp.mqm.client;

import java.io.File;
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
 * Because client cares about login automatically all methods except {@link #release()} can
 * throw {@link com.hp.mqm.client.exception.AuthenticationException} in case authentication failed and
 * {@link com.hp.mqm.client.exception.AuthenticationErrorException} in case of IO error or error in the HTTP protocol
 * during authentication.
 * </p>
 *
 */
public interface MqmRestClient {

    /**
     * Tries login and when it fails, it returns true.
     * @return true if login passes and vice versa
     */
    boolean checkLogin();

    /**
     * Checks if domain and project exists.
     * @return true if domain and project exist
     */
    boolean checkDomainAndProject();

    /**
     * Posts test results to MQM. Test results can be large data and therefore be aware to keep it in memory.
     * Also divide extra large test results into smaller parts which will be posted individually
     * (multiple invocation of this method) to avoid HTTP request timeout.
     * <p/>
     * InputStream is automatically closed after all data are read.
     * @param testResultReportStream input stream with test results in MQM XML format.
     */
    void postTestResult(InputStream testResultReportStream);

    /**
     * Posts test results to MQM. Divide extra large test results into smaller files which will be posted individually
     * (multiple invocation of this method) to avoid HTTP request timeout.
     * @param  testResultReport XML file with test reports
     */
    void postTestResult(File testResultReport);

    /**
     * This method should be called when client is not needed. It performs logout and releases all system resources if it is necessary.
     */
    void release();
}
