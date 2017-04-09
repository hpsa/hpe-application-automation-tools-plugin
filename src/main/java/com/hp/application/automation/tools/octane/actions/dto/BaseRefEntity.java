package com.hp.application.automation.tools.octane.actions.dto;

/**
 * Created by berkovir on 09/04/2017.
 */
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109"})
public class BaseRefEntity {

    private String type;
    private Long id;

    public static BaseRefEntity create(String type, Long id) {
        BaseRefEntity baseRefEntity = new BaseRefEntity();
        baseRefEntity.setType(type);
        baseRefEntity.setId(id);
        return baseRefEntity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }
}
