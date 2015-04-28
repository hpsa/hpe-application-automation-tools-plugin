package com.hp.mqm.opb.loopback.mock.internal;

import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.InputStream;
import java.util.List;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class OpbDataServiceMock implements OpbDataService {
    final static OpbDataService singleton = new OpbDataServiceMock();

    public static OpbDataService getInstance() {
        return singleton;
    }

    @Override
    public String storeData(OpbTask entity, InputStream data) {
        return null;
    }

    @Override
    public String storeData(OpbTask entity, InputStream data, String storageId) {
        return null;
    }

    @Override
    public void deleteData(OpbTask entity, String storageId) {

    }

    @Override
    public OpbDataContainer getDataContainer(OpbTask entity, String storageId) {
        return null;
    }

    @Override
    public List<OpbDataContainer> getAllDataContainers(OpbTask entity) {
        return null;
    }
}
