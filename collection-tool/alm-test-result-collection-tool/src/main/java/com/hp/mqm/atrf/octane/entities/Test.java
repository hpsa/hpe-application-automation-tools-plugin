package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class Test extends OctaneEntity {

    public static String TYPE = "test";
    public static String COLLECTION_NAME = "tests";

    public Test() {
        super(TYPE);
    }
}
