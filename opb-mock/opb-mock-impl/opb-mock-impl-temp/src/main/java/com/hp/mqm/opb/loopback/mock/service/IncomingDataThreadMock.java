package com.hp.mqm.opb.loopback.mock.service;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbDataContainerMockImpl;
import com.hp.mqm.opb.loopback.mock.service.logging.ContextLoggersMock;
import com.hp.mqm.opb.service.api.callback.IncomingDataCallback;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * For data that flows Agent to Maas
 *
 * User: Gil Adjiashvili
 * Date: 4/18/13
 */
public class IncomingDataThreadMock implements Runnable {
    private OpbIntegrationServiceMockImpl mockService;
    private OpbTask task;
    private byte [] data;
    private Map<String,String> parameters;

    IncomingDataThreadMock(OpbIntegrationServiceMockImpl mockService, OpbTask task,
                           byte [] data, Map<String,String> parameters) {
        this.mockService = mockService;
        this.task = task;
        this.data = data;
        this.parameters = parameters;
    }

    @Override
    public void run() {
        try {
            long sleepIntervalMillis = mockService.getSendDataSleepIntervalMillis();
            if (sleepIntervalMillis > 0) {
                Thread.sleep(sleepIntervalMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        IncomingDataCallback incomingDataCallBackClass;
        if(task.getIncomingBackendPoint() == null) {
            return;
        }

        try {
            incomingDataCallBackClass = (IncomingDataCallback)Class.forName(task.getIncomingBackendPoint()).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        incomingDataCallBackClass.dataInFromAgent(task.getId(), new OpbDataContainerMockImpl(parameters, data.length, new ByteArrayInputStream(data)) , new ContextLoggersMock());

        // makes sure the callback is called AFTER all the send/get data threads exit, this is a guarantee
        // we give to Apps, that the callback will be called after all the send/gets are called
        mockService.joinAllPreviousThreads();

    }
}

