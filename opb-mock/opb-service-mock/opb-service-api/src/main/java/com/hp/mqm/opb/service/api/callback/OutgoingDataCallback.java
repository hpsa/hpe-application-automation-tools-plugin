package com.hp.mqm.opb.service.api.callback;
import com.hp.mqm.opb.service.logging.ContextLoggers;

import java.util.Map;

/**
 * Callback for transferring data out to agent.
 *
 * Note the callback instance is created using reflection with default no-parameters constructor.
 * <p>So, do not implement a constructor with parameters or using Spring annotation
 * since all of the params will be valued as null.</p>
 * <p>To get bean, you can use QCApi.getBean(clazz)</p>
 */
public interface OutgoingDataCallback {
    /**
     * Task callback to send data to Agent.
     * Return the cached data id which is used to get the real data (binary) to send out to agent.
     * After that the cached will be invalidated automatically.
     *
     * @param taskId - task id
     * @param params - request parameters
     * @param callbackLoggers - loggers for the callback
     * @return cached data id
     */
    OpbDataContainer dataOutToAgent(Integer taskId, Map<String, String> params, ContextLoggers callbackLoggers);
}
