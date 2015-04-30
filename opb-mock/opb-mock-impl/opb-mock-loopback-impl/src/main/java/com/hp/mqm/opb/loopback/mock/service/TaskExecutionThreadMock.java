package com.hp.mqm.opb.loopback.mock.service;

import com.hp.mqm.opb.TaskExecutionResult;
import com.hp.mqm.opb.TaskFinishStatus;
import com.hp.mqm.opb.domain.TaskExecutor;
import com.hp.mqm.opb.domain.TaskMetadata;
import com.hp.mqm.opb.loopback.mock.internal.OpbDataService;
import com.hp.mqm.opb.loopback.mock.internal.OpbDataServiceMock;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbDataInfoMockImpl;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbTaskResultMockImpl;
import com.hp.mqm.opb.loopback.mock.service.logging.ContextLoggersMockImpl;
import com.hp.mqm.opb.service.api.callback.OpbResultCallbackStatus;
import com.hp.mqm.opb.service.api.callback.TaskResponseCallback;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * User: Gil Adjiashvili Date: 4/18/13
 */
public class TaskExecutionThreadMock implements Runnable {

    private OpbServiceMockImpl mockService;
    private OpbTask task;
    private TaskMetadata metadata;
    private TaskExecutor executor;

    TaskExecutionThreadMock(
            OpbServiceMockImpl mockService,
            OpbTask task,
            TaskMetadata metadata,
            TaskExecutor executor) {
        this.mockService = mockService;
        this.task = task;
        this.metadata = metadata;
        this.executor = executor;
    }

    @Override
    public void run() {
        try {
            long sleepIntervalMillis = mockService.getExecuteSleepIntervalMillis();
            if (sleepIntervalMillis > 0) {
                Thread.sleep(sleepIntervalMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        boolean haveFailed = false;
        TaskExecutionResult result = null;
        try {
            result = executor.execute(metadata, mockService.getExecutionApi(), null);
        } catch (Throwable t) {
            haveFailed = true;
        }


        String finishStatus;
        String taskResult = "";
        //Response handling
        if (task.getResponseBackendPoint() != null) {
            // if have callback - call the callback
            TaskResponseCallback taskResponseCallback;
            try {
                taskResponseCallback = (TaskResponseCallback)Class.forName(task.getResponseBackendPoint()).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // makes sure the callback is called AFTER all the send/get data threads exit, this is a guarantee
            // we give to Apps, that the callback will be called after all the send/gets are called
            mockService.joinAllPreviousThreads();
            if(result != null) {
                finishStatus = result.getTaskFinishStatus().name();
                taskResult = result.getTaskResult();
                if(TaskFinishStatus.FAILED.name().equalsIgnoreCase(result.getTaskFinishStatus().name())) {
                    haveFailed = true;
                }

                OpbDataService opbDataServiceMock = new OpbDataServiceMock();
                if(task.getParameters() != null) {
                    byte[] data = task.getParameters().toString().getBytes();
                    opbDataServiceMock.storeData(task, new ByteArrayInputStream(data)); //store anything in mock db or repository
                }
                taskResponseCallback.response(new OpbResultCallbackStatus(task.getId(), finishStatus, !haveFailed, taskResult),
                        new OpbDataInfoMockImpl(task, opbDataServiceMock), new ContextLoggersMockImpl());
                mockService.setTaskResult(new OpbTaskResultMockImpl(task.getId(), null, finishStatus, taskResult));
            } else {
                mockService.setTaskResult(new OpbTaskResultMockImpl(task.getId(), null, TaskFinishStatus.FAILED.name(), taskResult));
            }
        }

    }
}
