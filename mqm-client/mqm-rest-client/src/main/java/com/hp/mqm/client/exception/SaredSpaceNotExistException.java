package com.hp.mqm.client.exception;

public class SaredSpaceNotExistException extends RequestException {

    public SaredSpaceNotExistException() {
    }

    public SaredSpaceNotExistException(String message) {
        super(message);
    }

    public SaredSpaceNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public SaredSpaceNotExistException(Throwable cause) {
        super(cause);
    }

}
