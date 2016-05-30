package com.hpe.application.automation.tools.common.integration;

/**
 * Created with IntelliJ IDEA.
 * User: yanghanx
 * Date: 5/20/16
 * Time: 1:46 PM
 */
public class HttpConnectionException extends Exception {

    private String errorMsg;

    public HttpConnectionException(){}

    public HttpConnectionException(String msg) {
        super(msg);
        errorMsg = msg;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
