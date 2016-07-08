package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;

/**
 * Created by kazaky on 07/07/2016.
 */
public class LrScenarioResult extends LrJobResults{

    public LrScenarioResult() {
    }

    public String get_scenrioName() {
        return _scenrioName;
    }

    public void setScenrio(String scenrioName)
    {
        _scenrioName = scenrioName;
    }

    private String _scenrioName = "";

    public ArrayList<WholeRunResult> wholeRunResults = new ArrayList<WholeRunResult>(0);
    public ArrayList<TransactionTimeRange> transactionTimeRanges = new ArrayList<TransactionTimeRange>(0);


}
