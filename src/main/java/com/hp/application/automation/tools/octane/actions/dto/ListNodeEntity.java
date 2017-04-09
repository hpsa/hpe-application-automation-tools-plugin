package com.hp.application.automation.tools.octane.actions.dto;

/**
 * Created by berkovir on 09/04/2017.
 */
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109"})
public class ListNodeEntity extends BaseRefEntity {

    public ListNodeEntity(){
        setType("list_node");
    }

    public static ListNodeEntity create(Long id) {
        ListNodeEntity entity = new ListNodeEntity();
        entity.setId(id);
        return entity;
    }

}
