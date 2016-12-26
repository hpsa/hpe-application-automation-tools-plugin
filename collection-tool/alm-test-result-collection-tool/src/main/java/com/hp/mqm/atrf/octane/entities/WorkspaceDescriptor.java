package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class WorkspaceDescriptor extends OctaneEntityDescriptor {
    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return Workspace.class;
    }

    @Override
    public String getEntityTypeName() {
        return Workspace.TYPE;
    }

    @Override
    public String getCollectionName() {
        return Workspace.COLLECTION_NAME;
    }

    @Override
    public Context getContext() {
        return Context.Sharedspace;
    }
}
