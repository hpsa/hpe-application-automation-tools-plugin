// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

final public class Field {

    private final Integer id;
    private final String name;
    private final int parentId;
    private final String parentName;
    private final String parentLogicalName;

    public Field(Integer id, String name, int parentId, String parentName, String parentLogicalName) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.parentName = parentName;
        this.parentLogicalName = parentLogicalName;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentLogicalName() {
        return parentLogicalName;
    }
}
