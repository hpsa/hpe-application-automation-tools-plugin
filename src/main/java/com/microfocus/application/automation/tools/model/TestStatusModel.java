package com.microfocus.application.automation.tools.model;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class TestStatusModel {
    private boolean checked;
    private String statusName;

    @DataBoundConstructor
    public TestStatusModel(boolean checked, String statusName) {
        this.checked = checked;
        this.statusName = statusName;
    }

    public boolean isChecked() {
        return checked;
    }

    @DataBoundSetter
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getStatusName() {
        return statusName;
    }

    @DataBoundSetter
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
