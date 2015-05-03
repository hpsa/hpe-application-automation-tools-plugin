package service;

import callback.SampleIncomingDataCallback;
import callback.SampleOutgoingDataCallback;
import callback.SampleResponseCallback;
import com.hp.mqm.opb.loopback.mock.ConstantsShared;
import com.hp.mqm.opb.loopback.mock.OpbLoopbackContext;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbAgentMockImpl;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbEndpointMockImpl;
import com.hp.mqm.opb.service.TaskPriority;
import com.hp.mqm.opb.service.TaskResult;
import com.hp.mqm.opb.service.api.OpbServiceApi;
import com.hp.mqm.opb.service.api.OpbTaskConfiguration;
import com.hp.mqm.opb.service.api.entities.OpbAgent;
import com.hp.mqm.opb.service.api.entities.OpbEndpoint;
import com.hp.mqm.opb.service.api.entities.OpbTask;


import java.util.HashMap;
import java.util.Map;

public class SampleService {

    private OpbLoopbackContext opbLoopbackContext;


    OpbAgent agent;
    OpbEndpoint endPoint;

    public SampleService() {
        mocksSetup();
    }

    // ******************************************************************************************************

    /**
     * this method sets up the mock environment.
     * It will not be required in the non-mock version.
     */
    private void mocksSetup() {
        // the opbLoopbackContext sets up the mocks
        opbLoopbackContext = OpbLoopbackContext.initContext();
        // optionally register credentials, they can be retrieved on the Agent side
        opbLoopbackContext.registerCredentials(ConstantsShared.CREDS_ID, ConstantsShared.CREDS_NAME, "alm", "alm", null);
        agent = new OpbAgentMockImpl(ConstantsShared.OPB_AGENT_ID, ConstantsShared.OPB_AGENT_GUID);
        endPoint = new OpbEndpointMockImpl(ConstantsShared.OPB_ENDPOINT_ID, ConstantsShared.OPB_AGENT_ID);
        opbLoopbackContext.addEndPoint(endPoint);
    }

    /**
     * For each task you want to submit, use this way to obtain the service,
     * same mock service cannot be reused to submit 2 separate tasks
     * (will get exception if tried, multitasking constraints..)
     */
    public OpbServiceApi getOpbService() {
        return opbLoopbackContext.getOpbService();
    }

    /**
     * Example of task submission to agent
     */
    public void runSampleTask() {
        OpbServiceApi opbService = getOpbService();
        //create task
        OpbTask task = opbService.getObjectFactory().createTask("Sync task", "Sync", endPoint.getId(), agent.getGuid(),
                ConstantsShared.SAMPLE_TASK_EXECUTOR, new OpbTaskConfiguration(
                        TaskPriority.REGULAR,
                        1,
                        SampleIncomingDataCallback.class,
                        SampleOutgoingDataCallback.class,
                        SampleResponseCallback.class));

        Map<String, String> params = new HashMap<>();
        params.put("TaskParam_key1", "TaskParam_value1");
        task.setParameters(params);
        System.out.println("SERVICE: " + " Submitting task...");
        opbService.submitTask(task);
        joinAllThreads();
        System.out.println("SERVICE: " + "Get task result...");
        TaskResult taskResult = opbService.getTaskResult(task.getId());
        System.out.println("SERVICE: task result status: " + taskResult.getTaskResultStatus() + " Message: " + taskResult.getContent());
    }

    public void joinAllThreads() {
        opbLoopbackContext.joinAllThreads();
    }

    public static void main(String[] args) {
        SampleService sampleMaasTask = new SampleService();
        sampleMaasTask.runSampleTask();
        sampleMaasTask.joinAllThreads();
    }
}

