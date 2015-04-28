package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.api.TaskInputId;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class TaskInputIdMockImpl implements TaskInputId {
    private String id = "";
    TaskInputIdMockImpl(String id) {
        this.id = id ;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
