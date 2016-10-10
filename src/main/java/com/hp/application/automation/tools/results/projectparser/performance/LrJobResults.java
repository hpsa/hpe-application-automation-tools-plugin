package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.HashMap;

/**
 * Created by kazaky on 07/07/2016.
 */
public class LrJobResults extends LrRunResults implements LrTest{

    public LrJobResults(){
    }


    public HashMap<String, JobLrScenarioResult> getLrScenarioResults() {
        return _scenarioResults;
    } //TODO:

    public HashMap<String, JobLrScenarioResult> _scenarioResults = new HashMap<String, JobLrScenarioResult>();

    public JobLrScenarioResult addScenrio(JobLrScenarioResult scenario)
    {
        JobLrScenarioResult JobLrScenarioResult = null;
         if((JobLrScenarioResult = _scenarioResults.put(scenario.getScenarioName(),scenario)) != null)
         {
             _totalErrors += scenario.get_totalErrors();
             _totalFailures += scenario.get_totalFailures();
             _time += get_time();
         }
        return JobLrScenarioResult;
    }


//    private ArrayList<JobLrScenarioResult> _scenarioResults =  new ArrayList<JobLrScenarioResult>(0);



}
