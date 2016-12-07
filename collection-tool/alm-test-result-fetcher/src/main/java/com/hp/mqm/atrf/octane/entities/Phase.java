package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class Phase extends OctaneEntity {

    public static String TYPE = "phase";
    public static String COLLECTION_NAME = "phases";

    public Phase() {
        super(TYPE);
    }
}
