package com.hp.mqm.opb.loopback.mock.callback;

import com.hp.mqm.opb.service.api.callback.IncomingDataCallback;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.logging.ContextLoggers;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ginni on 20/04/2015.
 *
 */
public class SampleIncomingDataCallback implements IncomingDataCallback {
    @Override
    public void dataInFromAgent(Integer taskId, OpbDataContainer dataContainer, ContextLoggers callbackLoggers) {
        Map<String, String> params = dataContainer.getDataParameters();
        for(String param : params.keySet()) {
            callbackLoggers.getDetailedLogger().info("Sample Incoming Data Callback: printing param " + param + " with value " + params.get(param));
        }

        try {
            callbackLoggers.getDetailedLogger().info("Sample Incoming Data Callback, got data: " + IOUtils.toString(dataContainer.getDataInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(dataContainer.getDataInputStream());
        }
    }
}
