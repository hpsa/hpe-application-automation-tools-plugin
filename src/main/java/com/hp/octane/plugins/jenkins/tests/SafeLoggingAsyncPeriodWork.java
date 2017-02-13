// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public abstract class SafeLoggingAsyncPeriodWork extends AsyncPeriodicWork {

    private static Logger logger = Logger.getLogger(SafeLoggingAsyncPeriodWork.class.getName());

    protected SafeLoggingAsyncPeriodWork(String name) {
        super(name);
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        try {
            doExecute(listener);
        } catch (IOException e) {
            // by default this ends up in log file and is rewritten on each execution
            // we want this in the regular Jenkins log in order to be able to troubleshoot
            logError(e);
        } catch (InterruptedException e) {
            // the same as IOException
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
}