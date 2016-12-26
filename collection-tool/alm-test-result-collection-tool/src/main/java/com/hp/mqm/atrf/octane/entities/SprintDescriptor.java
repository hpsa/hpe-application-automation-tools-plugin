package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class SprintDescriptor extends OctaneEntityDescriptor {

    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return Sprint.class;
    }

    @Override
    public String getEntityTypeName() {
        return Sprint.TYPE;
    }

    @Override
    public String getCollectionName() {
        return Sprint.COLLECTION_NAME;
    }
}
