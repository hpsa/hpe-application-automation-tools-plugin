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
//    public WholeRunResult averageThroughputResults = null;
//    public WholeRunResult totalThroughputResults = null;
//    public WholeRunResult averageHitsPerSecondResults = null;
//    public WholeRunResult totalHitsResults = null;
//    public ArrayList<PercentileTransactionWholeRun> percentileTransactionResults = new ArrayList<PercentileTransactionWholeRun>(0);
//    public ArrayList<AvgTransactionResponseTime> transactionTimeRanges= new ArrayList<AvgTransactionResponseTime>(0);
//    public TimeRangeResult errPerSecResults = null;
}
