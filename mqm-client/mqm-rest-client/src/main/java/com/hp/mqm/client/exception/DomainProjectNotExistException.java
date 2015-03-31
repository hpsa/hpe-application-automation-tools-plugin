package com.hp.mqm.client.exception;

public class DomainProjectNotExistException extends RequestException {

    public DomainProjectNotExistException() {
    }

    public DomainProjectNotExistException(String message) {
        super(message);
    }

    public DomainProjectNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomainProjectNotExistException(Throwable cause) {
        super(cause);
    }

}
