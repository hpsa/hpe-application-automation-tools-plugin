package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 11/07/2016.
 */
public class TimeRange {
        public TimeRange(double _actualValue, double _goalValue, LrTest.SLA_STATUS slaStatus, int loadAmount, double startTime, double endTime) {
            this._actualValue = _actualValue;
            this._goalValue = _goalValue;
            this.slaStatus = slaStatus;
            this.loadAmount = loadAmount;
            this.startTime = startTime;
            this.endTime = endTime;
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

        LrTest.SLA_STATUS slaStatus = LrTest.SLA_STATUS.bad;

        public int getLoadAmount() {
            return loadAmount;
        }

        public void setLoadAmount(int loadAmount) {
            this.loadAmount = loadAmount;
        }

        private int loadAmount = 0;


        public double getStartTime() {
            return startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public double getEndTime() {
            return endTime;
        }

        public void setEndTime(double endTime) {
            this.endTime = endTime;
        }

        private double startTime = 0;
        private double endTime = 0;



}
