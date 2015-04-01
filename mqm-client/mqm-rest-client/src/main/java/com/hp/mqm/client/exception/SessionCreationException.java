package com.hp.mqm.client.exception;

public class SessionCreationException extends LoginException {

    public SessionCreationException() {
    }

    public SessionCreationException(String message) {
        super(message);
    }

    public SessionCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionCreationException(Throwable cause) {
        super(cause);
    }
}
