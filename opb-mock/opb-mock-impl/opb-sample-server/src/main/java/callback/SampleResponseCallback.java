package callback;

import com.hp.mqm.opb.loopback.mock.service.entities.OpbRepositoryDataContainerImpl;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.callback.OpbDataInfo;
import com.hp.mqm.opb.service.api.callback.OpbResultCallbackStatus;
import com.hp.mqm.opb.service.api.callback.TaskResponseCallback;
import com.hp.mqm.opb.service.logging.ContextLoggers;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;


/**
 * Created by ginni on 20/04/2015.
 *
 */
public class SampleResponseCallback implements TaskResponseCallback {
    @Override
    public void response(OpbResultCallbackStatus taskResultStatus, OpbDataInfo dataInfo, ContextLoggers callbackLoggers) {

        List<OpbDataContainer> containers = dataInfo.getDataContainers();
        callbackLoggers.getDetailedLogger().info("Sample Response Callback : " + ((OpbRepositoryDataContainerImpl) containers.get(0)).getEntityFile());
        try {
            String taskParams = IOUtils.toString(containers.get(0).getDataInputStream());
            callbackLoggers.getDetailedLogger().info("Sample Response Callback : Printing sample data : " + taskParams);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if(taskResultStatus.isSuccess()) {
            callbackLoggers.getDetailedLogger().info("Sample Response Callback: Task Success! Status is " + taskResultStatus.getStatus() + " Message: " + taskResultStatus.getResult());
        } else {
            callbackLoggers.getDetailedLogger().info("Sample Response Callback: Task Failed ! Status is " + taskResultStatus.getStatus() + " Message: " + taskResultStatus.getResult());
        }
    }
}
