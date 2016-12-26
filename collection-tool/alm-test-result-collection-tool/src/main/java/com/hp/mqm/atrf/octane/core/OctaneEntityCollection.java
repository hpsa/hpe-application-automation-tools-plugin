package com.hp.mqm.atrf.octane.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by berkovir on 21/11/2016.
 */
public class OctaneEntityCollection {

    private int totalCount;

    private boolean exceedsTotalCount;

    private List<OctaneEntity> data = new ArrayList<>();

    public List<OctaneEntity> getData() {
        return data;
    }

    public void setData(List<OctaneEntity> data) {
        this.data = data;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isExceedsTotalCount() {
        return exceedsTotalCount;
    }

    public void setExceedsTotalCount(boolean exceedsTotalCount) {
        this.exceedsTotalCount = exceedsTotalCount;
    }
}
