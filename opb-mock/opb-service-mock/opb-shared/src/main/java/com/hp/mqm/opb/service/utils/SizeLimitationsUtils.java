package com.hp.mqm.opb.service.utils;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * User: Gil Adjiashvili
 * Date: 4/22/13
 * Verifies size limitations for objects passed in requests
 */
public class SizeLimitationsUtils {

    public static final int MAX_SIZE_OF_TASK_PARAMS = 2048;
    public static final int MAX_SIZE_OF_SEND_DATA_PARAMS = 1024;
    public static final int MAX_SIZE_OF_PREPARE_DATA_PARAMS = 5 * 1024;
    public static final int MAX_SIZE_OF_REPORT_PROGRESS_PARAMS = 10 * 1024;

    public static final int MAX_SIZE_OF_SEND_DATA = (int)(20 * 1024 * 1024); //default value of the data send from agent to agm, can be configured by vm param
    public static final int MAX_SIZE_OF_GET_DATA = (int)(20 * 1024 * 1024); //value of the data send from agm to agent, cannot be configured
    public static final int MAX_SIZE_OF_GET_DATA_ENLARGE_LIMIT = (int)(30 * 1024 * 1024); //enable to enlarge value of the data send from agm to agent a bit more in case needed , cannot be configured
    public static final String MAX_SIZE_PARAM_NAME = "max.size.send.data";

    /**
     * Not very exact, doesn't account for the size of the map object itself.
     * This way doesn't require instrumentation, and also usually the size that is needed in of JSON
     * representation of the map and ont of the Java object anyway.
     *
     * @param map the map
     * @param maxSize the max size
     * @return true if the total size of the bytes in map are less than the given max size
     */
    public static boolean verifyStringMapSizeConstraint(Map<String, String> map, int maxSize) {
        if (map == null) {
            return true;
        }

        int totalSize = 0;
        Charset charset = Charset.defaultCharset();  // needed for violation fix

        // count the sizes of the keys
        for(String str : map.keySet()) {
            totalSize += str.getBytes(charset).length;
            if (totalSize >= maxSize) {
                return false;
            }
        }

        // count the sizes of the values
        for(String str : map.values()) {
            totalSize += str.getBytes(charset).length;
            if (totalSize >= maxSize) {
                return false;
            }
        }
        return true;
    }

}

