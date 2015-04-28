package com.hp.mqm.opb.service;

public class FailedResult {
    private String errorDesc;
    private String errorDetails;
    public FailedResult(String errorDesc, String errorDetails) {
        super();
        this.errorDesc = errorDesc;
        this.errorDetails = errorDetails;
    }
    public String getErrorDesc() {
        return errorDesc;
    }
    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }
    public String getErrorDetails() {
        return errorDetails;
    }
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
}
