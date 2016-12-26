package com.hp.mqm.atrf.octane.core;


/**
 * Created by berkovir on 22/11/2016.
 */
public abstract class OctaneEntityDescriptor {

    public enum Context{Workspace, Sharedspace};

    public abstract Class<? extends OctaneEntity> getEntityClass();

    public abstract String getEntityTypeName();

    public abstract String getCollectionName();

    public  Context getContext() {
        return Context.Workspace;
    }

}
