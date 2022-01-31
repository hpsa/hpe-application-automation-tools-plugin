package com.microfocus.application.automation.tools.octane.tests.junit.codeless;

import java.io.Serializable;

/**
 * @author Itay Karo on 02/01/2022
 */
public class CodelessResultParameter implements Serializable {

    private String id;

    private String name;

    private String type;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
