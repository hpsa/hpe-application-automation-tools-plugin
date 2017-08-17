package com.hpe.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 07/07/2016.
 */
public class WholeRunResult extends GoalResult {

    private double _actualValue;
    private double _goalValue;

    /**
     * Instantiates a new Whole run result.
     *
     * @param _actualValue the actual value
     * @param _goalValue   the goal value
     */
    public WholeRunResult(double _actualValue, double _goalValue) {
        this._actualValue = _actualValue;
        this._goalValue = _goalValue;
    }

    /**
     * Instantiates an empty new Whole run result.
     */
    public WholeRunResult() {
        this(0,0);
    }

    /**
     * Gets actual value.
     *
     * @return the actual value
     */
    public double getActualValue() {
        return _actualValue;
    }

    /**
     * Sets actual value.
     *
     * @param actualValue the actual value
     */
    public void setActualValue(double actualValue) {
        this._actualValue = actualValue;
    }

    /**
     * Gets goal value.
     *
     * @return the goal value
     */
    public double getGoalValue() {
        return _goalValue;
    }

    /**
     * Sets goal value.
     *
     * @param goalValue the goal value
     */
    public void setGoalValue(double goalValue) {
        this._goalValue = goalValue;
    }

}
