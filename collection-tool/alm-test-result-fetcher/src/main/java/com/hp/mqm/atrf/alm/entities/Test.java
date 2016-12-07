package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by berkovir on 29/06/2016.
 */
public class Test extends AlmEntity {

    public static String TYPE = "test";
    public static String COLLECTION_NAME = "tests";

    public static String FIELD_SUBTYPE = "subtype-id";


    public Test() {
        super(TYPE);
    }

    public String getSubType(){
        return  getString(FIELD_SUBTYPE);
    }

    public String getDescription(){
        return  getString(FIELD_DESCRIPTION);
    }

}
