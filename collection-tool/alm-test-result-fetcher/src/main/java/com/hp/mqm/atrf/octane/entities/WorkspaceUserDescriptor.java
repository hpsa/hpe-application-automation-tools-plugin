package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class WorkspaceUserDescriptor extends OctaneEntityDescriptor {
    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return WorkspaceUser.class;
    }

    @Override
    public String getEntityTypeName() {
        return WorkspaceUser.TYPE;
    }

    @Override
    public String getCollectionName() {
        return WorkspaceUser.COLLECTION_NAME;
    }
}
