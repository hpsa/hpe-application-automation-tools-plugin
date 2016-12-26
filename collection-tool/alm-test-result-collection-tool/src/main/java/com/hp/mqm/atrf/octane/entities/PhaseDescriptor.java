package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class PhaseDescriptor extends OctaneEntityDescriptor {
    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return Phase.class;
    }

    @Override
    public String getEntityTypeName() {
        return Phase.TYPE;
    }

    @Override
    public String getCollectionName() {
        return Phase.COLLECTION_NAME;
    }
}
