package com.hp.mqm.atrf.octane.core;

import com.hp.mqm.atrf.core.entities.MapBasedObject;

/**
 * Created by berkovir on 11/12/2016.
 */
public class OctaneTestResultOutput extends MapBasedObject {
    public static final String FIELD_ID = "id";
    public static final String FIELD_STATUS = "status";

    public static final String FIELD_BULK_ID = "bulkId";
    public static final String FIELD_FIRST_RUN_ID = "firstRunId";
    public static final String FIELD_LAST_RUN_ID = "lastRunId";

    public static final String FAILED_SEND_STATUS = "failed to send";
    public static final String ERROR_STATUS = "error";


    public Integer getId() {
        return (Integer) get(FIELD_ID);
    }

    public String getStatus() {
        return getString(FIELD_STATUS);
    }
}
