package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by bennun on 29/06/2016.
 */
public class TestConfiguration extends AlmEntity {

    public static String TYPE = "test-config";
    public static String COLLECTION_NAME = "test-configs";

    public TestConfiguration() {
        super(TYPE);
    }

}
