package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.core.AlmEntityDescriptor;

/**
 * Created by berkovir on 22/11/2016.
 */
public class TestConfigurationDescriptor extends AlmEntityDescriptor {

    @Override
    public Class<? extends AlmEntity> getEntityClass() {
        return TestConfiguration.class;
    }

    @Override
    public String getEntityTypeName() {
        return TestConfiguration.TYPE;
    }

    @Override
    public String getCollectionName() {
        return TestConfiguration.COLLECTION_NAME;
    }

}
