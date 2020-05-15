/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
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
 */

package com.microfocus.application.automation.tools.commonResultUpload;

import com.microfocus.application.automation.tools.sse.sdk.Logger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CommonUploadLogger implements Logger {

    private static final String ERR_PREFIX = "ERR: ";
    private static final String INFO_PREFIX = "INFO: ";
    private static final String WARN_PREFIX = "WARN: ";

    private int errorCount;
    private List<String> failedEntityNames;
    private PrintStream printStream;

    public CommonUploadLogger(PrintStream printStream) {
        this.printStream = printStream;
        failedEntityNames = new ArrayList<>();
    }

    public void error(String message) {
        errorCount++;
        storeError(message);
        message = ERR_PREFIX + message;
        log(message);
    }

    public void info(String message) {
        message = INFO_PREFIX + message;
        log(message);
    }

    public void warn(String message) {
        message = WARN_PREFIX + message;
        log(message);
    }

    @Override
    public void log(String message) {
        if (printStream != null) {
            printStream.println(message);
        }
    }

    public List<String> getFailedEntityNames() {
        return failedEntityNames;
    }

    public int getErrorCount() {
        return errorCount;
    }

    private void storeError(String message) {
        if (message.indexOf("(") > 0 && message.indexOf(")") > message.indexOf("(")) {
            failedEntityNames.add(
                    message.substring(message.indexOf("(") + 1, message.indexOf(")"))
            );
        }
    }
}
