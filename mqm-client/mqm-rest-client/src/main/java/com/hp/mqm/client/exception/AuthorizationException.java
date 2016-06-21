package com.hp.mqm.client.exception;

public class AuthorizationException extends LoginException {

	public AuthorizationException() {
	}

	public AuthorizationException(String message) {
		super(message);
	}

	public AuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationException(Throwable cause) {
		super(cause);
	}

}
