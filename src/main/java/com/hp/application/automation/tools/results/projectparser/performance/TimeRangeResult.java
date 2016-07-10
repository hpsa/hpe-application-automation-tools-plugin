package com.hp.application.automation.tools.results.projectparser.performance;

import hudson.tasks.test.TabulatedResult;

import java.util.ArrayList;

/**
 * Created by kazaky on 07/07/2016.
 */
public class TimeRangeResult extends GoalResult implements LrTest {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected String name = "";

    public double getActualValueAvg() {

        //TODO: calculate avg and adda aflag to graph

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

    private double _actualValue;
    private double _goalValue;

    public ArrayList<TimeRange> timeRanges = new ArrayList<TimeRange>(0);

    public class TimeRange
    {
        public TimeRange(double _actualValue, double _goalValue, SLA_STATUS slaStatus, int loadAmount, double startTime, double endTime) {
            this._actualValue = _actualValue;
            this._goalValue = _goalValue;
            this.slaStatus = slaStatus;
            this.loadAmount = loadAmount;
            StartTime = startTime;
            EndTime = endTime;
        }

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

        private double _actualValue;
        private double _goalValue;

        SLA_STATUS slaStatus = SLA_STATUS.bad;

        public int getLoadAmount() {
            return loadAmount;
        }

        public void setLoadAmount(int loadAmount) {
            this.loadAmount = loadAmount;
        }

        private int loadAmount = 0;


        public double getStartTime() {
            return StartTime;
        }

        public void setStartTime(double startTime) {
            StartTime = startTime;
        }

        public double getEndTime() {
            return EndTime;
        }

        public void setEndTime(double endTime) {
            EndTime = endTime;
        }

        private double StartTime = 0;
        private double EndTime = 0;


    }

}
