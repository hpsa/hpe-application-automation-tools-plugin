package com.hp.octane.plugins.jenkins.tests.junit;

import java.io.Serializable;

/**
 * Created by lev on 14/03/2016.
 */
public final class TestError  implements Serializable {
    private final String stackTraceStr;
    private final String errorType;
    private final String errorMsg;

    public TestError(String stackTraceStr, String errorType, String errorMsg) {
        this.stackTraceStr = stackTraceStr;
        this.errorType = errorType;
        this.errorMsg = errorMsg;
    }

    public String getStackTraceStr() {
        return stackTraceStr;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
