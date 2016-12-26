package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class ReleaseDescriptor extends OctaneEntityDescriptor {

    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
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
