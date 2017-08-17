package com.hpe.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 07/07/2016.
 */
public class AvgTransactionResponseTime extends TimeRangeResult {

    public AvgTransactionResponseTime() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected String name = "";
}
