package com.hp.mqm.client.exception;

/**
 * Exception is thrown when MQM server returns unexpected status code. Which means that intended result was not achieved.
 */
public class RequestException extends RuntimeException {

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
}
