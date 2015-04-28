package com.hp.mqm.opb.service.api.callback;


import com.hp.mqm.opb.service.logging.ContextLoggers;

/**
 * Callback for incoming data from agent.
 *
 * Note the callback instance is created using reflection with default no-parameters constructor.
 * So, do not implement a constructor with parameters or using Spring annotation
 * since all of the params will be valued as null
 */
public interface IncomingDataCallback {
    /**
     * Execute the callback when receiving data from agent.
     * Binary data has already stored and can be accessed by {@link OpbDataContainer}
     *
     * The binary data storage will be deleted once task execution is completed.
     *
     * @param taskId - task id
     * @param dataContainer - The data container
     * @param callbackLoggers - Callback user/detailed loggers
     */
    void dataInFromAgent(Integer taskId, OpbDataContainer dataContainer, ContextLoggers callbackLoggers);
}
