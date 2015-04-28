// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

final public class Field {

    private final Integer id;
    private final String value;
    private final int listId;
    private final String listName;
    private final String logicalListName;
    private final boolean extensible;
    private final boolean multiValue;

    public Field(Integer id, String value, int listId, String listName, String logicalListName, boolean extensible, boolean multiValue) {
        this.id = id;
        this.value = value;
        this.listId = listId;
        this.listName = listName;
        this.logicalListName = logicalListName;
        this.extensible = extensible;
        this.multiValue = multiValue;
    }

    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public int getListId() {
        return listId;
    }

    public String getListName() {
        return listName;
    }

    public String getLogicalListName() {
        return logicalListName;
    }

    public boolean isExtensible() {
        return extensible;
    }

    public boolean isMultiValue() {
        return multiValue;
    }
}
