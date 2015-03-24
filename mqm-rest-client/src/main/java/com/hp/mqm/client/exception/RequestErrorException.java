package com.hp.mqm.client.exception;

/**
 * Exception means some IO error or error in the HTTP protocol.
 */
public class RequestErrorException extends RuntimeException {

    public RequestErrorException() {
    }

    public RequestErrorException(String message) {
        super(message);
    }

    public RequestErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestErrorException(Throwable cause) {
        super(cause);
    }
}
