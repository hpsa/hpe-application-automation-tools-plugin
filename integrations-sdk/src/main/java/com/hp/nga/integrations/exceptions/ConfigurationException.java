package com.hp.nga.integrations.exceptions;

/**
 * Created by kashbi on 29/02/2016.
 */
public class ConfigurationException extends RuntimeException{
    private int errorCode;

    public ConfigurationException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
