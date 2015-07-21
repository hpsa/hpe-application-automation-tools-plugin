package com.hp.mqm.client.exception;

public class SharedSpaceNotExistException extends RequestException {

    public SharedSpaceNotExistException() {
    }

    public SharedSpaceNotExistException(String message) {
        super(message);
    }

    public SharedSpaceNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public SharedSpaceNotExistException(Throwable cause) {
        super(cause);
    }

}
