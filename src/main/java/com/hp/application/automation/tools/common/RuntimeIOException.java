package com.hp.application.automation.tools.common;

/**
 * Created by berkovir on 08/05/2017.
 */
public class RuntimeIOException extends RuntimeException {

    public RuntimeIOException() {
        super();
    }

    public RuntimeIOException(String msg) {
        super(msg);
    }

    public RuntimeIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
