// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.model;

import java.util.List;

final public class PagedList<E> {

    final private List<E> items;
    final private int offset;
    final private int totalCount;

    public PagedList(List<E> items, int offset, int totalCount) {
        this.items = items;
        this.offset = offset;
        this.totalCount = totalCount;
    }

    public List<E> getItems() {
        return items;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
