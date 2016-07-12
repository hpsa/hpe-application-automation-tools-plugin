package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.HashMap;

/**
 * Created by kazaky on 12/07/2016.
 */
public class ProjectLrResults extends LrRunResults {

    public HashMap<String, LrProjectScenarioResults> getLrScenarioResults() {
        return _scenarioResults;
    } //TODO:

    public HashMap<String, LrProjectScenarioResults> _scenarioResults = new HashMap<String, LrProjectScenarioResults>();

    public LrProjectScenarioResults addScenrio(LrProjectScenarioResults scenario)
    {
        LrProjectScenarioResults jobLrScenarioResult = null;
        if((jobLrScenarioResult = _scenarioResults.put(scenario.getScenarioName(),scenario)) != null)
        {
            _totalErrors += scenario.get_totalErrors();
            _totalFailures += scenario.get_totalFailures();
            _time += get_time();
        }
        return jobLrScenarioResult;
    }
}
