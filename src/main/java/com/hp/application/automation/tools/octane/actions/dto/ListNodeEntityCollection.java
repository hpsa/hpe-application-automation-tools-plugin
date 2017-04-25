package com.hp.application.automation.tools.octane.actions.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Created by berkovir on 09/04/2017.
 */
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109"})
public class ListNodeEntityCollection {

    private List<ListNodeEntity> data;

    public static ListNodeEntityCollection create(ListNodeEntity item) {
        ListNodeEntityCollection coll = new ListNodeEntityCollection();
        coll.setData(Arrays.asList(item));
        return coll;
    }

    public List<ListNodeEntity> getData() {
        return data;
    }

    public void setData(List<ListNodeEntity> data) {
        this.data = data;
    }
}
