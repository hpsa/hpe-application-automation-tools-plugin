package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class Workspace extends OctaneEntity {

    public static String TYPE = "workspace";
    public static String COLLECTION_NAME = "workspaces";

    public Workspace() {
        super(TYPE);
    }
}
