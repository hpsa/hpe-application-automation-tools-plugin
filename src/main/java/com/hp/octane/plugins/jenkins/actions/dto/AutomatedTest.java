package com.hp.octane.plugins.jenkins.actions.dto;

/**
 * Created by kashbi on 25/09/2016.
 */
public class AutomatedTest {
    private String type = "test";
    private String subtype = "test_automated";
    private com.hp.octane.plugins.jenkins.actions.dto.TestingToolType testing_tool_type;
    private TestFramework framework;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public TestingToolType getTesting_tool_type() {
        return testing_tool_type;
    }

    public void setTesting_tool_type(TestingToolType testing_tool_type) {
        this.testing_tool_type = testing_tool_type;
    }

    public TestFramework getFramework() {
        return framework;
    }

    public void setFramework(TestFramework framework) {
        this.framework = framework;
    }
}
