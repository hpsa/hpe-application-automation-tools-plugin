package com.hp.mqm.opb.api;

/**
 * The exception is thrown when a task is cancelled but its task executor still send requests to server to get data, send data
 * or report progress. In this case, the task executor should catch {@link TaskCancelledException}
 * and finishes the task.
 *
 */
public class TaskCancelledException extends RuntimeException {
    public TaskCancelledException(String message) {
        super(message);
    }

    public TaskCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
