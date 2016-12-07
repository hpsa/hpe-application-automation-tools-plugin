package com.hp.mqm.atrf.alm.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berkovir on 21/11/2016.
 */
public class AlmEntityCollection {

    private int total;

    private List<AlmEntity> entities = new ArrayList<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<AlmEntity> getEntities() {
        return entities;
    }
}
