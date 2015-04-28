package com.hp.mqm.opb.loopback.mock.internal;


import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.InputStream;
import java.util.List;

/**
 * Handles put/get data from/to the application
 * As part of the operation the {@link OpbDataService} uses other services
 */
public interface OpbDataService {

    /**
     * Stores the given data to the storage of task entity
     *
     * @param entity
     *
     * @param data
     *      the data to be saved
     * @return the file path which can be used to retrieve the stored data
     */
    String storeData(OpbTask entity, InputStream data);

    /**
     * Stores the given data to the storage of task entity
     *
     * @param entity
     *
     * @param data
     *      the data to be saved
     * @param storageId
     *      the data storage id
     * @return the file path which can be used to retrieve the stored data
     */
    String storeData(OpbTask entity, InputStream data, String storageId);

    /**
     * Delete the given data from repository
     *
     * @param entity
     *      the entity that file is linked into
     * @param storageId
     *      agm data storage id
     */
    void deleteData(OpbTask entity, String storageId);

    /**
     *
     * @param entity
     *      {@link OpbTask} task entity
     * @param storageId
     *      agm data storage id
     * @return
     *
     */
    OpbDataContainer getDataContainer(OpbTask entity, String storageId);

    /**
     * Retrieves stored data(s) for given task id
     *
     * @param entity
     *      the task entity
     * @return
     *      list of {@link OpbDataContainer} if exists, otherwise returns empty list
     */
    List<OpbDataContainer> getAllDataContainers(OpbTask entity);
}
