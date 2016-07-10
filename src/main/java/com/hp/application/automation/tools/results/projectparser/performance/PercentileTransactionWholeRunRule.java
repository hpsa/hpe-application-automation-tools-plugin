package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 10/07/2016.
 */
public class PercentileTransactionWholeRunRule extends WholeRunResult {

    public PercentileTransactionWholeRunRule() {
    }

    public double getPrecentage() {
        return _precentage;
    }

    public void setPrecentage(double precentage) {
        this._precentage = precentage;
    }

    double _precentage;


}
