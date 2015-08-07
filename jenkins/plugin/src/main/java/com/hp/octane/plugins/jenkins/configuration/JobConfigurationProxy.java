// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.model.Field;
import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.octane.plugins.jenkins.Messages;
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
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JobConfigurationProxy {

    private final static Logger logger = Logger.getLogger(JobConfigurationProxy.class.getName());

    final private AbstractProject project;
    final private RetryModel retryModel;

    private static final String NOT_SPECIFIED = "-- Not specified --";

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
			long pipelineId = client.createPipeline(
					ServerIdentity.getIdentity(),
					project.getName(),
					pipelineObject.getString("name"),
					1001l, // pipelineObject.optLong("workspaceId"),
					pipelineObject.optLong("releaseId"),
                    toString(structureItem),
					toString(serverInfo)).getId();
            result.put("id", pipelineId);

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
			long pipelineId = pipelineObject.getLong("id");
			client.updatePipelineMetadata(ServerIdentity.getIdentity(), project.getName(), pipelineId, pipelineObject.getString("name"), pipelineObject.getLong("releaseId"));

            LinkedList<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
            JSONArray taxonomyTags = pipelineObject.getJSONArray("taxonomyTags");
            for (JSONObject jsonObject: toCollection(taxonomyTags)) {
				taxonomies.add(new Taxonomy(jsonObject.optLong("tagId"), jsonObject.getString("tagName"),
						new Taxonomy(jsonObject.optLong("tagTypeId"), jsonObject.getString("tagTypeName"), null)));
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

            // we can't add full fieldTags (we don't have metadata), pass what we have and let the client handle it
            JSONArray fieldsArray = new JSONArray();
            for (Field field: pipeline.getFields()) {
                JSONObject fieldObject = new JSONObject();
                fieldObject.put("id", field.getId());
                fieldObject.put("parentId", field.getParentId());
                fieldObject.put("name", field.getName());
                fieldObject.put("parentName", field.getParentName());
                fieldObject.put("parentLogicalName", field.getParentLogicalName());
                fieldsArray.add(fieldObject);
            }
            result.put("fields", fieldsArray);

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
//			List<FieldMetadata> fields = jobConfiguration.getFieldMetadata();
            for(Pipeline relatedPipeline: jobConfiguration.getRelatedPipelines()) {
                JSONObject pipeline = new JSONObject();
                pipeline.put("id", relatedPipeline.getId());
                pipeline.put("name", relatedPipeline.getName());
                pipeline.put("releaseId", relatedPipeline.getReleaseId());
//				pipeline.put("releaseName", relatedPipeline.getReleaseName());
				pipeline.put("isRoot", relatedPipeline.isRoot());

//				addTags(pipeline, relatedPipeline, fields);

                Map<String, List<FieldValue>> valuesByField = new HashMap<String, List<FieldValue>>();
//				for (FieldMetadata field : fields) {
//					valuesByField.put(field.getLogicalListName(), new LinkedList<FieldValue>());
//				}
                for (Field field: relatedPipeline.getFields()) {
                    valuesByField.get(field.getParentLogicalName()).add(new FieldValue(field.getId(), field.getName()));
                }
                JSONArray fieldTags = new JSONArray();
//				for (FieldMetadata field : fields) {
//					List<FieldValue> values = valuesByField.get(field.getLogicalListName());
//					JSONArray valuesArray = new JSONArray();
//					for (FieldValue value : values) {
//						valuesArray.add(fieldValue(value.getId(), value.getName()));
//					}
//					JSONObject fieldObject = new JSONObject();
//					fieldObject.put("logicalListName", field.getLogicalListName());
//					fieldObject.put("listId", field.getListId());
//					fieldObject.put("listName", field.getListName());
//					fieldObject.put("values", valuesArray);
//					fieldObject.put("extensible", field.isExtensible());
//					fieldObject.put("multiValue", field.isMultiValue());
//					fieldTags.add(fieldObject);
//				}
                pipeline.put("fieldTags", fieldTags);

                pipelines.add(pipeline);
            }

            ret.put("pipelines", pipelines);

            JSONArray allFields = new JSONArray();
//			for (FieldMetadata field : fields) {

//				JSONObject fieldObj = new JSONObject();
//				fieldObj.put("logicalListName", field.getLogicalListName());
//				fieldObj.put("listId", field.getListId());
//				fieldObj.put("listName", field.getListName());
//				fieldObj.put("extensible", field.isExtensible());
//				fieldObj.put("multiValue", field.isMultiValue());

//				allFields.add(fieldObj);
//			}
            ret.put("fields", allFields);

			//client.release();
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

    @JavaScriptMethod
    public JSONObject searchListItems(int listId, String term, boolean multiValue) {
        int defaultSize = 10;
        JSONObject ret = new JSONObject();

        MqmRestClient client;
        try {
            client = createClient();
        } catch (ClientException e) {
            logger.log(Level.WARNING, "MQM server connection failed", e);
            return error(e.getMessage());
        }
        try {

            PagedList<ListItem> listItemPagedList = client.queryListItems(listId, term, 1002l, 0, defaultSize);
            List<ListItem> listItems = listItemPagedList.getItems();
            boolean moreResults = listItemPagedList.getTotalCount() > listItems.size();

            JSONArray retArray = new JSONArray();
            if (moreResults) {
                retArray.add(createMoreResultsJson());
            }

            if (!multiValue) {
                String quotedTerm = Pattern.quote(term.toLowerCase());
                if (Pattern.matches(".*" + quotedTerm + ".*", NOT_SPECIFIED.toLowerCase())) {
                    JSONObject notSpecifiedItemJson = new JSONObject();
                    notSpecifiedItemJson.put("id", -1);
                    notSpecifiedItemJson.put("text", NOT_SPECIFIED);
                    retArray.add(notSpecifiedItemJson);
                }
            }

            for (ListItem item : listItems) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("id", item.getId());
                itemJson.put("text", item.getName());
                retArray.add(itemJson);
            }
            if (!multiValue) {
                //if exactly one item matches, we do not want to bother user with "new value" item
                if ((listItems.size() != 1) || (!listItems.get(0).getName().toLowerCase().equals(term.toLowerCase()))) {
                    retArray.add(createNewValueJson("0"));
                }
            }

            ret.put("results", retArray);
            client.release();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to retrieve list items", e);
            return error("Unable to retrieve job configuration");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to retrieve list items", e);
            return error("Unable to retrieve list items");
        } finally {
            try {
                client.release();
            } catch (Exception e) {
            }
        }
        return ret;
    }

    @JavaScriptMethod
    public JSONObject searchReleases(String term) {
        int defaultSize = 5;
        JSONObject ret = new JSONObject();

        MqmRestClient client;
        try {
            client = createClient();
        } catch (ClientException e) {
            logger.log(Level.WARNING, "MQM server connection failed", e);
            return error(e.getMessage());
        }
        try {
            PagedList<Release> releasePagedList = client.queryReleases(term, 1002l, 0, defaultSize);
            List<Release> releases = releasePagedList.getItems();
            boolean moreResults = releasePagedList.getTotalCount() > releases.size();

            JSONArray retArray = new JSONArray();
            if (moreResults) {
                retArray.add(createMoreResultsJson());
            }

            String quotedTerm = Pattern.quote(term.toLowerCase());
            if (Pattern.matches(".*" + quotedTerm + ".*", NOT_SPECIFIED.toLowerCase())) {
                JSONObject notSpecifiedItemJson = new JSONObject();
                notSpecifiedItemJson.put("id", -1);
                notSpecifiedItemJson.put("text", NOT_SPECIFIED);
                retArray.add(notSpecifiedItemJson);
            }

            for (Release release : releases) {
                JSONObject relJson = new JSONObject();
                relJson.put("id", release.getId());
                relJson.put("text", release.getName());
                retArray.add(relJson);
            }
            ret.put("results", retArray);

            client.release();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to retrieve releases", e);
            return error("Unable to retrieve job configuration");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to retrieve releases", e);
            return error("Unable to retrieve taxonomies");
        } finally {
            try {
                client.release();
            } catch (Exception e) {
            }
        }
        return ret;
    }

    @JavaScriptMethod
    public JSONObject searchTaxonomies(String term, JSONArray pipelineTaxonomies) {
        int defaultSize = 10;
        JSONObject ret = new JSONObject();

        MqmRestClient client;
        try {
            client = createClient();
        } catch (ClientException e) {
            logger.log(Level.WARNING, "MQM server connection failed", e);
            return error(e.getMessage());
        }
        try {

            //currently existing taxonomies on pipeline -> we need to show these options as disabled
            List<Long> pipelineTaxonomiesList = new LinkedList<Long>();
            for (int i = 0; i < pipelineTaxonomies.size(); i++) {
                JSONObject pipelineTaxonomy = pipelineTaxonomies.getJSONObject(i);
                if (pipelineTaxonomy.containsKey("tagId") && pipelineTaxonomy.containsKey("tagTypeId")) {   // we need to compare only taxonomies which already exist on server
                    pipelineTaxonomiesList.add(pipelineTaxonomy.getLong("tagId"));
                }
            }
            //retrieving taxonomies from server
            PagedList<Taxonomy> foundTaxonomies = client.queryTaxonomies(term, 1002l, 0, defaultSize);
            final List<Taxonomy> foundTaxonomiesList = foundTaxonomies.getItems();
            boolean moreResults = foundTaxonomies.getTotalCount() > foundTaxonomiesList.size();

            //creating map <TaxonomyCategoryID : Set<Taxonomy>>
            // for easier creating result JSON
            Map<Long, Set<Taxonomy>> taxonomyMap = new HashMap<Long, Set<Taxonomy>>();
            Map<Long, String> taxonomyCategories = new HashMap<Long, String>();
            for (Taxonomy taxonomy : foundTaxonomiesList) {
                if (taxonomy.getRoot() != null) {
                    if (taxonomyMap.containsKey(taxonomy.getRoot().getId())) {
                        taxonomyMap.get(taxonomy.getRoot().getId()).add(taxonomy);
                    } else {
                        taxonomyMap.put(taxonomy.getRoot().getId(), new LinkedHashSet<Taxonomy>(Arrays.asList(taxonomy)));
                        taxonomyCategories.put(taxonomy.getRoot().getId(), taxonomy.getRoot().getName());
                    }
                }
            }

            //writing result json
            JSONArray select2InputArray = new JSONArray();
            JSONObject allTags = new JSONObject();
            JSONObject tagTypesByName = new JSONObject();
            JSONObject tagTypes = new JSONObject();

            //show warning, that there are more results and user should filter more specific
            if (moreResults) {
                select2InputArray.add(createMoreResultsJson());
            }

            for (Entry<Long, Set<Taxonomy>> taxonomyType : taxonomyMap.entrySet()) {
                Long tagTypeId = taxonomyType.getKey();
                String tagTypeName = taxonomyCategories.get(tagTypeId);
                JSONArray childrenArray = new JSONArray();

                JSONObject optgroup = new JSONObject();
                optgroup.put("text", tagTypeName);

                //for tagTypesByName
                JSONObject tagTypeJson = new JSONObject();
                tagTypeJson.put("tagTypeId", tagTypeId);
                tagTypeJson.put("tagTypeName", tagTypeName);
                JSONArray tagTypeByNameValues = new JSONArray();

                for (Taxonomy tax : taxonomyType.getValue()) {
                    //creating input format for select2, so that this structure does not have to be refactored in javascript
                    JSONObject taxonomyJson = new JSONObject();
                    taxonomyJson.put("id", tax.getId());
                    taxonomyJson.put("text", tax.getName());
                    taxonomyJson.put("value", tax.getId());
                    if (pipelineTaxonomiesList.contains(tax.getId())) {
                        taxonomyJson.put("disabled", "disabled");
                    }
                    childrenArray.add(taxonomyJson);

                    //for allTags - adding tag into table of selected ones
                    JSONObject tagObject = new JSONObject();
                    tagObject.put("tagId", tax.getId());
                    tagObject.put("tagName", tax.getName());
                    tagObject.put("tagTypeId", tax.getRoot().getId());
                    tagObject.put("tagTypeName", tax.getRoot().getName());
                    allTags.put(String.valueOf(tax.getId()), tagObject);

                    //for tagTypesByName
                    JSONObject tagTypeByNameValue = new JSONObject();
                    tagTypeByNameValue.put("tagId", tax.getId());
                    tagTypeByNameValue.put("tagName", tax.getName());
                    tagTypeByNameValues.add(tagTypeByNameValue);
                }
                //New value.. for current type
                JSONObject newValueJson = createNewValueJson(Long.toString(tagTypeValue(tagTypeId)));
                childrenArray.add(newValueJson);

                optgroup.put("children", childrenArray);
                select2InputArray.add(optgroup);
                tagTypeJson.put("values", tagTypeByNameValues);
                tagTypesByName.put(tagTypeName, tagTypeJson);
                tagTypes.put(Long.toString(tagTypeId), tagTypeJson);
            }

            // New type... New value...
            JSONObject optgroup = new JSONObject();
            optgroup.put("text", "New type...");
            JSONObject newValueJson = createNewValueJson("newTagType");
            JSONArray childrenArray = new JSONArray();
            childrenArray.add(newValueJson);
            optgroup.put("children", childrenArray);
            select2InputArray.add(optgroup);

            ret.put("select2Input", select2InputArray);
            ret.put("allTags", allTags);
            ret.put("tagTypesByName", tagTypesByName);
            ret.put("tagTypes", tagTypes);
            ret.put("more", moreResults);

            client.release();
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to retrieve environments", e);
            return error("Unable to retrieve job configuration");
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to retrieve environments", e);
            return error("Unable to retrieve environments");
        } finally {
            try {
                client.release();
            } catch (Exception e) {
            }
        }
        return ret;
    }

    private JSONObject createMoreResultsJson() {
        JSONObject moreResultsJson = new JSONObject();
        moreResultsJson.put("id", "moreResultsFound");
        moreResultsJson.put("text", Messages.TooManyResults());
        moreResultsJson.put("warning", "true");
        moreResultsJson.put("disabled", "disabled");
        return moreResultsJson;
    }

    private JSONObject createNewValueJson(String id) {
        JSONObject newValueJson = new JSONObject();
        newValueJson.put("id", id);
        newValueJson.put("text", "New value...");
        newValueJson.put("newValue", "true");
        return newValueJson;
    }

    private long tagTypeValue(long n) {
        // mapping to ensure negative value (solve the "0" tag type ID)
        return -(n+1);
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

	private JSONObject tag(Long tagId, String value) {
        JSONObject tag = new JSONObject();
        tag.put("tagId", String.valueOf(tagId));
        tag.put("tagName", value);
        return tag;
    }

    private JSONObject tag(Taxonomy taxonomy) {
        JSONObject tag = tag(taxonomy.getId(), taxonomy.getName());
		tag.put("tagTypeId", String.valueOf(taxonomy.getRoot().getId()));
		tag.put("tagTypeName", taxonomy.getRoot().getName());
        return tag;
    }

	private void fillTagType(JSONObject target, long typeId, String typeName) {
        target.put("tagTypeId", String.valueOf(typeId));
        target.put("tagTypeName", typeName);
    }

    private JSONObject tagType(long typeId, String typeName) {
        JSONObject result = new JSONObject();
        fillTagType(result, typeId, typeName);
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
				configuration.sharedSpace,
                configuration.username,
                configuration.password);
        try {
			client.tryToConnectSharedSpace();
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
