package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by bennun on 29/06/2016.
 */
public class TestFolder extends AlmEntity {

    public static String TYPE = "test-folder";
    public static String COLLECTION_NAME = "test-folders";

    public TestFolder() {
        super(TYPE);
    }

}
