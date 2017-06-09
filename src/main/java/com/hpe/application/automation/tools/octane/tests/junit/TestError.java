/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests.junit;

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
