// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.tests;

import java.util.Date;

public class TestResultPushStatus {

    private String status;
    private Date until;

    public TestResultPushStatus(String status, Date until) {
        this.until = until;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Date getUntil() {
        return until;
    }
}
