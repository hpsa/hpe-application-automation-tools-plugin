// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.model;

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
}
