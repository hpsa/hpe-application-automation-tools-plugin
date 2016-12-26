package com.hp.mqm.atrf.alm.core;

import com.hp.mqm.atrf.core.entities.MapBasedObject;

/**
 * Created by berkovir on 21/11/2016.
 */
public class AlmEntity extends MapBasedObject {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static String FIELD_PARENT_ID = "parent-id";

    private String type;

    public AlmEntity(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


    public String getId() {
        return getString(FIELD_ID);
    }

    public String getName() {
        return getString(FIELD_NAME);
    }


    @Override
    public String toString() {
        return getType() + " #" + getId() + " " + getName();
    }
}

