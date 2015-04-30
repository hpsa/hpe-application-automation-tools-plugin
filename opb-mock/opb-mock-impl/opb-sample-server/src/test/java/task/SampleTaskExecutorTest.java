package task;

import com.hp.mqm.opb.loopback.mock.ConstantsShared;
import com.hp.mqm.opb.ExecutorAPI;
import com.hp.mqm.opb.TaskExecutionResult;
import com.hp.mqm.opb.TaskFinishStatus;
import com.hp.mqm.opb.api.*;
import com.hp.mqm.opb.domain.TaskExecutor;
import com.hp.mqm.opb.domain.TaskMetadata;
import com.hp.mqm.opb.loopback.mock.service.logging.AgentLoggerMockImpl;
import com.hp.mqm.opb.service.logging.ContextLoggers;
import org.junit.Assert;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ginni on 21/04/2015.
 *
 */

public class SampleTaskExecutorTest implements TaskExecutor {
    private final static String TASK_NAME = "getALMDomains";
    private final static String TASK_DESCRIPTION = "getting ALM domains task";

    private final static String TASK_PARAMS_KEY = "key1";
    private final static String TASK_PARAMS_VALUE = "value1";

    private final static String DATA_IN_VALUE = "Data In Sample";
    private final static String DATA_OUT_VALUE = "Data Out Sample";

    private final static String OK = "OK";
    private final static String FAILED_MESSAGE = "Me failed";

    private final static String CREDS_USER = "alm";
    private final static String CREDS_PASS = "alm";
    private final static String CREDS_NAME = "myCreds";


    @Override
    public TaskExecutionResult execute(TaskMetadata taskMetadata, ExecutorAPI executorAPI, ContextLoggers contextLoggers) {
        Assert.assertNotNull(taskMetadata.getId());
        Assert.assertEquals(taskMetadata.getName(), TASK_NAME);
        Assert.assertEquals(taskMetadata.getDescription(), TASK_DESCRIPTION);
        Endpoint endpoint = taskMetadata.getEndpoint();
        Assert.assertNotNull(endpoint);
        Assert.assertNotNull(endpoint.getName());
        Assert.assertNotNull(endpoint.getCredentialsId());

        Properties taskParameters = taskMetadata.getTaskParameters();
        boolean testingFailure = false;
        if (taskParameters.size() == 0) {
            // test the failure flow
            testingFailure = true;
        }
        Assert.assertEquals(taskParameters.size(), 1);
        Assert.assertTrue(taskMetadata.getTaskParameters().keySet().contains(TASK_PARAMS_KEY));
        Assert.assertTrue(taskMetadata.getTaskParameters().values().contains(TASK_PARAMS_VALUE));
        AgentLogger logger = new AgentLoggerMockImpl();
        Map<String, String> params = new HashMap<>();
        params.put(TASK_PARAMS_KEY, TASK_PARAMS_VALUE);
        params.put(ConstantsShared.DATA_OUT_PARAMS, DATA_OUT_VALUE);
        try {
            executorAPI.sendData(executorAPI.getFactory().createTaskOutputData(DATA_IN_VALUE.getBytes(Charset.forName("UTF-8"))), params);
            // report progress
            executorAPI.reportTaskProgress(executorAPI.getFactory().createTaskProgress("in progress..", 50, null));
            TaskInputId dataId = executorAPI.prepareData(params);
            // Get data
            TaskInputData dataFromMqm = executorAPI.getPreparedData(dataId);
            Assert.assertEquals(DATA_OUT_VALUE, new String(dataFromMqm.getData()));

            // Get credentials
            EndpointCredentials credentials = executorAPI.getCredentials(CREDS_NAME);
            Assert.assertNotNull(credentials);
            Assert.assertEquals(credentials.getUser(), CREDS_USER);
            Assert.assertEquals(new String(credentials.getPassword()), CREDS_PASS);
        } catch (Exception e) {
            logger.error(getClass(), "Rmi failed", e);
            throw new IllegalStateException("Rmi failed", e);
        }


        // mock logger coverage :)

        logger.debug(getClass(), "debug..");
        logger.info(getClass(), "info..");
        logger.warn(getClass(), "warn..");
        logger.error(getClass(), "error..");
        logger.fatal(getClass(), "fatal..");
        logger.fatal(getClass(), "fatal..", null);
        logger.error(getClass(), "fatal..", null);
        logger.isDebugEnabled(getClass());

        // the result will be returned in the result callback
        if(testingFailure) {
            return executorAPI.getFactory().createTaskExecutionResult(
                    TaskFinishStatus.FAILED,
                    FAILED_MESSAGE);
        } else {
            return executorAPI.getFactory().createTaskExecutionResult(
                    TaskFinishStatus.SUCCESS,
                    OK);
        }
    }

    @Override
    public void shutdown() {}
}
