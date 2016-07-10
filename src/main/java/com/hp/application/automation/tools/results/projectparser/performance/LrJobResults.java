package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;

import static com.hp.application.automation.tools.results.projectparser.performance.LrTest.SLA_STATUS.Failed;

/**
 * Created by kazaky on 07/07/2016.
 */
public class LrJobResults  implements LrTest{

    public LrJobResults(){
    }

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

    public void updateStatus(SLA_STATUS slaStatus)
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

    private int _totalNoData = 0;
    private int _totalFailures = 0;
    private int _totalErrors = 0;
    private int _totalPassed = 0;

    public int get_testCount() {
        return _testCount;
    }

    private int _testCount = 0;

    public double get_time() {
        return _time;
    }

    public void set_time(double time) {
        this._time = time;
    }

    private double _time = 0;

    public ArrayList<LrScenarioResult> getLrScenarioResults() {
        return _scenarioResults;
    }

    public boolean addScenrio(LrScenarioResult scenario)
    {

        if( _scenarioResults.add(scenario))
        {
            _totalErrors += scenario.get_totalErrors();
            _totalFailures += scenario.get_totalFailures();
            _time += get_time();
        }
        return false;
    }

    private ArrayList<LrScenarioResult> _scenarioResults =  new ArrayList<LrScenarioResult>(0);

}
