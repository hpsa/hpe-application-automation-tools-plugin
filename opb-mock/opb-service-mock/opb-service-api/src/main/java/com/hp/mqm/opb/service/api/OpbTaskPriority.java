package com.hp.mqm.opb.service.api;

import com.hp.mqm.opb.service.TaskPriority;

/**
 * Predefined task priority and timeout.
 * Tasks will be executed in this priority.
 * For tasks that do sync and take a lot of time only SYNC_TASK priority should be used.
 * The task priority also determines timeout for the task.
 *
 * User: Gil Adjiashvili
 * Date: 2/4/13
 */
public enum OpbTaskPriority {

    // Lower value for priority int => higher priority for the task
    // timeout = -1: get timeout from site params SYNC_ADHOC_TIMEOUT_SECONDS or SYNC_REGULAR_TIMEOUT_SECONDS at runtime
    AD_HOC_TASK(TaskPriority.ADHOC, -1),
    SYNC_TASK(TaskPriority.REGULAR, -1);

    private final TaskPriority priority;
    private final int defaultTimeout;

    OpbTaskPriority(TaskPriority priority, int defaultTimeout) {
        this.priority = priority;
        this.defaultTimeout = defaultTimeout;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }
}

