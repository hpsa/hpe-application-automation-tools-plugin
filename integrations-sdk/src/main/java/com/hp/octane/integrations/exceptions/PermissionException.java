package com.hp.octane.integrations.exceptions;

/**
 * Created by kashbi on 29/02/2016.
 */

public class PermissionException extends RuntimeException {
	private int errorCode;

	public PermissionException(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
