package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kazaky on 12/07/2016.
 */
public class LrProjectScenarioResults extends LrScenario {
    public Map<Integer, WholeRunResult> averageThroughputResults = new HashMap<Integer, WholeRunResult>();
    public Map<Integer, WholeRunResult> totalThroughtputResutls = new HashMap<Integer, WholeRunResult>();
    public Map<Integer, WholeRunResult> averageHitsPerSecondResults = new HashMap<Integer, WholeRunResult>();
    public Map<Integer, WholeRunResult> totalHitsResults = new HashMap<Integer, WholeRunResult>();

    public Map<Integer, TimeRangeResult> errPerSecResults = new HashMap<Integer, TimeRangeResult>();
    public Map<Integer, ArrayList<PercentileTransactionWholeRun>> percentileTransactionResultsProject = new HashMap<Integer, ArrayList<PercentileTransactionWholeRun>>();
    public Map<Integer, ArrayList<TransactionTimeRange>> transactionTimeRangesProject = new HashMap<Integer, ArrayList<TransactionTimeRange>>();

    public LrProjectScenarioResults(String scenarioName) {
        this.setScenrioName(scenarioName);
    }

    public LrProjectScenarioResults() {
    }
}
