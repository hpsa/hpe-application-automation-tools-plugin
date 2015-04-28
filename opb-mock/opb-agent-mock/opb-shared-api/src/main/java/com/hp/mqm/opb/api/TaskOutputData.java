package com.hp.mqm.opb.api;

import java.io.Serializable;

/**
 * This class represents output data of a task.
 *
 * User: ginni
 * Date: 4/15/15
 */
public interface TaskOutputData extends Serializable {

    /**
     * Get the task output data as byte array.
     *
     * @return The data as byte array.
     */
    public byte[] getData();

}
