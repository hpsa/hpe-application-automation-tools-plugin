package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.api.TaskOutputData;

/**
 * Created with IntelliJ IDEA.
 * User: ginni
 * Date: 4/15/15
 * mock to task output data
 */
public class TaskOutputDataMockImpl implements TaskOutputData {

    private static final long serialVersionUID = 8574246966438942214L;
    private byte[] data;

    public TaskOutputDataMockImpl(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
