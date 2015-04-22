// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

final public class Taxonomy {

    final private int id;
    final private int taxonomyTypeId;
    final private String name;
    final private String taxonomyTypeName;

    public Taxonomy(int id, int taxonomyTypeId, String name, String taxonomyTypeName) {
        this.id = id;
        this.taxonomyTypeId = taxonomyTypeId;
        this.name = name;
        this.taxonomyTypeName = taxonomyTypeName;
    }

    public int getId() {
        return id;
    }

    public int getTaxonomyTypeId() {
        return taxonomyTypeId;
    }

    public String getName() {
        return name;
    }

    public String getTaxonomyTypeName() {
        return taxonomyTypeName;
    }
}
