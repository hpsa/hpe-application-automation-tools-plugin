// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TaxonomyType;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import hudson.ExtensionList;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        if (StringUtils.isEmpty(configuration.location)) {
            throw new RuntimeException("Not configured");
        }

        ExtensionList<JenkinsMqmRestClientFactory> items = Jenkins.getInstance().getExtensionList(JenkinsMqmRestClientFactory.class);
        Assert.assertEquals(1, items.size());
        JenkinsMqmRestClientFactory clientFactory = items.get(0);

        MqmRestClient client = clientFactory.create(
                configuration.location,
                configuration.domain,
                configuration.project,
                configuration.username,
                configuration.password);
        client.tryToConnectProject();

        JobConfiguration jobConfiguration = client.getJobConfiguration(ServerIdentity.getIdentity(), project.getName());

        JSONObject ret = new JSONObject();
        ret.put("jobId", jobConfiguration.getJobId());

        boolean isRoot = jobConfiguration.isPipelineRoot();
        String jobName = jobConfiguration.getJobName();

        JSONArray pipelines = new JSONArray();
        for(Pipeline relatedPipeline: jobConfiguration.getRelatedPipelines()) {
            JSONObject pipeline = new JSONObject();
            pipeline.put("id", relatedPipeline.getId());
            pipeline.put("name", relatedPipeline.getName());
            pipeline.put("releaseId", relatedPipeline.getReleaseId());
            pipeline.put("releaseName", relatedPipeline.getReleaseName());
            pipeline.put("isRoot", isRoot && jobName.equals(relatedPipeline.getRootJobName()));
            // TODO: janotav: flag not present
            pipeline.put("noPush", false);

            JSONArray taxonomyTags = new JSONArray();
            for(Taxonomy taxonomy: relatedPipeline.getTaxonomies()) {
                taxonomyTags.add(tag(taxonomy.getTaxonomyTypeId(), taxonomy.getTaxonomyTypeName(), taxonomy.getId(), taxonomy.getName()));
            }
            // TODO: janotav: mock data (real data not present yet)
            taxonomyTags.add(tag(1001, "Browser", 1001, "Chrome"));
            taxonomyTags.add(tag(1002, "DB", 1005, "MSSQL"));
            pipeline.put("taxonomyTags", taxonomyTags);
            pipelines.add(pipeline);
        }
        ret.put("pipelines", pipelines);

        JSONObject releases = new JSONObject();
        for (Release release: client.getReleases(null, 0, 50).getItems()) {
            releases.put(String.valueOf(release.getId()), release.getName());
        }
        ret.put("releases", releases);

        JSONArray allTaxonomies = new JSONArray();
        MultiValueMap multiMap = new MultiValueMap();
        List<Taxonomy> taxonomies = client.getTaxonomies(null, 0, 50).getItems();
        for (Taxonomy taxonomy: taxonomies) {
            multiMap.put(taxonomy.getTaxonomyTypeId(), tag(taxonomy.getId(), taxonomy.getName()));
        }
        List<TaxonomyType> taxonomyTypes = client.getTaxonomyTypes(null, 0, 50).getItems();
        for (TaxonomyType taxonomyType: taxonomyTypes) {
            Collection<JSONObject> tags = multiMap.getCollection(taxonomyType.getId());
            allTaxonomies.add(tagType(taxonomyType.getId(), taxonomyType.getName(), tags == null? Collections.<JSONObject>emptyList(): tags));
        }
        ret.put("taxonomies", allTaxonomies);

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

    private JSONObject tagType(int typeId, String typeName, Collection<JSONObject> tags) {
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
