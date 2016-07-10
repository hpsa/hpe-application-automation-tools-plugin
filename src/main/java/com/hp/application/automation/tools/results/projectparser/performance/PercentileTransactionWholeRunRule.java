package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 10/07/2016.
 */
public class PercentileTransactionWholeRunRule extends WholeRunResult {

    public PercentileTransactionWholeRunRule(double precentage) {
        this.precentage = precentage;
    }

    public double getPrecentage() {
        return precentage;
    }

    public void setPrecentage(double precentage) {
        this.precentage = precentage;
    }

    double precentage;


}
