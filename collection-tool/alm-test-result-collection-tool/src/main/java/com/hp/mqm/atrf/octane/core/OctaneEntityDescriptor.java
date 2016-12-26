package com.hp.mqm.atrf.octane.core;


/**
 * Created by berkovir on 22/11/2016.
 */
public abstract class OctaneEntityDescriptor {

    public abstract Class<? extends OctaneEntity> getEntityClass();

    public abstract String getEntityTypeName();

    public abstract String getCollectionName();

}
