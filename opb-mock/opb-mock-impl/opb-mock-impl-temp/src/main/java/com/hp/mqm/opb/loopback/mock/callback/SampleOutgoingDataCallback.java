package com.hp.mqm.opb.loopback.mock.callback;

import com.hp.mqm.opb.loopback.mock.service.entities.OpbDataContainerMockImpl;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.callback.OutgoingDataCallback;
import com.hp.mqm.opb.service.logging.ContextLoggers;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Random;

/**
 * Created by ginni on 20/04/2015.
 *
 */
public class SampleOutgoingDataCallback implements OutgoingDataCallback {

    @Override
    public OpbDataContainer dataOutToAgent(Integer taskId, Map<String, String> params, ContextLoggers callbackLoggers) {
        callbackLoggers.getDetailedLogger().info("Sample Outgoing Data Callback params " + params);
        int rundNumRange = Integer.parseInt(params.get("RandRange"));
        Random rand = new Random();
        Integer randNum = rand.nextInt(rundNumRange) + 1;
        byte[] data = randNum.toString().getBytes();

        return new OpbDataContainerMockImpl(params, data.length, new ByteArrayInputStream(data));
    }
}
