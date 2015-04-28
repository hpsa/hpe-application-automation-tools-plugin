package sample;

import com.hp.mqm.opb.ExecutorAPI;
import com.hp.mqm.opb.TaskExecutionResult;
import com.hp.mqm.opb.TaskFinishStatus;
import com.hp.mqm.opb.api.EndpointCredentials;
import com.hp.mqm.opb.api.TaskInputDataResult;
import com.hp.mqm.opb.api.TaskInputId;
import com.hp.mqm.opb.api.TaskOutputData;
import com.hp.mqm.opb.domain.TaskExecutor;
import com.hp.mqm.opb.domain.TaskMetadata;
import com.hp.mqm.opb.service.logging.ContextLoggers;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class SampleTaskExecutor implements TaskExecutor {

    @Override
    public TaskExecutionResult execute(TaskMetadata taskMetadata, ExecutorAPI executorAPI, ContextLoggers contextLoggers) {

        System.out.println("AGENT: doing some work...");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("key1", "val1");
        parameters.put("key2", "val2");
        parameters.put("RandRange", "40");

        try {

            // Get credentials, same ones that were created during setup
            System.out.println("AGENT: Get Credentials");
            EndpointCredentials credentials = executorAPI.getCredentials(SampleService.CREDS_NAME);
            System.out.println("AGENT: Got credentials: " + " Id: " + credentials.getId() + " Name: " + credentials.getName());

            executorAPI.reportTaskProgress(executorAPI.getFactory().createTaskProgress(
                    "Report task progress: begin processing",
                    20,
                    new HashMap<>()));
            // Get data - synchronous
            TaskInputId id = executorAPI.prepareData(parameters);
            System.out.println("AGENT: Get Prepared Data");
            TaskInputDataResult inputData = executorAPI.getPreparedData(id);
            System.out.println("AGENT: Get data of random number from callback :" +  new String(inputData.getData()));
            // Report progress
            Map<String, String> inputProperties = new HashMap<>();
            inputProperties.put("description", "processing input data");
            executorAPI.reportTaskProgress(executorAPI.getFactory().createTaskProgress(
                    "Report task progress: processing input data",
                    50,
                    inputProperties));

            // Send data
            Map<String, String> outputParams = new HashMap<>();
            outputParams.put("description", "message from bridge");

            byte[] outputBytes = inputData.getData();
            String data = new String(outputBytes, Charset.forName("UTF-8"));
            String newData = data + " (bridge processed)";
            TaskOutputData taskOutputData =
                    executorAPI.getFactory().createTaskOutputData(newData.getBytes(Charset.forName("UTF-8")));
            executorAPI.sendData(taskOutputData, outputParams);
            System.out.println("AGENT: Sent data 1");
            executorAPI.sendData(executorAPI.getFactory().createTaskOutputData("Data 2".getBytes(Charset.forName("UTF-8"))), parameters);
            System.out.println("AGENT: Sent data 2");
            executorAPI.sendData(executorAPI.getFactory().createTaskOutputData("Data 3".getBytes(Charset.forName("UTF-8"))));
            System.out.println("AGENT: Sent data 3");

            executorAPI.reportTaskProgress(executorAPI.getFactory().createTaskProgress(
                    "Report task progress: after send output ",
                    80,
                    new HashMap<>()));


            // the result string will be returned in the result callback in case it was specified (probably some JSON, domain code specific)
            // Returns task result
            return executorAPI.getFactory().createTaskExecutionResult(
                    TaskFinishStatus.SUCCESS,
                    "Task succeeded");


            //System.out.println("AGENT: Report task progress : ");
        } catch (Exception e) {
            System.out.println("Rmi Failed " + e.getMessage());
            throw new RuntimeException(e);
        }

        // to fail a task: throw an exception:
        // throw new RuntimeException("failed..");
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
