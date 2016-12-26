package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by berkovir on 29/06/2016.
 */
public class Release extends AlmEntity {

    public static String TYPE = "release";

    public static String COLLECTION_NAME = "releases";

    public Release() {
        super(TYPE);
    }

}
