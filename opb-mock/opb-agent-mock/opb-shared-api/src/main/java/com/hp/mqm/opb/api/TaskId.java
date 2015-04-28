// (C) Copyright 2003-2013 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.opb.api;

import java.io.Serializable;

public class TaskId implements Serializable {
    private int id;
    private String guid;

    public TaskId(int id, String guid) {
        this.id = id;
        this.guid = guid;
    }

    public int getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null) {
            return false;
        }

        if (getClass() != otherObject.getClass()) {
            return false;
        }

        TaskId other = (TaskId) otherObject;

        if (guid == null) {
            return id == other.id && other.guid == null;
        }

        return id == other.id && guid.equals(other.guid);

    }

    @Override
    public int hashCode() {
        return 31 * id + (guid != null ? guid.hashCode() : 0);
    }

    @Override
    public String toString() {
        return id + "_" + guid;
    }
}
