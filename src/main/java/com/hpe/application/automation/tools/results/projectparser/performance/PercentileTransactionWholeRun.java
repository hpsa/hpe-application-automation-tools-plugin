package com.hpe.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 10/07/2016.
 */
public class PercentileTransactionWholeRun extends WholeRunResult {

    private String name;
    private double _precentage;

    public PercentileTransactionWholeRun() {
        _precentage = 0.0;
        name = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrecentage() {
        return _precentage;
    }

    public void setPrecentage(double precentage) {
        this._precentage = precentage;
    }


}
