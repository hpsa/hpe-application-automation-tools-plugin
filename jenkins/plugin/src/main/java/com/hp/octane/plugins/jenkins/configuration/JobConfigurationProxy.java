// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import hudson.model.AbstractProject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class JobConfigurationProxy {

    final private AbstractProject project;

    public JobConfigurationProxy(AbstractProject project) {
        this.project = project;
    }

    @JavaScriptMethod
    public JSONObject storeJobConfigurationOnServer(JSONObject jobConfiguration) throws IOException {
//        StructureItem structureItem = new StructureItem(project);
//        Writer w = new StringWriter();
//        new ModelBuilder().get(StructureItem.class).writeTo(structureItem, Flavor.JSON.createDataWriter(structureItem, w));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        JSONArray errors = new JSONArray();
//        errors.add("Unknown tag");

        JSONObject result = new JSONObject();
        result.put("errors", errors);
        result.put("config", jobConfiguration);
        return result;
    }

    @JavaScriptMethod
    public JSONObject loadJobConfigurationFromServer() throws IOException {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }
        JSONObject ret = new JSONObject();

        ret.put("buildTypeId", 1);

        JSONArray pipelines = new JSONArray();

        JSONObject pipeline1 = new JSONObject();
        pipeline1.put("id", 1);
        pipeline1.put("name", "First Pipeline");
        pipeline1.put("releaseId", "2");
        pipeline1.put("isRoot", true);
        pipeline1.put("noPush", false);
        JSONArray tags = new JSONArray();
        tags.add(tag(1, "Browser", 1, "Chrome"));
        tags.add(tag(2, "DB", 5, "MSSQL"));
        pipeline1.put("tags", tags);
        pipelines.add(pipeline1);

        JSONObject pipeline2 = new JSONObject();
        pipeline2.put("id", 2);
        pipeline2.put("name", "Second Pipeline");
        pipeline2.put("releaseId", "1");
        pipeline2.put("isRoot", false);
        pipeline2.put("noPush", false);
        JSONArray tags2 = new JSONArray();
        tags2.add(tag(1, "Browser", 1, "Firefox"));
        pipeline2.put("tags", tags2);
        pipelines.add(pipeline2);

        ret.put("pipelines", pipelines);

        JSONObject releases = new JSONObject();
        releases.put("1", "First");
        releases.put("2", "Second");
        ret.put("releases", releases);

        JSONArray availableTags = new JSONArray();
        availableTags.add(tagType(1, "Browser", Arrays.asList(
                tag(1, "Chrome"),
                tag(2, "Firefox"),
                tag(3, "IE"))));
        availableTags.add(tagType(2, "DB", Arrays.asList(
                tag(4, "Oracle"),
                tag(5, "MSSQL"))));
        availableTags.add(tagType(3, "System", Arrays.asList(
                tag(6, "Windows"),
                tag(7, "Linux"),
                tag(8, "HP-UX"))));
        availableTags.add(tagType(4, "Environment", Arrays.asList(
                tag(9, "Dev"),
                tag(10, "QA"),
                tag(11, "Staging"),
                tag(12, "Production"))));
        ret.put("availableTags", availableTags);

        return ret;
    }

    private JSONObject tag(int tagId, String value) {
        JSONObject tag = new JSONObject();
        tag.put("tagId", String.valueOf(tagId));
        tag.put("tagName", value);
        return tag;
    }

    private JSONObject tag(int typeId, String typeName, int tagId, String value) {
        JSONObject tag = tag(tagId, value);
        tag.put("tagTypeId", String.valueOf(typeId));
        tag.put("tagTypeName", typeName);
        return tag;
    }

    private void fillTagType(JSONObject target, int typeId, String typeName) {
        target.put("tagTypeId", String.valueOf(typeId));
        target.put("tagTypeName", typeName);
    }

    private JSONObject tagType(int typeId, String typeName, List<JSONObject> tags) {
        JSONObject result = new JSONObject();
        fillTagType(result, typeId, typeName);
        JSONArray values = new JSONArray();
        for (JSONObject tag: tags) {
            values.add(tag);
        }
        result.put("values", values);
        return result;
    }
}
