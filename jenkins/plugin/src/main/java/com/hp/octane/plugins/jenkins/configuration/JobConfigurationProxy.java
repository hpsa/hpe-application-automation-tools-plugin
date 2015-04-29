// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.model.Field;
import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TaxonomyType;
import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import hudson.ExtensionList;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobConfigurationProxy {

    private final static Logger logger = Logger.getLogger(JobConfigurationProxy.class.getName());

    final private AbstractProject project;
    final private RetryModel retryModel;

    public JobConfigurationProxy(AbstractProject project) {
        this.project = project;
        this.retryModel = getExtension(RetryModel.class);
    }

    @JavaScriptMethod
    public JSONObject createPipelineOnServer(JSONObject pipelineObject) throws IOException {
        JSONObject result = new JSONObject();

        StructureItem structureItem = new StructureItem(project);
        PluginActions.ServerInfo serverInfo = new PluginActions.ServerInfo();
        try {
            MqmRestClient client = createClient();
            int pipelineId = client.createPipeline(pipelineObject.getString("name"),
                    pipelineObject.getInt("releaseId"),
                    toString(structureItem),
                    toString(serverInfo));
            result.put("id", pipelineId);
            // TODO: janotav: fields not provided
//            addFieldTags(result, null);
            result.put("fieldTags", new JSONArray());

            client.release();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to create pipeline", e);
            return error("Unable to create pipeline");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to create pipeline", e);
            return error("Unable to create pipeline");
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Failed to create pipeline", e);
            return error(e.getMessage());
        }
        return result;
    }

    @JavaScriptMethod
    public JSONObject updatePipelineOnSever(JSONObject pipelineObject) throws IOException {
        JSONObject result = new JSONObject();

        try {
            MqmRestClient client = createClient();
            int pipelineId = pipelineObject.getInt("id");
            client.updatePipelineMetadata(pipelineId, pipelineObject.getString("name"), pipelineObject.getInt("releaseId"));

            LinkedList<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
            JSONArray taxonomyTags = pipelineObject.getJSONArray("taxonomyTags");
            for (JSONObject jsonObject: toCollection(taxonomyTags)) {
                Integer tagId = jsonObject.containsKey("tagId")? jsonObject.getInt("tagId"): null;
                Integer tagTypeId = jsonObject.containsKey("tagTypeId")? jsonObject.getInt("tagTypeId"): null;
                taxonomies.add(new Taxonomy(
                        tagId,
                        tagTypeId,
                        jsonObject.getString("tagName"),
                        jsonObject.getString("tagTypeName")));
            }

            LinkedList<Field> fields = new LinkedList<Field>();
            JSONArray fieldTags = pipelineObject.getJSONArray("fieldTags");
            for (JSONObject jsonObject: toCollection(fieldTags)) {
                for(JSONObject value: toCollection(jsonObject.getJSONArray("values"))) {
                    Integer id;
                    if (value.containsKey("id")) {
                        id = value.getInt("id");
                    } else {
                        id = null;
                    }
                    fields.add(new Field(id,
                            value.getString("name"),
                            jsonObject.getInt("listId"),
                            jsonObject.getString("listName"),
                            jsonObject.getString("logicalListName")));
                }
            }

            Pipeline pipeline = client.updatePipelineTags(ServerIdentity.getIdentity(), project.getName(), pipelineId, taxonomies, fields);
            addTaxonomyTags(result, pipeline);

            client.release();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to update pipeline", e);
            return error("Unable to update pipeline");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to update pipeline", e);
            return error("Unable to update pipeline");
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Failed to update pipeline", e);
            return error(e.getMessage());
        }

        return result;
    }

    @JavaScriptMethod
    public JSONObject loadJobConfigurationFromServer() throws IOException {
        MqmRestClient client;
        try {
            client = createClient();
        } catch (ClientException e) {
            logger.log(Level.WARNING, "MQM server connection failed", e);
            return error(e.getMessage());
        }

        JSONObject ret = new JSONObject();
        JSONArray pipelines = new JSONArray();

        try {
            JobConfiguration jobConfiguration = client.getJobConfiguration(ServerIdentity.getIdentity(), project.getName());
            ret.put("jobId", jobConfiguration.getJobId());
            String jobName = jobConfiguration.getJobName();
            boolean isRoot = jobConfiguration.isPipelineRoot();
            List<FieldMetadata> fields = jobConfiguration.getFieldMetadata();
            for(Pipeline relatedPipeline: jobConfiguration.getRelatedPipelines()) {
                JSONObject pipeline = new JSONObject();
                pipeline.put("id", relatedPipeline.getId());
                pipeline.put("name", relatedPipeline.getName());
                pipeline.put("releaseId", relatedPipeline.getReleaseId());
                pipeline.put("releaseName", relatedPipeline.getReleaseName());
                pipeline.put("isRoot", isRoot && relatedPipeline.getRootJobName().equals(jobName));

                addTags(pipeline, relatedPipeline, fields);

                Map<String, List<FieldValue>> valuesByField = new HashMap<String, List<FieldValue>>();
                for (FieldMetadata field: fields) {
                    valuesByField.put(field.getLogicalListName(), new LinkedList<FieldValue>());
                }
                for (Field field: relatedPipeline.getFields()) {
                    valuesByField.get(field.getParentLogicalName()).add(new FieldValue(field.getId(), field.getName()));
                }
                JSONArray fieldTags = new JSONArray();
                for (FieldMetadata field: fields) {
                    List<FieldValue> values = valuesByField.get(field.getLogicalListName());
                    JSONArray valuesArray = new JSONArray();
                    for (FieldValue value: values) {
                        valuesArray.add(fieldValue(value.getId(), value.getName()));
                    }
                    JSONObject fieldObject = new JSONObject();
                    fieldObject.put("logicalListName", field.getLogicalListName());
                    fieldObject.put("listId", field.getListId());
                    fieldObject.put("listName", field.getListName());
                    fieldObject.put("values", valuesArray);
                    fieldObject.put("extensible", field.isExtensible());
                    fieldObject.put("multiValue", field.isMultiValue());
                    fieldTags.add(fieldObject);
                }
                pipeline.put("fieldTags", fieldTags);

                pipelines.add(pipeline);
            }

            ret.put("pipelines", pipelines);

            JSONObject releases = new JSONObject();
            for (Release release: client.queryReleases(null, 0, 50).getItems()) {
                releases.put(String.valueOf(release.getId()), release.getName());
            }
            ret.put("releases", releases);

            JSONArray allTaxonomies = new JSONArray();
            MultiValueMap multiMap = new MultiValueMap();
            List<Taxonomy> taxonomies = client.queryTaxonomies(null, null, 0, 50).getItems();
            for (Taxonomy taxonomy: taxonomies) {
                multiMap.put(taxonomy.getTaxonomyTypeId(), tag(taxonomy.getId(), taxonomy.getName()));
            }
            List<TaxonomyType> taxonomyTypes = client.queryTaxonomyTypes(null, 0, 50).getItems();
            for (TaxonomyType taxonomyType: taxonomyTypes) {
                Collection<JSONObject> tags = multiMap.getCollection(taxonomyType.getId());
                allTaxonomies.add(tagType(taxonomyType.getId(), taxonomyType.getName(), tags == null ? Collections.<JSONObject>emptyList() : tags));
            }
            ret.put("taxonomies", allTaxonomies);

            JSONArray allFields = new JSONArray();
            for (FieldMetadata field: fields) {
                List<ListItem> items = client.queryListItems(field.getListId(), null, 0, 50).getItems();
                JSONArray array = new JSONArray();
                for (ListItem item: items) {
                    array.add(fieldValue(item.getId(), item.getName()));
                }
                JSONObject fieldObj = new JSONObject();
                fieldObj.put("logicalListName", field.getLogicalListName());
                fieldObj.put("values", array);
                allFields.add(fieldObj);
            }
            ret.put("fields", allFields);

            client.release();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to retrieve job configuration", e);
            return error("Unable to retrieve job configuration");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to retrieve job configuration", e);
            return error("Unable to retrieve job configuration");
        } finally {
            try {
                client.release();
            } catch (Exception e) {
                // TODO: janotav: introduce releaseQuietly
            }
        }

        return ret;
    }

    private void addTags(JSONObject result, Pipeline pipeline, List<FieldMetadata> fields) {
        addTaxonomyTags(result, pipeline);
        addFieldTags(result, pipeline, fields);
    }

    private void addTaxonomyTags(JSONObject result, Pipeline pipeline) {
        JSONArray pipelineTaxonomies = new JSONArray();
        for (Taxonomy taxonomy: pipeline.getTaxonomies()) {
            pipelineTaxonomies.add(tag(taxonomy));
        }
        result.put("taxonomyTags", pipelineTaxonomies);
    }

    private void addFieldTags(JSONObject result, Pipeline pipeline, List<FieldMetadata> fields) {
        JSONArray fieldTags = new JSONArray();
        Map<String, List<FieldValue>> valuesByField = new HashMap<String, List<FieldValue>>();
        for (FieldMetadata field : fields) {
            valuesByField.put(field.getLogicalListName(), new LinkedList<FieldValue>());
        }
        if (pipeline != null) {
            for (Field field : pipeline.getFields()) {
                valuesByField.get(field.getParentLogicalName()).add(new FieldValue(field.getId(), field.getName()));
            }
        }
        for (FieldMetadata field : fields) {
            List<FieldValue> values = valuesByField.get(field.getLogicalListName());
            JSONArray valuesArray = new JSONArray();
            for (FieldValue value : values) {
                valuesArray.add(fieldValue(value.getId(), value.getName()));
            }
            JSONObject fieldObject = new JSONObject();
            fieldObject.put("logicalListName", field.getLogicalListName());
            fieldObject.put("listId", field.getListId());
            fieldObject.put("listName", field.getListName());
            fieldObject.put("values", valuesArray);
            fieldObject.put("extensible", field.isExtensible());
            fieldObject.put("multiValue", field.isMultiValue());
            fieldTags.add(fieldObject);
        }
        result.put("fieldTags", fieldTags);
   }

    private JSONObject fieldValue(int id, String name) {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("name", name);
        return result;
    }

    private static Collection<JSONObject> toCollection(JSONArray array) {
        return (Collection<JSONObject>)array.subList(0, array.size());
    }

    private JSONObject tag(int tagId, String value) {
        JSONObject tag = new JSONObject();
        tag.put("tagId", String.valueOf(tagId));
        tag.put("tagName", value);
        return tag;
    }

    private JSONObject tag(Taxonomy taxonomy) {
        JSONObject tag = tag(taxonomy.getId(), taxonomy.getName());
        tag.put("tagTypeId", String.valueOf(taxonomy.getTaxonomyTypeId()));
        tag.put("tagTypeName", taxonomy.getTaxonomyTypeName());
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

    private JSONObject error(String message) {
        JSONObject result = new JSONObject();
        JSONArray errors = new JSONArray();
        errors.add(message);
        result.put("errors", errors);
        return result;
    }

    private MqmRestClient createClient() throws ClientException {
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        if (StringUtils.isEmpty(configuration.location)) {
            throw new ClientException("MQM server not configured");
        }

        if (retryModel.isQuietPeriod()) {
            throw new ClientException("MQM server not connected");
        }

        JenkinsMqmRestClientFactory clientFactory = getExtension(JenkinsMqmRestClientFactory.class);
        MqmRestClient client = clientFactory.create(
                configuration.location,
                configuration.domain,
                configuration.project,
                configuration.username,
                configuration.password);
        try {
            client.tryToConnectProject();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "MQM server connection failed", e);
            retryModel.failure();
            throw new ClientException("Connection to MQM server failed");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "MQM server connection failed", e);
            retryModel.failure();
            throw new ClientException("Connection to MQM server failed");
        }
        retryModel.success();
        return client;
    }

    private static <T> T getExtension(Class<T> clazz) {
        ExtensionList<T> items = Jenkins.getInstance().getExtensionList(clazz);
        Assert.assertEquals(1, items.size());
        return items.get(0);
    }

    private static <T> String toString(T bean) throws IOException {
        StringWriter writer = new StringWriter();
        Model<T> model = new ModelBuilder().get((Class<T>)bean.getClass());
        model.writeTo(bean, Flavor.JSON.createDataWriter(bean, writer));
        return writer.toString();
    }

    private static class FieldValue {

        private int id;
        private String name;

        private FieldValue(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class ClientException extends Exception {

        public ClientException(String message) {
            super(message);
        }

    }
}
