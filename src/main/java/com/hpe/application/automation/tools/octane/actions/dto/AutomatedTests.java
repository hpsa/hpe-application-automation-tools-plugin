package com.hpe.application.automation.tools.octane.actions.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kashbi on 25/09/2016.
 */
public class AutomatedTests {

    private List<AutomatedTest> data = new ArrayList<>();

    public static AutomatedTests createWithTests(Collection<AutomatedTest> tests) {
        AutomatedTests result = new AutomatedTests();
        result.setData(new ArrayList<>(tests));
        return result;
    }

    public List<AutomatedTest> getData() {
        return data;
    }

    public void setData(List<AutomatedTest> data) {
        this.data = data;
    }
}
