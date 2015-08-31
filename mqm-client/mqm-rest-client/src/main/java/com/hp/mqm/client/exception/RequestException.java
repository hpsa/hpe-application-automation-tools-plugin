package com.hp.mqm.client.exception;

/**
 * Exception is thrown when MQM server returns unexpected response. Which means that intended result was not achieved.
 */
public class RequestException extends RuntimeException {

    private String description;
    private String errorCode;
    private int statusCode;
    private String reason;

    public RequestException() {
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestException(Throwable cause) {
        super(cause);
    }

    public RequestException(String message, String description, String errorCode, int statusCode, String reason, Throwable cause) {
        super(message, cause);
        this.description = description;
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }
}
