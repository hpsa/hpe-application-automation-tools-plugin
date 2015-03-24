package com.hp.mqm.client.exception;


/**
 * Exception means some IO error or error in the HTTP protocol during authentication.
 */
public class AuthenticationErrorException extends RequestErrorException {

    public AuthenticationErrorException() {
    }

    public AuthenticationErrorException(String message) {
        super(message);
    }

    public AuthenticationErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationErrorException(Throwable cause) {
        super(cause);
    }
}
