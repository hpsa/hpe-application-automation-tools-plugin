package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class Release extends OctaneEntity {

    public static String TYPE = "release";
    public static String COLLECTION_NAME = "releases";

    public Release() {
        super(TYPE);
    }
}
