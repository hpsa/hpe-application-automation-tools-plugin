package com.hp.mqm.opb;

import com.hp.mqm.opb.api.*;

import java.io.File;
import java.util.Map;

/**
 * Platform Executor API.
 *
 * @author avrahame
 */
public interface ExecutorAPI {

    /**
     * Send output data for task in the current execution context.
     *
     * @param taskOutputData Output data for the task in the current execution context.
     * @param parameters     Domain parameters.
     * @param persistent     Whether data is persisted on server for completion callback
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public FutureSendResult sendData(TaskOutputData taskOutputData, Map<String, String> parameters, boolean persistent) throws TaskCancelledException;

    /**
     * Send output data for task in the current execution context.
     * Data is persisted on server for completion callback
     *
     * @param taskOutputData Output data for the task in the current execution context.
     * @param parameters     Domain parameters.
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public FutureSendResult sendData(TaskOutputData taskOutputData, Map<String, String> parameters) throws TaskCancelledException;

    /**
     * Send output data for task in the current execution context.
     *
     * @param taskOutputData Output data for the task in the current execution context.
     * @param persistent     Whether data is persisted on server for completion callback
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public FutureSendResult sendData(TaskOutputData taskOutputData, boolean persistent) throws TaskCancelledException;

    /**
     * Send output data for task in the current execution context.
     * Data is persisted on server for completion callback
     *
     * @param taskOutputData Output data for the task in the current execution context.
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public FutureSendResult sendData(TaskOutputData taskOutputData) throws TaskCancelledException;

    /**
     * Start get data operation.
     * The id returned here is later used in getData call to retrieve the actual data.
     *
     * @param parameters Domain parameters.
     * @return the id used in getData call.
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public TaskInputId prepareData(Map<String, String> parameters) throws TaskCancelledException;

    /**
     * Start get data operation.
     * The id returned here is later used in getData call to retrieve the actual data.
     *
     * @return the id used in getData call.
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public TaskInputId prepareData() throws TaskCancelledException;

    /**
     * @return TaskInputDataResult which holds information about the status and the data of the receive request for the given TaskInputId.
     * <br></br><b>note:</b> if timeout was triggered then result is <code>null</code>, when using this function check for nullity first.
     * <br></br><b>note:</b> this method is blocking current thread, use with care
     * waiting for result to arrive maximum allowed time
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public TaskInputDataResult getPreparedData(TaskInputId id) throws TaskCancelledException;

    /**
     * Returns endpoint credentials according to the credentials Id given.
     *
     * @param credentialsId The credentials identifier.
     * @return Endpoint credentials according to the credentials Id given.
     */
    public EndpointCredentials getCredentials(String credentialsId);

    /**
     * Report progress of the task in the current execution context.
     *
     * @param taskProgress The task progress.
     *
     * @throws TaskCancelledException will be thrown in case the current task is cancelled
     */
    public FutureSendResult reportTaskProgress(TaskProgress taskProgress) throws TaskCancelledException;

    /**
     * Abstract factory for the api classes.
     *
     * @return factory implementation
     */
    public ExecutorApiFactory getFactory();
    
    /**
     * Returns domain descriptor which holds information defined in file domain.xml. 
     * @return
     */
    public DomainProperties getDomainProperties();

    /**
     * Returns the working folder for the domain executor.
     * In this folder the domain executor should write data, folder's content is not guaranteed to be kept.
     * @return the working folder file
     */
    public File getDomainExecutorWorkingFolder();

    /**
     * Check the task is cancelled wether or not in the current execution context
     * @return
     */
    public boolean isCancelled();
}

