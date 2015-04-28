package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.api.TaskProgress;

import java.util.Map;

/**
 * Task progress mock class.
 * User: ginni
 * Date: 4/15/15
 */
public class TaskProgressMockImpl implements TaskProgress {

    /**
     *
     */
    private static final long serialVersionUID = -8529690104792873356L;
    private String description;
    private int percentage;
    private Map<String, String> properties;

    public TaskProgressMockImpl(
            String description,
            int percentage,
            Map<String, String> properties) {
        this.description = description;
        this.percentage = percentage;
        this.properties = properties;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getPercentage() {
        return percentage;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "TaskProgressMockImpl{"
                + "description=" + description
                + ", percentage=" + percentage
                + ", properties=" + properties
                + '}';
    }
}
