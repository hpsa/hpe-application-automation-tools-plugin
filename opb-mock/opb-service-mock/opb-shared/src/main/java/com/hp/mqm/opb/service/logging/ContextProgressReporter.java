package com.hp.mqm.opb.service.logging;

/**
 * Created by minh.nguyen-van@hp.com on 12/19/14.
 */
public interface ContextProgressReporter {
    /**
     * Update progress for task.
     *
     * @param percentage    progress percentage
     * @param message       progress message
     */
    public void updateProgress(int percentage, String message);

    /**
     * Update progress for task, setting percentage to the task's current percentage
     *
     * @param message       progress message
     */
    public void updateProgress(String message);
}
