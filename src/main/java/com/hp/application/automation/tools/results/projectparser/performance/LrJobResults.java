package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.HashMap;

/**
 * Created by kazaky on 07/07/2016.
 */
public class LrJobResults extends LrRunResults implements LrTest{

    public LrJobResults(){
    }


    public HashMap<String, jobLrScenarioResult> getLrScenarioResults() {
        return _scenarioResults;
    } //TODO:

    public HashMap<String, jobLrScenarioResult> _scenarioResults = new HashMap<String, jobLrScenarioResult>();

    public jobLrScenarioResult addScenrio(jobLrScenarioResult scenario)
    {
        jobLrScenarioResult jobLrScenarioResult = null;
         if((jobLrScenarioResult = _scenarioResults.put(scenario.getScenarioName(),scenario)) != null)
         {
             _totalErrors += scenario.get_totalErrors();
             _totalFailures += scenario.get_totalFailures();
             _time += get_time();
         }
        return jobLrScenarioResult;
    }


//    private ArrayList<jobLrScenarioResult> _scenarioResults =  new ArrayList<jobLrScenarioResult>(0);



}
