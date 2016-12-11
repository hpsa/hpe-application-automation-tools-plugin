package com.hp.mqm.atrf.octane.core;

import com.hp.mqm.atrf.core.entities.MapBasedObject;

/**
 * Created by berkovir on 11/12/2016.
 */
public class OctaneTestResultOutput extends MapBasedObject {
    public static final String FIELD_ID = "id";
    public static final String FIELD_STATUS = "status";

    public String getId() {
        return getString(FIELD_ID);
    }

    public String getStatus() {
        return getString(FIELD_STATUS);
    }
}
