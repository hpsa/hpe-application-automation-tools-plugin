package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 10/07/2016.
 */
public class PercentileTransactionWholeRun extends WholeRunResult {

    public PercentileTransactionWholeRun() {
    }

    public double getPrecentage() {
        return _precentage;
    }

    public void setPrecentage(double precentage) {
        this._precentage = precentage;
    }

    double _precentage;


}