package com.hp.mqm.opb.api;

import java.io.Serializable;
import java.util.Map;

/**
 * This interface represents task progress.*
 * User: ginni
 * Date: 4/15/15
 */
public interface TaskProgress extends Serializable, Cloneable {

    /**
     * Get a message describing the task progress.
     *
     * @return String describing the progress.
     */
    public String getDescription();

    /**
     * Get the task progress in percentages.
     *
     * @return Get the task progress in percentages as in between 0-100(rounded).
     */
    public int getPercentage();

    /**
     * Get properties that are connected to the task progress.
     *
     * @return Properties that are connected to the task progress.
     */
    public Map<String, String> getProperties();
}
