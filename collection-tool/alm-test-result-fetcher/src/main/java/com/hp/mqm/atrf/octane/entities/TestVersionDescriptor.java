package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class TestVersionDescriptor extends OctaneEntityDescriptor {
    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return TestVersion.class;
    }

    @Override
    public String getEntityTypeName() {
        return TestVersion.TYPE;
    }

    @Override
    public String getCollectionName() {
        return TestVersion.COLLECTION_NAME;
    }
}
