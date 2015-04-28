package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.loopback.mock.internal.OpbDataService;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.callback.OpbDataInfo;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.util.List;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class OpbDataInfoMockImpl implements OpbDataInfo {

    private OpbTask task;
    private OpbDataService dataHandler;

    public OpbDataInfoMockImpl(OpbTask task, OpbDataService dataHandler) {
        this.task = task;
        this.dataHandler = dataHandler;
    }

    @Override
    public List<OpbDataContainer> getDataContainers() {
        return dataHandler.getAllDataContainers(task);
    }
}
