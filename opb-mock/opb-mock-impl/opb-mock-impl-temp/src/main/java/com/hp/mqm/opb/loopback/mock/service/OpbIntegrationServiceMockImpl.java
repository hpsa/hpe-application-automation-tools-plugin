package com.hp.mqm.opb.loopback.mock.service;

import com.hp.mqm.opb.FutureSendResult;
import com.hp.mqm.opb.api.Endpoint;
import com.hp.mqm.opb.api.TaskId;
import com.hp.mqm.opb.domain.TaskExecutor;
import com.hp.mqm.opb.domain.TaskMetadata;
import com.hp.mqm.opb.loopback.mock.agent.ExecutorAPIMockImpl;
import com.hp.mqm.opb.loopback.mock.agent.TaskMetadataMockImpl;
import com.hp.mqm.opb.loopback.mock.callback.SampleOutgoingDataCallback;
import com.hp.mqm.opb.loopback.mock.service.entities.FutureSendResultMockImpl;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbObjectFactoryMockImpl;
import com.hp.mqm.opb.loopback.mock.service.logging.ContextLoggersMock;
import com.hp.mqm.opb.service.FailedResult;
import com.hp.mqm.opb.service.TaskResult;
import com.hp.mqm.opb.service.TaskResultStatus;
import com.hp.mqm.opb.service.api.OpbObjectFactory;
import com.hp.mqm.opb.service.api.OpbServiceApi;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.entities.OpbEndpoint;
import com.hp.mqm.opb.service.api.entities.OpbEndpointType;
import com.hp.mqm.opb.service.api.entities.OpbTask;
import com.hp.mqm.opb.service.api.entities.OpbTaskResult;
import com.hp.mqm.opb.service.api.scheduler.OpbTaskSchedulerAPI;
import com.hp.mqm.opb.service.utils.SizeLimitationsUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * User: Gil Adjiashvili
 * Date: 4/10/13
 */
public class OpbIntegrationServiceMockImpl implements OpbServiceApi {

    private final ExecutorAPIMockImpl executionApi;

    // defaults execution point
    private Map<Integer, OpbEndpoint> endpointMap;
    private long executeSleepIntervalMillis;
    private long sendDataSleepIntervalMillis;
    private long getDataSleepIntervalMillis;

    private boolean submitTaskCalled = false;
    private OpbTaskResult taskResult;

    private List<Thread> dataInThreads;
    private Thread executionThread;

    public OpbIntegrationServiceMockImpl(ExecutorAPIMockImpl executionApi,
                                         Map<Integer, OpbEndpoint> endpointMap,
                                         long executeSleepIntervalMillis,
                                         long sendDataSleepIntervalMillis,
                                         long getDataSleepIntervalMillis) {
        this.executionApi = executionApi;
        this.endpointMap = endpointMap;
        this.executeSleepIntervalMillis = executeSleepIntervalMillis;
        this.sendDataSleepIntervalMillis = sendDataSleepIntervalMillis;
        this.getDataSleepIntervalMillis = getDataSleepIntervalMillis;
        this.dataInThreads = new LinkedList<>();
    }

    ExecutorAPIMockImpl getExecutionApi() {
        return executionApi;
    }

    long getSendDataSleepIntervalMillis() {
        return sendDataSleepIntervalMillis;
    }

    long getGetDataSleepIntervalMillis() {
        return getDataSleepIntervalMillis;
    }

    long getExecuteSleepIntervalMillis() {
        return executeSleepIntervalMillis;
    }


    @Override
    public int submitTask(OpbTask task) {
        if (submitTaskCalled) {
            throw new RuntimeException("Submit already called on this service mock, use OpbLoopbackContext.getOpbService() for every new task you submit.");
        }
        submitTaskCalled = true;
        executionApi.setMyTask(task); // execution api is recreated for each new task submitted (this is the reason for the submitTaskCalled boolean - to make sure one submit per api) so it's ok to set the task here (otherwise multi-threading problems)

        if (!SizeLimitationsUtils.verifyStringMapSizeConstraint(task.getParameters(), SizeLimitationsUtils.MAX_SIZE_OF_TASK_PARAMS)) {
            throw new RuntimeException("Size of task parameters cannot exceed: " + SizeLimitationsUtils.MAX_SIZE_OF_TASK_PARAMS + " bytes.");
        }

        OpbEndpoint opbEndpoint = endpointMap.get(task.getEndpointId());
        Endpoint endpoint = new EndpointMock(opbEndpoint);
        TaskMetadata metadata = new TaskMetadataMockImpl(new TaskId(task.getId(), task.getGuid()), task.getType(), task.getDescription(), endpoint, null);
        TaskExecutor executor = getTaskExecutor(task);
        if (executor == null) {
            throw new RuntimeException("Executor for task: " + task.getType() + " not found.");
        }

        // run the task asynchronously
        Thread thread = new Thread(new TaskExecutionThreadMock(this, task, metadata, executor));
        executionThread = thread;
        thread.setDaemon(false);
        thread.start();
        return task.getId();
    }

    @Override
    public void updateFailedTaskResult(int taskId, FailedResult result) {

    }

    @Override
    public void updateOkTaskResult(int taskId, String resultJson) {

    }

    public void setTaskResult(OpbTaskResult taskResult) {
        this.taskResult = taskResult;
    }

    @Override
    public TaskResult getTaskResult(int taskId) {
        return new TaskResult(TaskResultStatus.valueOf(taskResult.getStatus()), taskResult.getContent());
    }

    @Override
    public List<OpbEndpointType> getEndpointTypes() {
        return null;
    }

    public void joinAllPreviousThreads() {
        Thread currentThread = Thread.currentThread();
        for (Thread thread : dataInThreads) {
            if (currentThread.equals(thread)) break;
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void joinAllThreads() {
        try {
            if (executionThread != null) {
                executionThread.join();
            }
            for (Thread thread : dataInThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public OpbObjectFactory getObjectFactory() {
        return new OpbObjectFactoryMockImpl();
    }

    @Override
    public void updateTaskProgress(int taskId, String status, String description, int percentage) {

    }

    @Override
    public void cancelTask(int taskId) {
        OpbTask task = executionApi.getMyTask();
        task.setIsCancelled(true);
        System.out.println("Request to Cancel task");
    }

    @Override
    public OpbTaskSchedulerAPI getOpbTaskSchedulerAPI() {
        return null;
    }


    // *********************************************************

    // Send data
    public FutureSendResult mockSendData(OpbTask task, byte[] data, Map<String, String> parameters) {
        // check size constraints
        if (!SizeLimitationsUtils.verifyStringMapSizeConstraint(parameters, SizeLimitationsUtils.MAX_SIZE_OF_SEND_DATA_PARAMS)) {
            throw new RuntimeException("Size of parameters cannot exceed: " + SizeLimitationsUtils.MAX_SIZE_OF_SEND_DATA_PARAMS + " bytes.");
        }
        if (data.length > SizeLimitationsUtils.MAX_SIZE_OF_SEND_DATA) {
            throw new RuntimeException("Size of sent data at one time cannot exceed: " + SizeLimitationsUtils.MAX_SIZE_OF_SEND_DATA + " bytes.");
        }

        Thread thread = new Thread(new IncomingDataThreadMock(this, task, data, parameters));
        dataInThreads.add(thread);
        thread.setDaemon(false);
        thread.start();

        return new FutureSendResultMockImpl();
    }

    // Get data
    public byte[] mockGetData(OpbTask task, Map<String, String> parameters) {
        try {
            long sleepIntervalMillis = getGetDataSleepIntervalMillis();
            if (sleepIntervalMillis > 0) {
                Thread.sleep(sleepIntervalMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }


        if(task.getOutgoingBackendPoint() == null) {
            return null;
        }
        SampleOutgoingDataCallback sampleOutgoingDataCallback;
        try {
            sampleOutgoingDataCallback = (SampleOutgoingDataCallback) Class.forName(task.getOutgoingBackendPoint()).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        OpbDataContainer dataContainer = sampleOutgoingDataCallback.dataOutToAgent(task.getId(), parameters, new ContextLoggersMock());
        // check that the data sent is not too big
        if (dataContainer.getDataSize() > SizeLimitationsUtils.MAX_SIZE_OF_GET_DATA) {
            throw new RuntimeException("Size of get data at one time cannot exceed: " + SizeLimitationsUtils.MAX_SIZE_OF_GET_DATA + " bytes.");
        }

        try {
            String str = IOUtils.toString(dataContainer.getDataInputStream());
            return str.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(dataContainer.getDataInputStream());
        }

    }

    /**
     * get the task executor for the given task. use reflection to create it.
     *
     * @param task the task that contain the task executor class name.
     * @return a <@see >TaskExecutor</@see>
     */
    private TaskExecutor getTaskExecutor(OpbTask task) {
        TaskExecutor taskExecutor;
        String domainClass = task.getExecutorClass();
        try {
            Class taskExecutorClass = Class.forName(domainClass);
            taskExecutor = (TaskExecutor) taskExecutorClass.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("can not instantiate task executor for " + domainClass, e);
        }
        return taskExecutor;
    }

    private class EndpointMock implements Endpoint {
        OpbEndpoint endpoint;
        EndpointMock(OpbEndpoint endpoint) {
            this.endpoint = endpoint;
        }
        @Override
        public String getCredentialsId() {
            return endpoint.getCredentialsId();
        }

        @Override
        public String getName() {
            return endpoint.getName();
        }

        @Override
        public Properties getProperties() {
            return null;
        }

        @Override
        public int getId() {
            return endpoint.getId();
        }
    }
}

