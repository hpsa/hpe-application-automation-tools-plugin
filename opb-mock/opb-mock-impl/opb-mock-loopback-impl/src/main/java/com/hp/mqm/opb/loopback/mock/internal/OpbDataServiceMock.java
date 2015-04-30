package com.hp.mqm.opb.loopback.mock.internal;

import com.hp.mqm.opb.loopback.mock.service.entities.OpbRepositoryDataContainerImpl;
import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.InputStream;
import java.util.*;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class OpbDataServiceMock implements OpbDataService {
    final static OpbDataService singleton = new OpbDataServiceMock();

    public Map<String, OpbFileMock> storageMap = new HashMap<>();

    public static OpbDataService getInstance() {
        return singleton;
    }

    @Override
    public String storeData(OpbTask entity, InputStream data) {
        String fileUuid = UUID.randomUUID().toString();
        return storeData(entity, data, fileUuid);
    }

    @Override
    public String storeData(OpbTask entity, InputStream data, String storageId) {
        storageMap.put(storageId, new OpbFileMock(entity, storageId, data));
        return storageId;
    }

    @Override
    public void deleteData(OpbTask entity, String storageId) {
        storageMap.remove(storageId);
    }

    @Override
    public OpbDataContainer getDataContainer(OpbTask entity, String storageId) {
        OpbFileMock entityFile = storageMap.get(storageId);
        return new OpbRepositoryDataContainerImpl(entityFile);
    }

    @Override
    public List<OpbDataContainer> getAllDataContainers(OpbTask entity) {
        List<OpbDataContainer> result = new ArrayList<>(storageMap.size());
        for (OpbFileMock fileMock : storageMap.values()) {
            result.add(getDataContainer(entity, fileMock.getStorageId()));
        }
        return result;
    }
}
