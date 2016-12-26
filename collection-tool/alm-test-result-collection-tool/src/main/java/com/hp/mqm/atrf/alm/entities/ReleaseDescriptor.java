package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntityDescriptor;
import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by berkovir on 22/11/2016.
 */
public class ReleaseDescriptor extends AlmEntityDescriptor {

    @Override
    public Class<? extends AlmEntity> getEntityClass() {
        return Release.class;
    }

    @Override
    public String getEntityTypeName() {
        return Release.TYPE;
    }

    @Override
    public String getCollectionName() {
        return Release.COLLECTION_NAME;
    }
}
