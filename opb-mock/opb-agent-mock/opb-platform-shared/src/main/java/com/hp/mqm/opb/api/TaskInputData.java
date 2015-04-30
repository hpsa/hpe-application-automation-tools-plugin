package com.hp.mqm.opb.api;

import java.io.Serializable;

/**
 * This class represents input data of a task.
 *
 * @author avrahame
 */
public interface TaskInputData extends Serializable {

    /**
     * Get the task input data as byte array.
     *
     * @return The data as byte array.
     */
    public byte[] getData();

}
