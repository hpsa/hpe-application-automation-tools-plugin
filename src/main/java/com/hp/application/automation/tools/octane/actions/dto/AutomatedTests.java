package com.hp.application.automation.tools.octane.actions.dto;

import java.util.ArrayList;

/**
 * Created by kashbi on 25/09/2016.
 */
public class AutomatedTests {
    private ArrayList<AutomatedTest> data=new ArrayList<>();

    public ArrayList<AutomatedTest> getData() {
        return data;
    }

    public void setData(ArrayList<AutomatedTest> data) {
        this.data = data;
    }
}
