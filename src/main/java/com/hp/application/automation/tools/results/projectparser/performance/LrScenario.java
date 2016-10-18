package com.hp.application.automation.tools.results.projectparser.performance;

import hudson.scheduler.Hash;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kazaky on 12/07/2016.
 */
public abstract class LrScenario extends LrJobResults{
    public String getScenarioName() {
        return _scenrioName;
    }


    private String _scenrioName = "";

    void setScenrioName(String scenrioName)
    {
        _scenrioName = scenrioName;
    };

    public Map<String, Integer> vUser = new HashMap<String, Integer>(0);
    public Map<String, Integer> transactionSum = new HashMap<String, Integer>(0);
    public Map<String, HashMap<String, Integer>> transactionData = new HashMap<String, HashMap<String, Integer>>(0);
    int connectionMax = 0;

    public int getConnectionMax() {
        return connectionMax;
    }

    public void setConnectionMax(int connectionMax) {
        this.connectionMax = connectionMax;
    }
}