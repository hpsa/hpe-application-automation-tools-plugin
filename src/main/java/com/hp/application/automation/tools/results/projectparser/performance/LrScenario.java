package com.hp.application.automation.tools.results.projectparser.performance;

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
}