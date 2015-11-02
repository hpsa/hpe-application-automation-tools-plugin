package com.hp.mqm.clt;

public enum ReturnCode {

    SUCCESS(0),
    FAILURE(1);

    private int returnCode;

    ReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getReturnCode() {
        return returnCode;
    }
}