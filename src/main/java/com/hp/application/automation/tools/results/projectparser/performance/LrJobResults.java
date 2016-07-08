package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;

/**
 * Created by kazaky on 07/07/2016.
 */
public class LrJobResults  implements LrTest{

    public LrJobResults(){
    }

    public int get_totalFailures() {
        return _totalFailures;
    }

    public void set_totalFailures(int _totalFailures) {
        this._totalFailures = _totalFailures;
    }

    public int get_totalErrors() {
        return _totalErrors;
    }

    public void set_totalErrors(int _totalErrors) {
        this._totalErrors = _totalErrors;
    }

    private int _totalFailures = 0;
    private int _totalErrors = 0;

    public double get_time() {
        return _time;
    }

    public void set_time(double _time) {
        this._time = _time;
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
