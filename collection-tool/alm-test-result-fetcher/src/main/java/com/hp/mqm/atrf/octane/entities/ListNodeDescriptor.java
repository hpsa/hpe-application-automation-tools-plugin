package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;

/**
 * Created by berkovir on 05/12/2016.
 */
public class ListNodeDescriptor extends OctaneEntityDescriptor {
    @Override
    public Class<? extends OctaneEntity> getEntityClass() {
        return ListNode.class;
    }

    @Override
    public String getEntityTypeName() {
        return ListNode.TYPE;
    }

    @Override
    public String getCollectionName() {
        return ListNode.COLLECTION_NAME;
    }
}
