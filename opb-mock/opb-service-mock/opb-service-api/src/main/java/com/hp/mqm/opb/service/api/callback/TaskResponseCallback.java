package com.hp.mqm.opb.service.api.callback;


import com.hp.mqm.opb.service.logging.ContextLoggers;

/**
 * Callback for task completion
 *
 * Note the callback instance is created using reflection with default no-parameters constructor.
 * So, do not implement a constructor with parameters or using Spring annotation
 * since all of the params will be valued as null
 */
public interface TaskResponseCallback {
    /**
     * Task callback once it finished. A task is considered as finished when it has at least one task progress and
     * the task progress status is in:
     * <ol>
     *     <li>TaskStatus.SUCCESS</li>
     *     <li>TaskStatus.FAILED</li>
     *     <li>TaskStatus.FINISHED_WITH_ERRORS</li>
     * </ol>
     *
     * @param taskResultStatus - task result (task id, is success, message,...)
     * @param dataInfo - chunk of data for this task, provided with the lazy-load feature. See {@link OpbDataInfo}
     * @param callbackLoggers - user/detailed loggers
     */
    void response(OpbResultCallbackStatus taskResultStatus, OpbDataInfo dataInfo, ContextLoggers callbackLoggers);
}
