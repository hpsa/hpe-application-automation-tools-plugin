// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.client;

public class TestEventPublisher implements EventPublisher {

    private boolean suspended;
    private int resumeCount;

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public void resume() {
        ++resumeCount;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public int getResumeCount() {
        return resumeCount;
    }
}
