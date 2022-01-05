package com.microfocus.application.automation.tools.octane.tests.junit.codeless;

import java.io.Serializable;
import java.util.List;

/**
 * @author Itay Karo on 02/01/2022
 */
public class CodelessResult implements Serializable {

    private List<List<CodelessResultUnit>> iterations;

    public List<List<CodelessResultUnit>> getIterations() {
        return iterations;
    }

    public void setIterations(List<List<CodelessResultUnit>> iterations) {
        this.iterations = iterations;
    }

}
