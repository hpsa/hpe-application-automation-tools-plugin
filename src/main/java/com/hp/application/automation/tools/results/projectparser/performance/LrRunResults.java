package com.hp.application.automation.tools.results.projectparser.performance;

/**
 * Created by kazaky on 12/07/2016.
 */
public class LrRunResults {
    protected int _totalFailures = 0;
    protected int _totalErrors = 0;
    protected double _time = 0;
    private int _totalNoData = 0;
    private int _totalPassed = 0;
    private int _testCount = 0;

    public int get_totalFailures() {
        return _totalFailures;
    }

    public void set_totalFailures(int totalFailures) {
        this._totalFailures = totalFailures;
    }

    public void incTotalFailures()
    {
        _totalFailures++;
    }

    public void incTotalErrors()
    {
        _totalErrors++;
    }

    public void incTotalNoData()
    {
        _totalNoData++;
    }

    public void incTotalPassed()
    {
        _totalPassed++;
    }

    public void incTotalTests()
    {
        _testCount++;
    }

    public int get_totalErrors() {
        return _totalErrors;
    }

    public void set_totalErrors(int totalErrors) {
        this._totalErrors = totalErrors;
    }

    public void updateStatus(LrTest.SLA_STATUS slaStatus)
    {
        switch(slaStatus)
        {
            case Failed:
                incTotalFailures();
                incTotalTests();
                break;
            case Passed:
                incTotalPassed();
                incTotalTests();
                break;
            case NoData:

                break;
            case bad:
                break;
        }
    }

    public int get_totalNoData() {
        return _totalNoData;
    }

    public int get_testCount() {
        return _testCount;
    }

    public double get_time() {
        return _time;
    }

    public void set_time(double time) {
        this._time = time;
    }
}
