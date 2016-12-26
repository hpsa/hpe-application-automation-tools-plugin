package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class TestVersion extends OctaneEntity {

    public static String TYPE = "test_version";
    public static String COLLECTION_NAME = "test_versions";

    public TestVersion() {
        super(TYPE);
    }
}
