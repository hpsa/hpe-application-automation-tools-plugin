// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import hudson.model.AbstractProject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.IOException;

public class JobConfigurationProxy {

    final private AbstractProject project;

    public JobConfigurationProxy(AbstractProject project) {
        this.project = project;
    }

    @JavaScriptMethod
    public JSONObject loadJobConfigurationFromServer() throws IOException {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }
        JSONObject ret = new JSONObject();

        JSONObject releases = new JSONObject();
        releases.put("1", "First");
        releases.put("2", "Second");
        ret.put("releases", releases);
        ret.put("release", "2");

        JSONObject tagTypes = new JSONObject();
        tagTypes.put("1", "Browser");
        tagTypes.put("2", "DB");
        tagTypes.put("3", "System");
        tagTypes.put("4", "Environment");
        ret.put("tagTypes", tagTypes);

        JSONObject allTags = new JSONObject();
        allTags.put("1", tag(1, "Chrome"));
        allTags.put("2", tag(1, "Firefox"));
        allTags.put("3", tag(1, "IE"));
        allTags.put("4", tag(2, "Oracle"));
        allTags.put("5", tag(2, "MSSQL"));
        allTags.put("6", tag(3, "Windows"));
        allTags.put("7", tag(3, "Linux"));
        allTags.put("8", tag(3, "HP-UX"));
        allTags.put("9", tag(4, "Dev"));
        allTags.put("10", tag(4, "QA"));
        allTags.put("11", tag(4, "Staging"));
        allTags.put("12", tag(4, "Production"));
        ret.put("allTags", allTags);

        JSONArray selectedTags = new JSONArray();
        selectedTags.add(1);
        selectedTags.add(4);
        ret.put("selectedTags", selectedTags);

        return ret;
    }

    private JSONObject tag(int type, String name) {
        JSONObject tag = new JSONObject();
        tag.put("type", String.valueOf(type));
        tag.put("name", name);
        return tag;
    }
}
