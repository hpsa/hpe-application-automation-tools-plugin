package com.hp.mqm.opb.loopback.mock.callback;

import com.hp.mqm.opb.service.api.callback.OpbDataInfo;
import com.hp.mqm.opb.service.api.callback.OpbResultCallbackStatus;
import com.hp.mqm.opb.service.api.callback.TaskResponseCallback;
import com.hp.mqm.opb.service.logging.ContextLoggers;


/**
 * Created by ginni on 20/04/2015.
 *
 */
public class SampleResponseCallback implements TaskResponseCallback {
    @Override
    public void response(OpbResultCallbackStatus taskResultStatus, OpbDataInfo dataInfo, ContextLoggers callbackLoggers) {
        if(taskResultStatus.isSuccess()) {
            callbackLoggers.getDetailedLogger().info("Sample Response Callback: Task Success! Status is " + taskResultStatus.getStatus() + " Message: " + taskResultStatus.getResult());
        } else {
            callbackLoggers.getDetailedLogger().info("Sample Response Callback: Task Failed ! Status is " + taskResultStatus.getStatus() + " Message: " + taskResultStatus.getResult());
        }
    }
}
