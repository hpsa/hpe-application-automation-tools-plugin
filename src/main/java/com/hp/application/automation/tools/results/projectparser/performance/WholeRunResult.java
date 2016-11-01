package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 07/07/2016.
 */
public class WholeRunResult extends GoalResult {

    private double _actualValue;
    private double _goalValue;

    public double getActualValue() {
        return _actualValue;
    }

    public void setActualValue(double actualValue) {
        this._actualValue = actualValue;
    }

    public double getGoalValue() {
        return _goalValue;
    }

    public void setGoalValue(double goalValue) {
        this._goalValue = goalValue;
    }

}
