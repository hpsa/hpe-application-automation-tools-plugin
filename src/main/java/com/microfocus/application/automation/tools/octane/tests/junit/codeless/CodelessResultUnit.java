package com.microfocus.application.automation.tools.octane.tests.junit.codeless;

import java.io.Serializable;
import java.util.List;

/**
 * @author Itay Karo on 02/01/2022
 */
public class CodelessResultUnit implements Serializable {

    private String name;

    private List<CodelessResultParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CodelessResultParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<CodelessResultParameter> parameters) {
        this.parameters = parameters;
    }

}
