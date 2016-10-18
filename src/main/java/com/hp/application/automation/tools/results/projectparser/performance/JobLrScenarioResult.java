package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;


/**
 * Holds information on the SLA's of one scenario (per job / run / build)
 */
public class JobLrScenarioResult extends LrScenario {
    public JobLrScenarioResult(String scenarioName) {
        this.setScenrioName(scenarioName);
    }
    public ArrayList<GoalResult> scenarioSlaResults = new ArrayList<GoalResult>(0);
}
