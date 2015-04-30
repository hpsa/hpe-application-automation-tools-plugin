package com.hp.mqm.opb.loopback.mock.service.entities;


import com.hp.mqm.opb.loopback.mock.internal.OpbFileMock;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * A data container using smart repository service
 */
public class OpbRepositoryDataContainerImpl extends OpbDataContainerMockImpl {
    // We can not use Spring injection here since this instance is not singleton.
    // For each piece of data received from OPB_Agent we wrap it into OpbDataContainer

    private OpbFileMock entityFile;

    public OpbRepositoryDataContainerImpl(OpbFileMock entityFile) {
        this(entityFile, null);
    }

    public OpbRepositoryDataContainerImpl(OpbFileMock entityFile, Map<String, String> params) {
        super(params, 0, null);
        this.entityFile = entityFile;
    }

    @Override
    public long getDataSize() {
        return entityFile.getLength();
    }

    @Override
    public InputStream getDataInputStream() {
        return entityFile.getInputStream();
    }

    public OpbFileMock getEntityFile() {
        return entityFile;
    }
}
