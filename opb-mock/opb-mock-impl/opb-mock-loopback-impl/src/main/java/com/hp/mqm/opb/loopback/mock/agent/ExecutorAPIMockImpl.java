package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.ExecutorAPI;
import com.hp.mqm.opb.ExecutorApiFactory;
import com.hp.mqm.opb.ExecutorException;
import com.hp.mqm.opb.FutureSendResult;
import com.hp.mqm.opb.api.*;
import com.hp.mqm.opb.loopback.mock.service.OpbServiceMockImpl;
import com.hp.mqm.opb.loopback.mock.service.entities.FutureSendResultMockImpl;
import com.hp.mqm.opb.service.api.entities.OpbTask;
import com.hp.mqm.opb.service.utils.SizeLimitationsUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ginni
 * Date: 4/15/15
 * mock to Execution API
 */
public class ExecutorAPIMockImpl implements ExecutorAPI {

    private OpbServiceMockImpl integrationService;
    private OpbTask myTask;
    private Map<String, EndpointCredentials> credentials;
    private Map<String, Map<String, String>> savedGetDataParams;

    public ExecutorAPIMockImpl() {
        savedGetDataParams = new HashMap<>();
    }

    public void setMyTask(OpbTask myTask) {
        this.myTask = myTask;
    }

    public OpbTask getMyTask(){
        return myTask;
    }

    public void setIntegrationService(OpbServiceMockImpl integrationService) {
        this.integrationService = integrationService;
    }

    @Override
    public FutureSendResult sendData(TaskOutputData taskOutputData, Map<String, String> parameters, boolean persistent) throws TaskCancelledException {
        return sendData(taskOutputData, parameters);
    }

    @Override
    public FutureSendResult sendData(TaskOutputData taskOutputData, Map<String, String> parameters) {
        return integrationService.mockSendData(myTask, taskOutputData.getData(), parameters);
    }

    @Override
    public FutureSendResult sendData(TaskOutputData taskOutputData, boolean persistent) throws TaskCancelledException {
        return sendData(taskOutputData, null);
    }

    @Override
    public FutureSendResult sendData(TaskOutputData taskOutputData) {
        return integrationService.mockSendData(myTask, taskOutputData.getData(), null);
    }

    @Override
    public TaskInputId prepareData(Map<String, String> parameters) {
        if (!SizeLimitationsUtils.verifyStringMapSizeConstraint(parameters, SizeLimitationsUtils.MAX_SIZE_OF_PREPARE_DATA_PARAMS)) {
            throw new ExecutorException("Size of parameters cannot exceed: " + String.format("%,d bytes.", SizeLimitationsUtils.MAX_SIZE_OF_PREPARE_DATA_PARAMS));
        }


        TaskInputId id = new TaskInputIdMockImpl(UUID.randomUUID().toString());
        if (parameters != null) {
            savedGetDataParams.put(id.getId(), parameters);
        }
        return id;
    }

    @Override
    public TaskInputId prepareData() {
        return prepareData(new HashMap<>());
    }

    @Override
    public TaskInputDataResult getPreparedData(TaskInputId id) {
        return new TaskInputDataMockImpl(integrationService.mockGetData(myTask, savedGetDataParams.get(id.getId())));
    }

    @Override
    public EndpointCredentials getCredentials(String credentialsId) {
        return credentials == null ? null : credentials.get(credentialsId);
    }

    @Override
    public FutureSendResult reportTaskProgress(TaskProgress taskProgress) {
        System.out.println("Status reported: " + taskProgress.getDescription() + ", percentage: " + taskProgress.getPercentage());
        return new FutureSendResultMockImpl();
    }

    @Override
    public ExecutorApiFactory getFactory() {
        return new ExecutorApiFactoryMockImpl();
    }

    @Override
    public DomainProperties getDomainProperties() {
        return null;
    }

    public void setCredentials(Map<String, EndpointCredentials> credentials) {
        this.credentials = credentials;
    }

    @Override
    public File getDomainExecutorWorkingFolder() {
        return null;
    }

    @Override
    public boolean isCancelled() {
        return myTask.isCancelled();
    }
}

