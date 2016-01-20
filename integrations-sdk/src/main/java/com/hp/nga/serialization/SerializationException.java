package com.hp.nga.serialization;

/**
 * Created by gullery on 30/12/2015.
 *
 * Custom generic exception that is the only one exposed in public APIs
 */

public class SerializationException extends RuntimeException {
	public SerializationException(Throwable cause) {
		super(cause);
	}
}
