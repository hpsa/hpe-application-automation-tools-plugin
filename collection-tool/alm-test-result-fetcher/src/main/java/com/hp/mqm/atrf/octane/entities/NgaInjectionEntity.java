package com.hp.mqm.atrf.octane.entities;

import java.util.Map;

/**
 * Created by berkovir on 23/11/2016.
 */
public class NgaInjectionEntity {

    private String testName;//test name + test configuration, if Test name =Test configuration, just keep test name
    private String testingToolType;//test type
    private String packageValue;//project
    private String classValue;//test path (not equal to test URL)
    private String component;//domain

    private String duration;//run duration
    private Map<String,String> environment;//testSet name
    private String externalReportUrl;//run url to alm
    private String runName;//alm Run ID\ alm Run Name
    private String releaseId;
    private String releaseName;
    private String startedTime;
    private String status;
    private String runBy;
    private String draftRun;


    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestingToolType() {
        return testingToolType;
    }

    public void setTestingToolType(String testingToolType) {
        this.testingToolType = testingToolType;
    }

    public String getPackageValue() {
        return packageValue;
    }

    public void setPackageValue(String packageValue) {
        this.packageValue = packageValue;
    }

    public String getClassValue() {
        return classValue;
    }

    public void setClassValue(String classValue) {
        this.classValue = classValue;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Map<String,String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String,String> environment) {
        this.environment = environment;
    }

    public String getExternalReportUrl() {
        return externalReportUrl;
    }

    public void setExternalReportUrl(String externalReportUrl) {
        this.externalReportUrl = externalReportUrl;
    }

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(String startedTime) {
        this.startedTime = startedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRunBy() {
        return runBy;
    }

    public void setRunBy(String runBy) {
        this.runBy = runBy;
    }

    public String getDraftRun() {
        return draftRun;
    }

    public void setDraftRun(String draftRun) {
        this.draftRun = draftRun;
    }
}
