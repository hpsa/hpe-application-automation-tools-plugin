package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.core.AlmEntityDescriptor;

/**
 * Created by berkovir on 22/11/2016.
 */
public class TestDescriptor extends AlmEntityDescriptor {

    @Override
    public Class<? extends AlmEntity> getEntityClass() {
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

    @Override
    public String getAlmRefUrlFormat() {
        return "%s://%s.%s.%s/TestPlanModule-00000000395028973?EntityType=ITest&EntityId=%s";
    }
}
