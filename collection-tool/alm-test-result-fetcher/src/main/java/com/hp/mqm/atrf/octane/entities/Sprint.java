package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class Sprint extends OctaneEntity {

    public static String TYPE = "sprint";
    public static String COLLECTION_NAME = "sprints";

    public Sprint() {
        super(TYPE);
    }
}
