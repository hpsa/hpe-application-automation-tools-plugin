package com.hp.mqm.opb.domain;

import com.hp.mqm.opb.ExecutorAPI;
import com.hp.mqm.opb.TaskExecutionResult;
import com.hp.mqm.opb.service.logging.ContextLoggers;

/**
 * An executor for specific task.
 * 
 * @author avrahame
 */
public interface TaskExecutor {
    
    /**
     * Execute task describes by the metadata provided.
     * 
     * @param taskMetadata
     *            The task metadata.
     * @param executorAPI
     *            The executor API.
     * @param contextLoggers
     *            container for both user & detailed loggers.
     * @return Execution result.
     */
    public TaskExecutionResult execute(TaskMetadata taskMetadata, ExecutorAPI executorAPI,
                                       ContextLoggers contextLoggers);
    
    /**
     * Acts as as an entry point to clean resources before performing shutdown. Using best effort
     * policy, there is no guaranty for this method being called during the shutdown process.
     * <BR><B>Important note:</B>this method will be called in the same thread context of the {@link TaskExecutor#execute(TaskMetadata, ExecutorAPI, ContextLoggers)} method! <BR>
     * for example, calling <pre>Thread.currentThread().interrupt();</pre> will not interrupt the {@link TaskExecutor#execute(TaskMetadata, ExecutorAPI, ContextLoggers)} method.
     */
    public void shutdown();
}
