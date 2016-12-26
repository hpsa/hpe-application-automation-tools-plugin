package com.hp.mqm.atrf.alm.entities;

import com.hp.mqm.atrf.alm.core.AlmEntity;
import com.hp.mqm.atrf.alm.core.AlmEntityDescriptor;

/**
 * Created by berkovir on 22/11/2016.
 */
public class RunDescriptor extends AlmEntityDescriptor {

    @Override
    public Class<? extends AlmEntity> getEntityClass() {
        return Run.class;
    }

    @Override
    public String getEntityTypeName() {
        return Run.TYPE;
    }

    @Override
    public String getCollectionName() {
        return Run.COLLECTION_NAME;
    }

    @Override
    public String getAlmRefUrlFormat() {
        //td://p1.radi.myd-vm02033.hpeswlab.net:8080/qcbin/TestRunsModule-00000000090859589?EntityType=IRun&EntityID=6;
        return "%s://%s.%s.%s/TestRunsModule-00000000090859589?EntityType=IRun&EntityID=%s";
    }
}
