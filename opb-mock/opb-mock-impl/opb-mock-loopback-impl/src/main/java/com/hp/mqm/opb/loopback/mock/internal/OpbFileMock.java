package com.hp.mqm.opb.loopback.mock.internal;

import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by ginni on 30/04/2015.
 *
 */
public class OpbFileMock {
    OpbTask entity;
    String storageId;
    InputStream inputStream;

    public OpbFileMock(OpbTask entity, String storageId, InputStream inputStream) {
        this.entity = entity;
        this.storageId = storageId;
        this.inputStream = inputStream;
    }

    public String getStorageId() {
        return storageId;
    }

    public long getLength() { return 0; }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String toString() {
        return "OpbMockFile: " + "StorageId: " + getStorageId();
    }
}
