package com.hp.application.automation.tools.octane.executor;

import hudson.model.Cause;

/**
 * Created by berkovir on 01/05/2017.
 */
public class TriggeredBySuiteRunCause extends Cause {

    String suiteRunId;


    public TriggeredBySuiteRunCause(String suiteRunId) {
        this.suiteRunId = suiteRunId;
    }

    public static TriggeredBySuiteRunCause create(String suiteRunId){
        return new TriggeredBySuiteRunCause(suiteRunId);
    }

    @Override
    public String getShortDescription() {
        return "Triggered by octane suite run " + suiteRunId;
    }
}
