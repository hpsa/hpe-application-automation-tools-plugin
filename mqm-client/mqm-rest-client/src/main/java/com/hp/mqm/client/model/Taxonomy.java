// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

final public class Taxonomy {

    final private Long id;
    final private String name;
    final private Taxonomy root;

    public Taxonomy(Long id, String name, Taxonomy root) {
        this.id = id;
        this.name = name;
        this.root = root;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Taxonomy getRoot() {
        return root;
    }

    //TODO: remove equals - please do not use equals and hashCode - this is temporary solution
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Taxonomy taxonomy = (Taxonomy) o;

        if (id != null ? !id.equals(taxonomy.id) : taxonomy.id != null) {
            return false;
        }
        if (taxonomyTypeId != null ? !taxonomyTypeId.equals(taxonomy.taxonomyTypeId) : taxonomy.taxonomyTypeId != null) {
            return false;
        }
        if (name != null ? !name.equals(taxonomy.name) : taxonomy.name != null) {
            return false;
        }
        return !(taxonomyTypeName != null ? !taxonomyTypeName.equals(taxonomy.taxonomyTypeName) : taxonomy.taxonomyTypeName != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (taxonomyTypeId != null ? taxonomyTypeId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (taxonomyTypeName != null ? taxonomyTypeName.hashCode() : 0);
        return result;
    }
}
