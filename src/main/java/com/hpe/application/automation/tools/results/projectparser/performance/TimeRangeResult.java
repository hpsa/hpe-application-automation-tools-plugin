package com.hpe.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazaky on 07/07/2016.
 */
public class TimeRangeResult extends GoalResult implements LrTest {


    /**
     * The Time ranges.
     */
    private ArrayList<TimeRange> timeRanges;
    private double _avgActualValue;
    private double _actualValueSum;
    private double _goalValue;
    private String LoadThrashold;
    /**
     * Instantiates a new Time range result.
     */
    public TimeRangeResult() {
        _actualValueSum = 0;
        _goalValue = 0;
        _avgActualValue = 0;
        timeRanges = new ArrayList<TimeRange>(0);
    }

    public List<TimeRange> getTimeRanges() {
        return timeRanges;
    }

    /**
     * Gets actual value avg.
     *
     * @return the actual value avg
     */
    public double getActualValueAvg() {
        _avgActualValue = _actualValueSum / timeRanges.size();
        return _avgActualValue;
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

    /**
     * Inc actual value.
     *
     * @param actualValue the actual value
     */
    public void incActualValue(double actualValue) {
        this._actualValueSum += actualValue;
    }

    /**
     * Gets load thrashold.
     *
     * @return the load thrashold
     */
    public String getLoadThrashold() {
        return LoadThrashold;
    }

    /**
     * Sets load thrashold.
     *
     * @param loadThrashold the load thrashold
     * @return the load thrashold
     */
    public TimeRangeResult setLoadThrashold(String loadThrashold) {
        LoadThrashold = loadThrashold;
        return this;
    }

}
