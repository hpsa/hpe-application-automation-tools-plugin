package com.hp.octane.plugins.jetbrains.teamcity.tests.model;


public class ResultFields {

    private String framework;
    private String testingTool;
    private String testLevel;

    public ResultFields() {
    }

    public ResultFields(final String framework, final String testingTool, final String testLevel) {
        this.framework = framework;
        this.testingTool = testingTool;
        this.testLevel = testLevel;
    }

    public String getFramework() {
        return framework;
    }

    public String getTestingTool() {
        return testingTool;
    }

    public String getTestLevel() {
        return testLevel;
    }

    public void setFramework(final String framework) {
        this.framework = framework;
    }

    public void setTestLevel(final String testLevel) {
        this.testLevel = testLevel;
    }

    public void setTestingTool(final String testingTool) {
        this.testingTool = testingTool;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ResultFields that = (ResultFields) o;

        if (framework != null ? !framework.equals(that.framework) : that.framework != null) {
            return false;
        }
        if (testingTool != null ? !testingTool.equals(that.testingTool) : that.testingTool != null) {
            return false;
        }
        return !(testLevel != null ? !testLevel.equals(that.testLevel) : that.testLevel != null);

    }

    @Override
    public int hashCode() {
        int result = framework != null ? framework.hashCode() : 0;
        result = 31 * result + (testingTool != null ? testingTool.hashCode() : 0);
        result = 31 * result + (testLevel != null ? testLevel.hashCode() : 0);
        return result;
    }
}
