package com.hpe.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 07/07/2016.
 */


public abstract class GoalResult implements LrTest{

    public SLA_STATUS getStatus() {
        return _status;
    }

    public void setStatus(SLA_STATUS _status) {
        this._status = _status;
    }

    public SLA_GOAL getSlaGoal() {
        return _slaGoal;
    }

    public void setSlaGoal(SLA_GOAL _slaGoal) {
        this._slaGoal = _slaGoal;
    }

    public double getDuration() {
        return _duration;
}

    public void setDuration(double _duration) {
        this._duration = _duration;
    }

    private SLA_GOAL _slaGoal;
    private SLA_STATUS _status;

    public String getFullName() {
        return _fullName;
    }

    public void setFullName(String _fullName) {
        this._fullName = _fullName;
    }

    private String _fullName;
    private double _duration;


}
