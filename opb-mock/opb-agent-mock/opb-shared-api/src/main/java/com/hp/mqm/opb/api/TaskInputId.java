package com.hp.mqm.opb.api;

/**
 * Represents task input identifier.
 *
 * @author avrahame
 */
public interface TaskInputId {

    /**
     * Get the task input identifier.
     *
     * @return The identifier of the data, the identifier is later used in the getData method.
     */
    public String getId();

}
