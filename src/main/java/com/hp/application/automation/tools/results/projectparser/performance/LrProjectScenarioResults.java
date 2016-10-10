package com.hp.application.automation.tools.results.projectparser.performance;

import hudson.scheduler.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Data model for a project / build / pipeline that contains multiple runs per scenario being run.
 */
public class LrProjectScenarioResults extends LrScenario {
    public HashMap<Integer, WholeRunResult> averageThroughputResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, WholeRunResult> totalThroughtputResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, WholeRunResult> averageHitsPerSecondResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, WholeRunResult> totalHitsResults = new HashMap<Integer, WholeRunResult>(0);

    public HashMap<Integer, TimeRangeResult> errPerSecResults = new HashMap<Integer, TimeRangeResult>(0);
    public HashMap<Integer, HashMap<String, PercentileTransactionWholeRun>> percentileTransactionResults = new HashMap<Integer, HashMap<String, PercentileTransactionWholeRun>>(0);
    public HashMap<Integer, HashMap<String, AvgTransactionResponseTime>> avgTransactionResponseTimeResults = new HashMap<Integer, HashMap<String, AvgTransactionResponseTime>>(0);
    public HashSet<String> transactions = new HashSet<String>(0);

    public LrProjectScenarioResults(String scenarioName) {
        this.setScenrioName(scenarioName);
    }

}
