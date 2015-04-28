// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.List;

final public class Pipeline {

    final private int id;
    final private String name;
    final private int releaseId;
    final private String releaseName;
    final private String rootJobName;
    final private List<Taxonomy> taxonomies;
    final private List<Field> fields;

    public Pipeline(int id, String name, int releaseId, String releaseName, String rootJobName,
                    List<Taxonomy> taxonomies, List<Field> fields) {
        this.id = id;
        this.name = name;
        this.releaseId = releaseId;
        this.releaseName = releaseName;
        this.rootJobName = rootJobName;
        this.taxonomies = taxonomies;
        this.fields = fields;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getRootJobName() {
        return rootJobName;
    }

    public List<Taxonomy> getTaxonomies() {
        return taxonomies;
    }

    public List<Field> getFields() {
        return fields;
    }
}
