// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

final public class FieldMetadata {

    private final String listName;
    private final String name;
    private final String logicalListName;
    private final boolean extensible;
    private final boolean multiValue;
    private final int order;

    public FieldMetadata(String name, String listName, String logicalListName, boolean extensible, boolean multiValue, int order) {
        this.name = name;
        this.listName = listName;
        this.logicalListName = logicalListName;
        this.extensible = extensible;
        this.multiValue = multiValue;
        this.order = order;
    }

    public String getName() {
        return name;
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

    public int getOrder() {
        return order;
    }

    public boolean isValid() {
        return this.listName != null &&
                this.name != null &&
                this.logicalListName != null;
    }
}