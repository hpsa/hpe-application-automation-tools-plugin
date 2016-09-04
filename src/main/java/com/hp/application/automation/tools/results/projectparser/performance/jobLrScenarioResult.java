package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;

/**
 * Created by kazaky on 07/07/2016.
 */
public class jobLrScenarioResult extends LrScenario {

    public jobLrScenarioResult() {
    }

    public jobLrScenarioResult(String scenarioName) {
        this.setScenrioName(scenarioName);
    }



    public WholeRunResult averageThroughputResults = new WholeRunResult();
    public WholeRunResult totalThroughtputResutls = new WholeRunResult();
    public WholeRunResult averageHitsPerSecondResults = new WholeRunResult();
    public WholeRunResult totalHitsResults = new WholeRunResult();
    public TimeRangeResult errPerSecResults = new TimeRangeResult();
    public PercentileTransactionWholeRun percentileTransactionResults = new PercentileTransactionWholeRun();
    public ArrayList<TransactionTimeRange> transactionTimeRanges = new ArrayList<TransactionTimeRange>();



}
