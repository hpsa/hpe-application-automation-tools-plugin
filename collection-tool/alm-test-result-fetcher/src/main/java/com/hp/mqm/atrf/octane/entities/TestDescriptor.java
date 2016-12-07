package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class TestDescriptor extends OctaneEntityDescriptor {
    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return Test.class;
    }

    @Override
    public String getEntityTypeName() {
        return Test.TYPE;
    }

    @Override
    public String getCollectionName() {
        return Test.COLLECTION_NAME;
    }
}
