/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.pc;

public enum RunState{

    // The order in this enum is important, changing it could harm other code.
    // please see functions on PCClient waitForRunCompletion and waitForRunState.
    UNDEFINED(""),
    INITIALIZING("Initializing"),
    RUNNING("Running"),
    STOPPING("Stopping"),
    BEFORE_COLLATING_RESULTS("Before Collating Results"),
    COLLATING_RESULTS("Collating Results"),
    BEFORE_CREATING_ANALYSIS_DATA("Before Creating Analysis Data"),
    PENDING_CREATING_ANALYSIS_DATA("Pending Creating Analysis Data"),
    CREATING_ANALYSIS_DATA("Creating Analysis Data"),
    FINISHED("Finished"),
    FAILED_COLLATING_RESULTS("Failed Collating Results"),
    FAILED_CREATING_ANALYSIS_DATA("Failed Creating Analysis Data"),
    CANCELED("Canceled"),
    RUN_FAILURE("Run Failure");

    
    private String value;
    
    private RunState(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    public boolean hasFailure () {
        return value.toLowerCase().contains("fail");
    }
    
    public static RunState get(String val){
        for (RunState state : RunState.values()) {
            if (val.equals(state.value()))
                    return state;
        }
        return UNDEFINED;
    }
}
