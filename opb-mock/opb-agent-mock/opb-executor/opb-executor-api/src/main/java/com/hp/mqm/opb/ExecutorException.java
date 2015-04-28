package com.hp.mqm.opb;

/**
 * Created with IntelliJ IDEA. User: borshtei Date: 07/05/13 Time: 11:23
 */
public class ExecutorException extends RuntimeException {
	private static final long serialVersionUID = -8649727766950353413L;

    public ExecutorException(String message) {
		super(message);
	}

    public ExecutorException(String message, Throwable cause) {
		super(message, cause);
	}
}
