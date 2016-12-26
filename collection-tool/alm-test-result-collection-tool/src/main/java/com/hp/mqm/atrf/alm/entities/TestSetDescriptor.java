package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntityDescriptor;
import com.hp.mqm.atrf.alm.core.AlmEntity;

/**
 * Created by berkovir on 22/11/2016.
 */
public class TestSetDescriptor extends AlmEntityDescriptor {

    @Override
    public Class<? extends AlmEntity> getEntityClass() {
        return TestSet.class;
    }

    @Override
    public String getEntityTypeName() {
        return TestSet.TYPE;
    }

    @Override
    public String getCollectionName() {
        return TestSet.COLLECTION_NAME;
    }
}
