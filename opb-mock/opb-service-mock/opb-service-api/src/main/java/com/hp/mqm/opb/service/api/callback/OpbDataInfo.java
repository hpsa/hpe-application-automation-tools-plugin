package com.hp.mqm.opb.service.api.callback;

import java.util.List;

public interface OpbDataInfo {
    /**
     * Use in the {@link TaskResponseCallback} to lazy-load
     * the {@link OpbDataContainer} list, we only load all data of a tasks from database when this method
     * is called.
     *
     * @return List of {@link OpbDataContainer} or empty list if there is no data
     */
    List<OpbDataContainer> getDataContainers();
}
