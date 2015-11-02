// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.model;

import java.util.List;

final public class TestRun {

    final private Integer id;
    final private String name;
    final private Release release;

    final private List<Taxonomy> taxonomies;

    public TestRun(Integer id, String name, Release release, List<Taxonomy> taxonomies) {
        this.id = id;
        this.name = name;
        this.release = release;
        this.taxonomies = taxonomies;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Taxonomy> getTaxonomies() {
        return taxonomies;
    }

    public Release getRelease() {
        return release;
    }
}
