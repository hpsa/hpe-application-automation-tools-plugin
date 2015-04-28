package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.OpbObjectFactory;
import com.hp.mqm.opb.service.api.OpbTaskConfiguration;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.InputStream;

/**
 * Created by ginni on 20/04/2015.
 *
 */
public class OpbObjectFactoryMockImpl implements OpbObjectFactory {
    @Override
    public OpbDataContainer createDataContainer(long size, InputStream dataInputStream) {
        return null;
    }

    @Override
    public OpbTask createTask(String description, String type, int endpointId, String executorClass, OpbTaskConfiguration configuration) {
        return createTask(description, type, endpointId, null, executorClass, configuration);
    }

    @Override
    public OpbTask createTask(String description, String type, String agentGuid, String executorClass, OpbTaskConfiguration configuration) {
        return createTask(description, type, -1, agentGuid, executorClass, configuration);
    }

    @Override
    public OpbTask createTask(String description, String type, int endpointId, String agentGuid, String executorClass, OpbTaskConfiguration configuration) {
        OpbTask task = new OpbTaskMockImpl(111, endpointId, 123, agentGuid, executorClass);

        task.setDescription(description);
        task.setType(type);
        task.setEndpointId(endpointId);
        task.setAgentGuid(agentGuid);
        task.setExecutorClass(executorClass);

        if (configuration.getIncomingDataCallback() != null) {
            task.setIncomingBackendPoint(configuration.getIncomingDataCallback().getName());
        }
        if (configuration.getOutgoingDataCallback() != null) {
            task.setOutgoingBackendPoint(configuration.getOutgoingDataCallback().getName());
        }
        if (configuration.getTaskResponseCallback() != null) {
            task.setResponseBackendPoint(configuration.getTaskResponseCallback().getName());
        }
        task.setPriority(configuration.getTaskPriority().name());
        task.setTimeoutSeconds(configuration.getTimeoutSeconds());

        return task;
    }
}
