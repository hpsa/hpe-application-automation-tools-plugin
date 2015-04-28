package com.hp.mqm.opb.service.api.callback;

import java.io.InputStream;
import java.util.Map;

/**
 * Container for data. Data sent from OPB Agent will be stored by
 * Smart Repository API, and wrapped by the OpbDataContainer which in return
 * passed to the callback.
 */
public interface OpbDataContainer {

    /**
     * @return the size of the data held by the data container
     */
    long getDataSize();

    /**
     * The the data as input stream
     * @return the data input stream
     */
    InputStream getDataInputStream();

    /**
     * Return the empty map if there is no parameters
     * @return the parameters from OPB_Agent, which sent along with the binary data
     */
    Map<String, String> getDataParameters();
}
