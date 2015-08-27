package com.hp.mqm.client.model;

public class Workspace {
    final private long id;
    final private String name;

    public Workspace(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
