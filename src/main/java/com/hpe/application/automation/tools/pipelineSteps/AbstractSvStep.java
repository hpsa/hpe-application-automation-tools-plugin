package com.hpe.application.automation.tools.pipelineSteps;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;

public abstract class AbstractSvStep extends AbstractStepImpl {
    protected final boolean force;
    protected final String serverName;

    public AbstractSvStep(String serverName, boolean force) {
        this.serverName = serverName;
        this.force = force;
    }

    protected abstract SimpleBuildStep getBuilder();

    public String getServerName() {
        return serverName;
    }

    public boolean isForce() {
        return force;
    }
}
