/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests.detection;

/**
 * Class describing metadata of executed tests for test pushing to Octane
 */
public class ResultFields {

    private String framework;
    private String testingTool;
    private String testLevel;
    private String testType;

    public ResultFields() {
    }

    public ResultFields(final String framework, final String testingTool, final String testLevel) {
        this(framework, testingTool, testLevel, null);
    }

    public ResultFields(final String framework, final String testingTool, final String testLevel, final String testType) {
        this.framework = framework;
        this.testingTool = testingTool;
        this.testLevel = testLevel;
        this.testType = testType;
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

        if (testType != null ? !testType.equals(that.testType) : that.testType != null) {
            return false;
        }
        return !(testLevel != null ? !testLevel.equals(that.testLevel) : that.testLevel != null);

    }

    @Override
    public int hashCode() {
        int result = framework != null ? framework.hashCode() : 0;
        result = 31 * result + (testingTool != null ? testingTool.hashCode() : 0);
        result = 31 * result + (testLevel != null ? testLevel.hashCode() : 0);
        result = 31 * result + (testType != null ? testType.hashCode() : 0);
        return result;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }
}
