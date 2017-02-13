package com.hp.octane.plugins.jenkins.actions.dto;

/**
 * Created by kashbi on 25/09/2016.
 */
public class TestFramework {
    //    private String type = "";
    private String type = "list_node";
    private String logical_name = "list_node.testing_tool_type.uft";
    //    private String logical_name ;
    private String name = "UFT";
    private Integer index = 3;
    private Integer id = 1055;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogical_name() {
        return logical_name;
    }

    public void setLogical_name(String logical_name) {
        this.logical_name = logical_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
