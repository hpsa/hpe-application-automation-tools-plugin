package com.hp.mqm.client.model;


import java.util.List;

public class ListField {

    private final String name;
    private List<ListItem> values;

    public ListField(final String name, final List<ListItem> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<ListItem> getValues() {
        return values;
    }
}
