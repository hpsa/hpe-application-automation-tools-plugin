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

package com.hpe.application.automation.tools.octane.tests;

import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public abstract class AbstractSafeLoggingAsyncPeriodWork extends AsyncPeriodicWork {

    private static Logger logger = Logger.getLogger(AbstractSafeLoggingAsyncPeriodWork.class.getName());

    protected AbstractSafeLoggingAsyncPeriodWork(String name) {
        super(name);
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        try {
            doExecute(listener);
        } catch (IOException|InterruptedException e) {
            // by default this ends up in log file and is rewritten on each execution
            // we want this in the regular Jenkins log in order to be able to troubleshoot
            logError(e);
        } catch (Throwable t) {
            // by default this ends up on the console as uncaught exception
            // we want this in the regular Jenkins log in order to be able to troubleshoot
            logError(t);
        }
    }

    protected abstract void doExecute(TaskListener listener) throws IOException, InterruptedException;

    private void logError(Throwable t) {
        LogRecord lr = new LogRecord(this.getErrorLoggingLevel(), "{0} thread failed with error");
        lr.setThrown(t);
        lr.setParameters(new Object[] { name });
        logger.log(lr);
    }

    @Override
    protected Level getNormalLoggingLevel() {
        return Level.CONFIG;
    }
}
