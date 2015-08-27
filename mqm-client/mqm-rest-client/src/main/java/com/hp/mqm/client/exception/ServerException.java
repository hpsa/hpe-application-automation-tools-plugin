// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.exception;

/**
 * Exception used as delimiter when reconstructing server exception as part of the client exception cause hierarchy in
 * order to clearly differentiate the two parts.
 */
public class ServerException extends RuntimeException {

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
