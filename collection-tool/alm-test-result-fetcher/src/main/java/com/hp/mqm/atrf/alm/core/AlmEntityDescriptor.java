package com.hp.mqm.atrf.alm.core;

/**
 * Created by berkovir on 22/11/2016.
 */
public abstract class AlmEntityDescriptor {

    public abstract Class<? extends AlmEntity> getEntityClass();

    public abstract String getEntityTypeName();

    public abstract String getCollectionName();

    public String getAlmRefUrlFormat(){
        return "";
    }
}
