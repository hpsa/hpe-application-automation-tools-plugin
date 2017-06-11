package com.hpe.application.automation.tools.sse.sdk;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * This is a naive implementation of an HTTP response. We use it to simplify matters in the
 * examples. It is nothing more than a container of the response headers and the response body.
 */
public class Response {
    
    private Map<String, List<String>> _headers;
    private byte[] _data;
    private Throwable _failure;
    private int _statusCode = -1;
    
    public Response() {
        
        this(null, null, null, -1);
    }
    
    public Response(Exception failure) {
        
        this(null, null, failure, -1);
    }
    
    public Response(
            Map<String, List<String>> headers,
            byte[] data,
            Exception failure,
            int statusCode) {
        
        _headers = headers;
        _data = data;
        _failure = failure;
        _statusCode = statusCode;
    }
    
    public Map<String, List<String>> getHeaders() {
        
        return _headers;
    }
    
    public void setHeaders(Map<String, List<String>> responseHeaders) {
        
        _headers = responseHeaders;
    }
    
    public byte[] getData() {
        
        return _data;
    }
    
    public void setData(byte[] data) {
        
        _data = data;
    }
    
    /**
     * @return the failure if the access to the requested URL failed, such as a 404 or 500. If no
     *         such failure occurred this method returns null.
     */
    public Throwable getFailure() {
        
        return _failure;
    }
    
    public void setFailure(Throwable cause) {
        
        this._failure = cause;
    }
    
    public int getStatusCode() {
        
        return _statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        
        _statusCode = statusCode;
    }
    
    public boolean isOk() {
        
        return getFailure() == null
               && (getStatusCode() == HttpURLConnection.HTTP_OK
                   || getStatusCode() == HttpURLConnection.HTTP_CREATED || getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
    }
    
    /**
     * @see Object#toString() return the contents of the byte[] data as a string.
     */
    @Override
    public String toString() {
        
        return new String(_data);
    }
}
