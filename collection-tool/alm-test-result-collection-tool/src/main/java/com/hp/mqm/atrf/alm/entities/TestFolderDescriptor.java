package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.core.AlmEntityDescriptor;

/**
 * Created by berkovir on 22/11/2016.
 */
public class TestFolderDescriptor extends AlmEntityDescriptor {

    @Override
    public Class<? extends AlmEntity> getEntityClass() {
        return TestFolder.class;
    }

    @Override
    public String getEntityTypeName() {
        return TestFolder.TYPE;
    }

    @Override
    public String getCollectionName() {
        return TestFolder.COLLECTION_NAME;
    }

}
