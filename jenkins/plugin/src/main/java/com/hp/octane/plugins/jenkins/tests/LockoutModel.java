// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.Extension;
import hudson.util.TimeUnit2;

@Extension
public class LockoutModel { // TODO: janotav: write test

    private static final long[] QUIET_PERIOD = { // TODO: janotav: verify against our Saas policy
            TimeUnit2.MINUTES.toMillis(1),
            TimeUnit2.MINUTES.toMillis(10),
            TimeUnit2.MINUTES.toMillis(60)
    };

    private long boundary;
    private int periodIndex;

    public LockoutModel() {
        success();
    }

    public synchronized boolean isQuietPeriod() {
        return System.currentTimeMillis() < boundary;
    }

    public synchronized void failure() {
        if (periodIndex < QUIET_PERIOD.length - 1) {
            periodIndex++;
        }
        boundary = System.currentTimeMillis() + QUIET_PERIOD[periodIndex];
    }

    public synchronized void success() {
        periodIndex = -1;
        boundary = 0;
    }
}
