package com.hp.mqm.client.exception;


/**
 * Exception means some IO error or error in the HTTP protocol during authentication.
 */
public class LoginErrorException extends RequestErrorException {

    public LoginErrorException() {
    }

    public LoginErrorException(String message) {
        super(message);
    }

    public LoginErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginErrorException(Throwable cause) {
        super(cause);
    }
}
