package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;

/**
 * Created by kazaky on 07/07/2016.
 */
public class TimeRangeResult extends GoalResult implements LrTest {



    public double getActualValueAvg() {

        return (_avgActualValue =  (double) _actualValueSum / timeRanges.size());
    }

    public double getGoalValue() {
         return _goalValue;
    }

    public void setGoalValue(double goalValue) {
        this._goalValue = goalValue;
    }

    private double _avgActualValue = 0;

    public void incActualValue(double actualValue) {
        this._actualValueSum += _actualValueSum;
    }

    private double _actualValueSum = 0;
    private double _goalValue = 0;

    public String getLoadThrashold() {
        return LoadThrashold;
    }

    public TimeRangeResult setLoadThrashold(String loadThrashold) {
        LoadThrashold = loadThrashold;
        return this;
    }

    private String LoadThrashold;

    public ArrayList<TimeRange> timeRanges = new ArrayList<TimeRange>(0);

}
