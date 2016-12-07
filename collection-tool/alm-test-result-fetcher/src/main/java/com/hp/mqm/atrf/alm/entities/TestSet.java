package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by bennun on 29/06/2016.
 */
public class TestSet extends AlmEntity {

    public static String TYPE = "test-set";
    public static String COLLECTION_NAME = "test-sets";

    public TestSet() {
        super(TYPE);
    }

}
