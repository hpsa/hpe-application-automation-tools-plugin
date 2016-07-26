package com.hp.application.automation.tools.pc;
import com.thoughtworks.xstream.XStream;

public class PcTrendedRun {


    private int RunID;

    private String RunDate;

    private int Duration;

    private String State;

    public void setRunID(int runID){
        this.RunID = runID;
    }

    public void setState(String state){
        this.State = state;
    }

    public int getRunID() {
        return RunID;
    }

    public String getRunDate() {
        return RunDate;
    }

    public int getDuration() {
        return Duration;
    }

    public String getState() {
        return State;
    }

}
