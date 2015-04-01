package com.hp.mqm.client.exception;

/**
 * Exception is thrown when authentication failed.
 */
public class LoginException extends RequestException {

    public LoginException() {
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(Throwable cause) {
        super(cause);
    }
}
