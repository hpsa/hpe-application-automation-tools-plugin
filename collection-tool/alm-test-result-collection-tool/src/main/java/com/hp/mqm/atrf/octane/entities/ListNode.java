package com.hp.mqm.atrf.octane.entities;

import com.hp.mqm.atrf.octane.core.OctaneEntity;

/**
 * Created by berkovir on 05/12/2016.
 */
public class ListNode extends OctaneEntity {

    public static String TYPE = "list_node";
    public static String COLLECTION_NAME = "list_nodes";

    public ListNode() {
        super(TYPE);
    }
}
