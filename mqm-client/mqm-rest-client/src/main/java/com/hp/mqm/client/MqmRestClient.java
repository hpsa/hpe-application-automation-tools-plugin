package com.hp.mqm.client;

import java.io.File;

/**
 * Client for connection to MQM public API. It wraps whole http communication with MQM server. Client handles login automatically but
 * when client is not intended to use anymore, method {@link #release()} must be called. Method {@link #release()} should be invoked also
 * when client is not intended to use for a long time.
 *
 * <p>
 * All methods can throw {@link com.hp.mqm.client.exception.RequestException} when unexpected result is returned from
 * MQM server and {@link com.hp.mqm.client.exception.RequestErrorException} in case of IO error or error in the HTTP protocol.
 * <p/>
 *
 * <p>
 * Because client cares about login automatically all methods (except {@link #release()}) can
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
     * InputStream obtained from InputStreamSource is automatically closed after all data are read.
     * @param inputStreamSource input stream source with test results in MQM XML format.
     */
    void postTestResult(InputStreamSource inputStreamSource);

    /**
     * Posts test results to MQM. Divide extra large test results into smaller files which will be posted individually
     * (multiple invocation of this method) to avoid HTTP request timeout.
     * @param  testResultReport XML file with test reports
     * @throws com.hp.mqm.client.exception.FileNotFoundException
     */
    void postTestResult(File testResultReport);

    /**
     * This method should be called when client is not needed or it should not be used for a long time. It performs
     * logout and releases all system resources if it is necessary. After invocation of {@link #release()} you can still
     * invoke any client method (but client will need to do authentication, create session, etc.).
     */
    void release();
}
