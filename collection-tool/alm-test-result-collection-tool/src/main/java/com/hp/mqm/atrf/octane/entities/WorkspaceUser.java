package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class WorkspaceUser extends OctaneEntity {

    public static String TYPE = "workspace_user";
    public static String COLLECTION_NAME = "workspace_users";

    public WorkspaceUser() {
        super(TYPE);
    }
}
