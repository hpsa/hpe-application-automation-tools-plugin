package com.hp.mqm.atrf.octane.entities;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by berkovir on 23/11/2016.
 */
public class TestRunResultEntity {

    private String runId;

    private String testName;
    private String testingToolType;
    private String packageValue;
    private String classValue;
    private String module;

    private String duration;
    private Map<String, String> environment;
    private String externalReportUrl;
    private String runName;
    private String releaseId;
    private String releaseName;
    private String startedTime;
    private String status;

    public void setRunId(String runId) {
        this.runId = runId;
    }

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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        if (StringUtils.isEmpty(duration)) {
            this.duration = "0";
        } else {
            this.duration = duration;
        }
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
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

    public void validateEntity() {
        if (testName == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run ", "testName", getRunId()));
        }
        if (testingToolType == null) {
            //throw new RuntimeException(String.format("The field %s is empty in the run ", "testingToolType", getRunId()));
        }
        if (packageValue == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run ", "packageValue", getRunId()));
        }
        if (classValue == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run ", "classValue", getRunId()));
        }
        if (module == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run %s", "module", getRunId()));
        }
        if (duration == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run %s", "duration", getRunId()));
        }
        if (externalReportUrl == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run %s", "externalReportUrl", getRunId()));
        }
        if (runName == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run %s", "runName", getRunId()));
        }
        if (startedTime == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run %s", "startedTime", getRunId()));
        }
        if (status == null) {
            throw new RuntimeException(String.format("The field %s is empty in the run %s", "", getRunId()));
        }
    }


    public String getRunId() {
        return runId;
    }
}
