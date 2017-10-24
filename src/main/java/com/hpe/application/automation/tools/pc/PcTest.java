package com.hpe.application.automation.tools.pc;

/**
 * Created by bemh on 6/5/2017.
 * Partial implementation of the test xml structure
 */
public class PcTest {


    private int testId;
    private String testName;

    private int trendReportId = -1;



    public int getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTrendReportId(int trendReportId) {
        this.trendReportId = trendReportId;
    }

    public int getTrendReportId() {
        return trendReportId;
    }

    public void setTestId(int testId){this.testId = testId;}

    public void setTestName(String testName){this.testName = testName;}
}
