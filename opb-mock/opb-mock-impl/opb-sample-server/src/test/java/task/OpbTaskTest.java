package task;

import callback.SampleIncomingDataCallback;
import callback.SampleOutgoingDataCallback;
import callback.SampleResponseCallback;
import com.hp.mqm.opb.loopback.mock.OpbLoopbackContext;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbAgentMockImpl;
import com.hp.mqm.opb.loopback.mock.service.entities.OpbEndpointMockImpl;
import com.hp.mqm.opb.service.TaskPriority;
import com.hp.mqm.opb.service.TaskResult;
import com.hp.mqm.opb.service.TaskResultStatus;
import com.hp.mqm.opb.service.api.OpbServiceApi;
import com.hp.mqm.opb.service.api.OpbTaskConfiguration;
import com.hp.mqm.opb.service.api.entities.OpbAgent;
import com.hp.mqm.opb.service.api.entities.OpbEndpoint;
import com.hp.mqm.opb.service.api.entities.OpbTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OpbTaskTest {

    private OpbLoopbackContext runner;

    private final static String TASK_NAME = "getALMDomains";
    private final static String TASK_DESCRIPTION = "getting ALM domains task";

    private final static String TASK_PARAMS_KEY = "key1";
    private final static String TASK_PARAMS_VALUE = "value1";

    private final static String CREDS_USER = "alm";
    private final static String CREDS_PASS = "alm";
    private final static String CREDS_NAME = "myCreds";

    private int taskId;

    private OpbEndpoint opbEndpoint = null;
    private OpbAgent agent = null;
    @Before
    public void setup() {
        // the runner sets up the mocks
        runner = OpbLoopbackContext.initContext();

        // make the callbacks asynchronous (more like in reality), this means for example that the
        // result callback may come BEFORE the send data callback, so the app code needs to be prepared for this
        runner.setExecuteTaskDelayIntervalMillis(1000);
        runner.setIncomingDataDelayIntervalMillis(1000);
        runner.setOutgoingDataDelayIntervalMillis(1000);

        String credentialsId = "1";
        runner.registerCredentials(credentialsId, CREDS_NAME, CREDS_USER, CREDS_PASS, null);

        agent = new OpbAgentMockImpl(101, "1121-1113-2242-3533");
        createEndPoint(credentialsId, agent.getId());
        runner.addEndPoint(opbEndpoint);
    }

    private OpbEndpoint createEndPoint(String credentialsId, Integer agentId) {
        opbEndpoint = new OpbEndpointMockImpl(1, 111);
        opbEndpoint.setType("endpointType");
        opbEndpoint.setName("ALM");
        opbEndpoint.setCredentialsId(credentialsId);
        opbEndpoint.setDescription("Description");
        opbEndpoint.setAgentId(agentId);
        return opbEndpoint;
    }

    public OpbServiceApi getOpbService() {
        return runner.getOpbService();
    }


    @Test
    public void testSuccess() {
        // create task
        OpbServiceApi opbService = getOpbService();
        OpbTask task = opbService.getObjectFactory().createTask(TASK_DESCRIPTION, TASK_NAME, opbEndpoint.getId(), agent.getGuid(),
                SampleTaskExecutorTest.class.getCanonicalName(), new OpbTaskConfiguration(
                        TaskPriority.REGULAR,
                        1,
                        SampleIncomingDataCallback.class,
                        SampleOutgoingDataCallback.class,
                        SampleResponseCallback.class));

        Map<String, String> params = new HashMap<>();
        params.put(TASK_PARAMS_KEY, TASK_PARAMS_VALUE);
        task.setParameters(params);
        // submit the task (asynchronous)
        taskId = opbService.submitTask(task);
        // wait for all the threads to exit
        runner.joinAllThreads();
        TaskResult result = opbService.getTaskResult(taskId);
        Assert.assertEquals(TaskResultStatus.SUCCESS, result.getTaskResultStatus());
    }

    @Test
    public void testFailure() {
        // create task
        OpbServiceApi opbService = getOpbService();

        OpbTask task = opbService.getObjectFactory().createTask(TASK_DESCRIPTION, TASK_NAME, opbEndpoint.getId(), agent.getGuid(),
                SampleTaskExecutorTest.class.getCanonicalName(), new OpbTaskConfiguration(
                        TaskPriority.REGULAR,
                        1,
                        SampleIncomingDataCallback.class,
                        SampleOutgoingDataCallback.class,
                        SampleResponseCallback.class));

        // add task result callback, called when the task finishes execution on the agent (optional)
        Map<String, String> params = new HashMap<>();
        //params are empty to fail the test
        task.setParameters(params);

        // submit the task (asynchronous)

        taskId = opbService.submitTask(task);
        // wait for all the threads to exit
        runner.joinAllThreads();
        TaskResult result = opbService.getTaskResult(taskId);
        Assert.assertEquals(TaskResultStatus.FAILED, result.getTaskResultStatus());
    }
}

