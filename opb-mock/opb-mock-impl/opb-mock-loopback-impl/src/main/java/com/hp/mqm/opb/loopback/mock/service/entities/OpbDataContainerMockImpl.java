package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.callback.OpbDataContainer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class OpbDataContainerMockImpl implements OpbDataContainer {

    private Map<String, String> dataParams;
    private InputStream stream;
    private long size;

    public OpbDataContainerMockImpl(Map<String, String> dataParams, long size, InputStream stream) {
        this.dataParams = dataParams;
        this.stream = stream;
        this.size = size;
    }

    @Override
    public long getDataSize() {
        return size;
    }

    @Override
    public InputStream getDataInputStream() {
        return stream;
    }

    @Override
    public Map<String, String> getDataParameters() {
        if (dataParams == null) {
            return new HashMap<>();
        }
        return dataParams;
    }
}
