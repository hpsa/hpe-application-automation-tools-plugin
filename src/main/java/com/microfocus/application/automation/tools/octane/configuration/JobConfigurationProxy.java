/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.configuration;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.entities.Entity;
import com.hp.octane.integrations.dto.entities.EntityConstants;
import com.hp.octane.integrations.dto.entities.ResponseEntityList;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.general.ListItem;
import com.hp.octane.integrations.dto.general.Taxonomy;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.pipelines.PipelineContext;
import com.hp.octane.integrations.dto.pipelines.PipelineContextList;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.services.entities.EntitiesService;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.Messages;
import com.microfocus.application.automation.tools.octane.model.ModelFactory;
import com.microfocus.application.automation.tools.octane.model.processors.parameters.ParameterProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import hudson.model.Job;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class is a proxy between JS UI code and server-side job configuration.
 */
public class JobConfigurationProxy {
	private final static Logger logger = LogManager.getLogger(JobConfigurationProxy.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	final private Job job;

	private static final String NOT_SPECIFIED = "-- Not specified --";

	JobConfigurationProxy(Job job) {
		this.job = job;
	}

	@JavaScriptMethod
	public JSONObject createPipelineOnServer(JSONObject pipelineObject) {
		JSONObject result = new JSONObject();

		PipelineNode pipelineNode = ModelFactory.createStructureItem(job);
		String instanceId = pipelineObject.getString("instanceId");
		CIServerInfo ciServerInfo = CIJenkinsServicesImpl.getJenkinsServerInfo();
		ciServerInfo.setInstanceId(instanceId);
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
		try {


			PipelineContext pipelineContext = dtoFactory.newDTO(PipelineContext.class)
					.setContextName(pipelineObject.getString("name"))
					.setWorkspace(pipelineObject.getLong("workspaceId"))
					.setReleaseId(pipelineObject.getLong("releaseId"))
					.setStructure(pipelineNode)
					.setServer(ciServerInfo);
			PipelineContext createdPipelineContext = octaneClient.getPipelineContextService().createPipeline(octaneClient.getInstanceId(), pipelineNode.getJobCiId(), pipelineContext);


			//WORKAROUND BEGIN
			//getting workspaceName - because the workspaceName is not returned from configuration API
			List<Entity> workspaces = getWorkspacesById(octaneClient, Collections.singletonList(pipelineContext.getWorkspaceId()));
			if (workspaces.size() != 1) {
				throw new ClientException("WorkspaceName could not be retrieved for workspaceId: " + pipelineContext.getWorkspaceId());
			}
			//WORKAROUND END

			JSONObject pipelineJSON = fromPipeline(createdPipelineContext, workspaces.get(0));
			enrichPipelineInstanceId(pipelineJSON, instanceId);

			//WORKAROUND BEGIN
			//all metadata have to be loaded in separate REST calls for this pipeline: releaseName, taxonomyNames and listFieldNames are not returned from configuration API
			enrichPipelineInternal(pipelineJSON, octaneClient);
			//WORKAROUND END
			result.put("pipeline", pipelineJSON);

			JSONArray fieldsMetadata = convertToJsonMetadata(getPipelineListNodeFieldsMetadata(octaneClient, createdPipelineContext.getWorkspaceId()));
			result.put("fieldsMetadata", fieldsMetadata);

		} catch (ClientException e) {
			logger.warn("Failed to create pipeline", e);
			return error(e.getMessage(), e.getLink());
		} catch (Exception e) {
			logger.warn("Failed to create pipeline", e);
			return error(e.getMessage());
		}
		return result;
	}

	@JavaScriptMethod
	public JSONObject updatePipelineOnSever(JSONObject pipelineObject) {
		JSONObject result = new JSONObject();

		String instanceId = pipelineObject.getString("instanceId");
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);

		try {
			long pipelineId = pipelineObject.getLong("id");

			//build taxonomies
			LinkedList<Taxonomy> taxonomies = new LinkedList<>();
			JSONArray taxonomyTags = pipelineObject.getJSONArray("taxonomyTags");
			for (JSONObject jsonObject : toCollection(taxonomyTags)) {
				taxonomies.add(dtoFactory.newDTO(Taxonomy.class)
						.setId(jsonObject.optLong("tagId"))
						.setName(jsonObject.getString("tagName"))
						.setParent(dtoFactory.newDTO(Taxonomy.class)
								.setId(jsonObject.optLong("tagTypeId"))
								.setName(jsonObject.getString("tagTypeName")))
				);
			}

			//build list fields
			Map<String, List<ListItem>> fields = new HashMap<>();
			JSONArray fieldTags = pipelineObject.getJSONArray("fieldTags");
			for (JSONObject jsonObject : toCollection(fieldTags)) {
				List<ListItem> assignedValues = new LinkedList<>();
				for (JSONObject value : toCollection(jsonObject.getJSONArray("values"))) {
					String id;
					if (value.containsKey("id")) {
						id = value.getString("id");
					} else {
						id = null;
					}
					assignedValues.add(dtoFactory.newDTO(ListItem.class).setId(id).setName(value.getString("name")));
				}
				fields.put(jsonObject.getString("name"), assignedValues);
			}

			final String jobCiId = JobProcessorFactory.getFlowProcessor(job).getTranslateJobName();

			PipelineContext pipelineContext = dtoFactory.newDTO(PipelineContext.class)
					.setContextEntityId(pipelineId)
					.setContextName(pipelineObject.getString("name"))
					.setWorkspace(pipelineObject.getLong("workspaceId"))
					.setReleaseId(pipelineObject.getLong("releaseId"))
					.setIgnoreTests(pipelineObject.getBoolean("ignoreTests"))
					.setTaxonomies(taxonomies)
					.setListFields(fields);

			PipelineContext pipeline = octaneClient.getPipelineContextService().updatePipeline(octaneClient.getInstanceId(), jobCiId, pipelineContext);

			//WORKAROUND BEGIN
			//getting workspaceName - because the workspaceName is not returned from configuration API
			List<Entity> workspaces = getWorkspacesById(octaneClient, Collections.singletonList(pipeline.getWorkspaceId()));
			if (workspaces.size() != 1) {
				throw new ClientException("WorkspaceName could not be retrieved for workspaceId: " + pipeline.getWorkspaceId());
			}
			//WORKAROUND END

			//TODO uncomment after getting pipelineContext
			JSONObject pipelineJSON = fromPipeline(pipeline, workspaces.get(0));
			enrichPipelineInstanceId(pipelineJSON, instanceId);

			//WORKAROUND BEGIN
			//all metadata have to be loaded in separate REST calls for this pipeline: releaseName, taxonomyNames and listFieldNames are not returned from configuration API
			enrichPipelineInternal(pipelineJSON, octaneClient);
			//WORKAROUND END
			result.put("pipeline", pipelineJSON);


			//Server might do partial sucess
			//So need to validate each item if it succedded or not
			//For now we add handling of duplicate pipeline name
			String originalName = pipelineObject.get("name").toString();
			String updatedName = pipelineJSON.get("name").toString();
			if (!originalName.equalsIgnoreCase(updatedName)) {
				JSONObject errorObj = new JSONObject();
				errorObj.put("message", "Failed to update pipeline name. Make sure not to enter the name of an existing pipeline.");
				result.put("error", errorObj);
			}
		} catch (ClientException e) {
			logger.warn("Failed to update pipeline", e);
			return error(e.getMessage(), e.getLink());
		} catch (Exception e) {
			logger.warn("Failed to update pipeline", e);
			return error("Unable to update pipeline");
		}

		return result;
	}

	@JavaScriptMethod
	public JSONObject deleteTests(JSONObject pipelineObject) {
		JSONObject result = new JSONObject();
		String instanceId = pipelineObject.getString("instanceId");
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
		try {
			long pipelineId = pipelineObject.getLong("id");
			long workspaceId = pipelineObject.getLong("workspaceId");
			octaneClient.getPipelineContextService().deleteTestsFromPipelineNodes(job.getName(), pipelineId, workspaceId);
			result.put("Test deletion was succeful", "");
		} catch (Exception e) {
			logger.warn("Failed to delete tests", e);
			return error("Unable to delete tests");
		}

		return result;
	}

	@JavaScriptMethod
	public JSONObject loadJobConfigurationFromServer(String instanceId) {
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);

		JSONObject ret = new JSONObject();
		JSONObject workspaces = new JSONObject();
		JSONArray fieldsMetadata = new JSONArray();
		try {
			boolean isUftJob = false;
			List<CIParameter> parameters = ParameterProcessors.getConfigs(job);
			if (parameters != null) {
				for (CIParameter parameter : parameters) {
					if (parameter != null && parameter.getName() != null && parameter.getName().equals("suiteId")) {
						isUftJob = true;
						break;
					}
				}
			}
			ret.put("isUftJob", isUftJob);

			final String jobCiId = JobProcessorFactory.getFlowProcessor(job).getTranslateJobName();
			PipelineContextList pipelineContextList = octaneClient.getPipelineContextService().getJobConfiguration(octaneClient.getInstanceId(), jobCiId);

			if (!pipelineContextList.getData().isEmpty()) {
				Map<Long, List<PipelineContext>> workspacesMap = pipelineContextList.buildWorkspace2PipelinesMap();
				//WORKAROUND BEGIN
				//getting workspaceName - because the workspaceName is not returned from configuration API
				Map<Long, String> relatedWorkspaces = new HashMap<>();
				List<Entity> workspaceList = getWorkspacesById(octaneClient, workspacesMap.keySet());
				for (Entity workspace : workspaceList) {
					relatedWorkspaces.put(Long.parseLong(workspace.getId()), workspace.getName());
				}
				//WORKAROUND END

				Map<Entity, List<PipelineContext>> sortedWorkspacesMap = new TreeMap<>(Comparator.comparing(Entity::getName));
				Comparator<PipelineContext> pipelineComparator = Comparator.comparing(PipelineContext::getContextEntityName);

				//create workspaces JSON Object
				for (Entry<Long, List<PipelineContext>> workspacePipelines : workspacesMap.entrySet()) {
					Entity relatedWorkspace = dtoFactory.newDTO(Entity.class);

					relatedWorkspace.setId(workspacePipelines.getKey().toString());
					relatedWorkspace.setName(relatedWorkspaces.get(workspacePipelines.getKey()));

					JSONObject relatedPipelinesJSON = new JSONObject();

					for (PipelineContext relatedPipeline : workspacePipelines.getValue()) {
						JSONObject pipelineJSON = fromPipeline(relatedPipeline, relatedWorkspace);
						enrichPipelineInstanceId(pipelineJSON, instanceId);
						relatedPipelinesJSON.put(String.valueOf(relatedPipeline.getContextEntityId()), pipelineJSON);
					}
					JSONObject workspaceJSON = new JSONObject();
					workspaceJSON.put("id", relatedWorkspace.getId());
					workspaceJSON.put("name", relatedWorkspace.getName());
					workspaceJSON.put("pipelines", relatedPipelinesJSON);
					workspaces.put(String.valueOf(relatedWorkspace.getId()), workspaceJSON);

					//inserting this workspace into sortedMap (sorted by workspaceName and by pipelineName, so that we can pick first workspace and its first pipeline as preselected values
					LinkedList<PipelineContext> workspacePipelinesList = new LinkedList<>(workspacePipelines.getValue());
					workspacePipelinesList.sort(pipelineComparator);
					sortedWorkspacesMap.put(relatedWorkspace, workspacePipelinesList);
				}

				//create currentPipeline JSON Object
				//currently the first pipeline in the first workspace is picked
				Entity preSelectedWorkspace = sortedWorkspacesMap.keySet().iterator().next();
				PipelineContext preSelectedPipeline = sortedWorkspacesMap.get(preSelectedWorkspace).get(0);
				JSONObject preSelectedPipelineJSON = fromPipeline(preSelectedPipeline, preSelectedWorkspace);
				enrichPipelineInstanceId(preSelectedPipelineJSON, instanceId);
				//WORKAROUND BEGIN
				//all metadata have to be loaded in separate REST calls for this pipeline: releaseName, taxonomyNames and listFieldNames are not returned from configuration API
				enrichPipelineInternal(preSelectedPipelineJSON, octaneClient);
				//WORKAROUND END
				ret.put("currentPipeline", preSelectedPipelineJSON);

				//retrieving metadata fields for preselected workspace
				fieldsMetadata = convertToJsonMetadata(getPipelineListNodeFieldsMetadata(octaneClient, Long.parseLong(preSelectedWorkspace.getId())));
			}

			ret.put("workspaces", workspaces);
			ret.put("fieldsMetadata", fieldsMetadata);

		} catch (Exception e) {
			logger.warn("Failed to retrieve job configuration", e);
			return error("Unable to retrieve job configuration");
		}

		return ret;
	}

	@JavaScriptMethod
	public JSONObject loadWorkspaceConfiguration(JSONObject pipelineJSON) {
		String instanceId = pipelineJSON.getString("instanceId");
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
		JSONObject ret = new JSONObject();

		try {
			JSONArray fieldsMetadata = convertToJsonMetadata(getPipelineListNodeFieldsMetadata(octaneClient, pipelineJSON.getLong("workspaceId")));
			ret.put("fieldsMetadata", fieldsMetadata);
			enrichPipelineInternal(pipelineJSON, octaneClient);
			ret.put("pipeline", pipelineJSON);
		} catch (Exception e) {
			logger.warn("Failed to retrieve metadata for workspace", e);
			return error("Unable to retrieve metadata for workspace");
		}

		return ret;
	}

	private static JSONObject fromPipeline(final PipelineContext pipeline, Entity relatedWorkspace) {
		JSONObject pipelineJSON = new JSONObject();
		pipelineJSON.put("id", pipeline.getContextEntityId());
		pipelineJSON.put("name", pipeline.getContextEntityName());
		pipelineJSON.put("releaseId", pipeline.getReleaseId() != null ? pipeline.getReleaseId() : -1);
		pipelineJSON.put("isRoot", pipeline.isPipelineRoot());
		pipelineJSON.put("workspaceId", relatedWorkspace.getId());
		pipelineJSON.put("workspaceName", relatedWorkspace.getName());
		pipelineJSON.put("ignoreTests", pipeline.getIgnoreTests());
		addTaxonomyTags(pipelineJSON, pipeline);
		addFields(pipelineJSON, pipeline);

		return pipelineJSON;
	}

	@JavaScriptMethod
	public JSONObject enrichPipeline(JSONObject pipelineJSON) {
		String instanceId = pipelineJSON.getString("instanceId");
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
		JSONObject ret = new JSONObject();
		try {
			enrichPipelineInternal(pipelineJSON, octaneClient);
			ret.put("pipeline", pipelineJSON);
		} catch (Exception e) {
			logger.warn("Failed to retrieve metadata for pipeline", e);
			return error("Unable to retrieve metadata for pipeline");
		}

		return ret;
	}

	private static void enrichPipelineInternal(JSONObject pipelineJSON, OctaneClient octaneClient) {
		enrichRelease(pipelineJSON, octaneClient);
		enrichTaxonomies(pipelineJSON, octaneClient);
		enrichFields(pipelineJSON, octaneClient);
	}

	private static void enrichPipelineInstanceId(JSONObject pipelineJSON, String instanceId) {
		pipelineJSON.put("instanceId", instanceId);
		pipelineJSON.put("instanceCaption", ConfigurationService.getSettings(instanceId).getCaption());
	}

	private static void enrichRelease(JSONObject pipeline, OctaneClient octaneClient) {
		long workspaceId = pipeline.getLong("workspaceId");
		if (pipeline.containsKey("releaseId") && pipeline.getLong("releaseId") != -1) {
			long releaseId = pipeline.getLong("releaseId");
			String releaseName = getReleasesById(octaneClient, Arrays.asList(releaseId), workspaceId).get(0).getName();
			pipeline.put("releaseName", releaseName);
		}
	}

	private static void enrichTaxonomies(JSONObject pipeline, OctaneClient octaneClient) {
		JSONArray ret = new JSONArray();
		if (pipeline.has("taxonomyTags")) {

			JSONArray taxonomyTags = pipeline.getJSONArray("taxonomyTags");
			List<Long> taxonomyIdsList = new LinkedList<>();
			for (int i = 0; i < taxonomyTags.size(); i++) {
				JSONObject taxonomy = taxonomyTags.getJSONObject(i);
				if (taxonomy.has("tagId")) {
					taxonomyIdsList.add(taxonomy.getLong("tagId"));
				}
			}
			List<Taxonomy> taxonomies = convertTaxonomies(getTaxonomiesById(octaneClient, taxonomyIdsList, pipeline.getLong("workspaceId")));
			for (Taxonomy tax : taxonomies) {
				ret.add(tag(tax));
			}
		}
		pipeline.put("taxonomyTags", ret);
	}

	private static void enrichFields(JSONObject pipeline, OctaneClient client) {
		JSONObject ret = new JSONObject();

		if (pipeline.has("fields")) {
			long workspaceId = pipeline.getLong("workspaceId");
			JSONObject pipelineFields = pipeline.getJSONObject("fields");
			Iterator<?> keys = pipelineFields.keys();
			//iteration over listFields
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (pipelineFields.get(key) instanceof JSONArray) {
					List<String> fieldTagsIdsList = new LinkedList<>();
					//getting all ids assigned to listField
					for (JSONObject singleField : toCollection(pipelineFields.getJSONArray(key))) {
						fieldTagsIdsList.add(singleField.getString("id"));
					}
					//retrieving names of assigned items
					if (fieldTagsIdsList.size() > 0) {
						List<Entity> enrichedFields = getListItemsById(client, fieldTagsIdsList, workspaceId);
						JSONArray values = new JSONArray();
						for (Entity item : enrichedFields) {
							JSONObject value = new JSONObject();
							value.put("id", item.getId());
							value.put("name", item.getName());
							values.add(value);
						}
						ret.put(key, values);
					}
				}
			}
		}
		pipeline.put("fields", ret);
	}

	@JavaScriptMethod
	public JSONObject searchListItems(String logicalListName, String term, String instanceId, long workspaceId, boolean multiValue, boolean extensible) {
		int defaultSize = 10;
		JSONObject ret = new JSONObject();
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
		try {
			ResponseEntityList listItemPagedList = queryListItems(octaneClient, logicalListName, term, workspaceId, defaultSize);
			List<Entity> listItems = listItemPagedList.getData();
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

			for (Entity item : listItems) {
				if (!toBeFiltered(item)) {
					JSONObject itemJson = new JSONObject();
					itemJson.put("id", item.getId());
					itemJson.put("text", item.getName());
					retArray.add(itemJson);
				}

			}
			// we shall use "if (extensible){}" on following line, but we do not have UI ready for the case: multiValue = true & extensible = true
			if (extensible && !multiValue) {
				//if exactly one item matches, we do not want to bother user with "new value" item
				if ((listItems.size() != 1) || (!listItems.get(0).getName().toLowerCase().equals(term.toLowerCase()))) {
					retArray.add(createNewValueJson("0"));
				}
			}


			ret.put("results", retArray);
		} catch (Exception e) {
			logger.warn("Failed to retrieve list items", e);
			return error("Unable to retrieve job configuration");
		}

		return ret;
	}

	private boolean toBeFiltered(Entity item) {
		return (item.getStringValue(EntityConstants.Base.LOGICAL_NAME_FIELD).equalsIgnoreCase("list_node.testing_tool_type.manual"));
	}

	@JavaScriptMethod
	public JSONObject searchReleases(String term, String instanceId, long workspaceId) {
		int defaultSize = 5;
		JSONObject ret = new JSONObject();
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);

		try {

			ResponseEntityList releasePagedList = queryReleasesByName(octaneClient, term, workspaceId, defaultSize);
			List<Entity> releases = releasePagedList.getData();
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

			for (Entity release : releases) {
				JSONObject relJson = new JSONObject();
				relJson.put("id", release.getId());
				relJson.put("text", release.getName());
				retArray.add(relJson);
			}
			ret.put("results", retArray);

		} catch (Exception e) {
			logger.warn("Failed to retrieve releases", e);
			return error("Unable to retrieve releases");
		}

		return ret;
	}

	@JavaScriptMethod
	public JSONObject searchWorkspaces(String term, String instanceId) {
		int defaultSize = 5;
		JSONObject ret = new JSONObject();
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);

		try {
			ResponseEntityList workspacePagedList = queryWorkspacesByName(octaneClient, term, defaultSize);
			List<Entity> workspaces = workspacePagedList.getData();
			boolean moreResults = workspacePagedList.getTotalCount() > workspaces.size();

			JSONArray retArray = new JSONArray();
			if (moreResults) {
				retArray.add(createMoreResultsJson());
			}

			for (Entity workspace : workspaces) {
				JSONObject relJson = new JSONObject();
				relJson.put("id", workspace.getId());
				relJson.put("text", workspace.getName());
				retArray.add(relJson);
			}
			ret.put("results", retArray);

		} catch (Exception e) {
			logger.warn("Failed to retrieve workspaces", e);
			return error("Unable to retrieve workspaces");
		}

		return ret;
	}

	@JavaScriptMethod
	public JSONObject searchSharedSpaces(String term) {
		JSONObject ret = new JSONObject();
		JSONArray retArray = new JSONArray();

		for (OctaneServerSettingsModel model : ConfigurationService.getAllSettings()) {
			if (StringUtils.isNotEmpty(term) && !model.getCaption().toLowerCase().contains(term.toLowerCase())) {
				continue;
			}
			JSONObject relJson = new JSONObject();
			relJson.put("id", model.getIdentity());
			relJson.put("text", model.getCaption());
			retArray.add(relJson);
		}
		ret.put("results", retArray);

		return ret;
	}

	@JavaScriptMethod
	public JSONObject searchTaxonomies(String term, String instanceId, long workspaceId, JSONArray pipelineTaxonomies) {
		int defaultSize = 20;
		JSONObject ret = new JSONObject();
		OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
		try {

			//currently existing taxonomies on pipeline -> we need to show these options as disabled
			List<Long> pipelineTaxonomiesList = new LinkedList<>();
			for (int i = 0; i < pipelineTaxonomies.size(); i++) {
				JSONObject pipelineTaxonomy = pipelineTaxonomies.getJSONObject(i);
				if (pipelineTaxonomy.containsKey("tagId") && pipelineTaxonomy.containsKey("tagTypeId")) {   // we need to compare only taxonomies which already exist on server
					pipelineTaxonomiesList.add(pipelineTaxonomy.getLong("tagId"));
				}
			}
			//retrieving taxonomies from server
			ResponseEntityList foundTaxonomies = queryTaxonomiesByName(octaneClient, term, workspaceId, defaultSize);
			final List<Entity> foundTaxonomiesList = foundTaxonomies.getData();
			boolean moreResults = foundTaxonomies.getTotalCount() > foundTaxonomiesList.size();

			//creating map <TaxonomyCategoryID : Set<Taxonomy>>
			// for easier creating result JSON
			Map<String, Set<Entity>> taxonomyMap = new HashMap<>();
			Map<String, String> taxonomyCategories = new HashMap<>();
			for (Entity taxonomy : foundTaxonomiesList) {
				Entity root = (Entity) taxonomy.getField(EntityConstants.Taxonomy.CATEGORY_NAME);
				if (root != null) {
					if (taxonomyMap.containsKey(root.getId())) {
						taxonomyMap.get(root.getId()).add(taxonomy);
					} else {
						taxonomyMap.put(root.getId(), new LinkedHashSet<>(Collections.singletonList(taxonomy)));
						taxonomyCategories.put(root.getId(), root.getName());
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

			for (Entry<String, Set<Entity>> taxonomyType : taxonomyMap.entrySet()) {
				String tagTypeId = taxonomyType.getKey();
				String tagTypeName = taxonomyCategories.get(tagTypeId);
				JSONArray childrenArray = new JSONArray();

				JSONObject optgroup = new JSONObject();
				optgroup.put("text", tagTypeName);

				//for tagTypesByName
				JSONObject tagTypeJson = new JSONObject();
				tagTypeJson.put("tagTypeId", tagTypeId);
				tagTypeJson.put("tagTypeName", tagTypeName);
				JSONArray tagTypeByNameValues = new JSONArray();

				for (Entity tax : taxonomyType.getValue()) {
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
					Entity root = (Entity) tax.getField(EntityConstants.Taxonomy.CATEGORY_NAME);
					JSONObject tagObject = new JSONObject();
					tagObject.put("tagId", tax.getId());
					tagObject.put("tagName", tax.getName());
					tagObject.put("tagTypeId", root.getId());
					tagObject.put("tagTypeName", root.getName());
					allTags.put(String.valueOf(tax.getId()), tagObject);

					//for tagTypesByName
					JSONObject tagTypeByNameValue = new JSONObject();
					tagTypeByNameValue.put("tagId", tax.getId());
					tagTypeByNameValue.put("tagName", tax.getName());
					tagTypeByNameValues.add(tagTypeByNameValue);
				}
				//New value.. for current type
				JSONObject newValueJson = createNewValueJson(Long.toString(tagTypeValue(Long.parseLong(tagTypeId))));
				childrenArray.add(newValueJson);

				optgroup.put("children", childrenArray);
				select2InputArray.add(optgroup);
				tagTypeJson.put("values", tagTypeByNameValues);
				tagTypesByName.put(tagTypeName, tagTypeJson);
				tagTypes.put(tagTypeId, tagTypeJson);
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

		} catch (Exception e) {
			logger.warn("Failed to retrieve environments", e);
			return error("Unable to retrieve environments");
		}

		return ret;
	}

	private static JSONObject createMoreResultsJson() {
		JSONObject moreResultsJson = new JSONObject();
		moreResultsJson.put("id", "moreResultsFound");
		moreResultsJson.put("text", Messages.TooManyResults());
		moreResultsJson.put("warning", "true");
		moreResultsJson.put("disabled", "disabled");
		return moreResultsJson;
	}

	private static JSONObject createNewValueJson(String id) {
		JSONObject newValueJson = new JSONObject();
		newValueJson.put("id", id);
		newValueJson.put("text", "New value...");
		newValueJson.put("newValue", "true");
		return newValueJson;
	}

	private static long tagTypeValue(long n) {
		// mapping to ensure negative value (solve the "0" tag type ID)
		return -(n + 1);
	}

	private static void addTaxonomyTags(JSONObject result, PipelineContext pipeline) {
		JSONArray pipelineTaxonomies = new JSONArray();
		for (Taxonomy taxonomy : pipeline.getTaxonomies()) {
			pipelineTaxonomies.add(tag(taxonomy));
		}
		result.put("taxonomyTags", pipelineTaxonomies);
	}

	private static void addFields(JSONObject result, PipelineContext pipeline) {
		JSONObject listFields = new JSONObject();

		for (Map.Entry<String, List<ListItem>> fieldEntry : pipeline.getListFields().entrySet()) {
			JSONArray assignedValuesArray = listFieldValues(fieldEntry.getValue());
			listFields.put(fieldEntry.getKey(), assignedValuesArray);
		}
		result.put("fields", listFields);
	}

	private static JSONArray listFieldValues(List<ListItem> listItems) {
		JSONArray ret = new JSONArray();

		for (ListItem item : listItems) {
			JSONObject value = new JSONObject();
			value.put("id", item.getId());
			if (item.getName() != null) {
				value.put("name", item.getName());
			}
			ret.add(value);
		}
		return ret;
	}

	private static Collection<JSONObject> toCollection(JSONArray array) {
		return (Collection<JSONObject>) array.subList(0, array.size());
	}

	private static JSONObject tag(Long tagId, String value) {
		JSONObject tag = new JSONObject();
		tag.put("tagId", String.valueOf(tagId));
		tag.put("tagName", value);
		return tag;
	}

	private static JSONObject tag(Taxonomy taxonomy) {
		JSONObject tag = tag(taxonomy.getId(), taxonomy.getName());
		if (taxonomy.getParent() != null) {
			tag.put("tagTypeId", String.valueOf(taxonomy.getParent().getId()));
			tag.put("tagTypeName", taxonomy.getParent().getName());
		}
		return tag;
	}

	private static JSONObject error(String message) {
		return error(message, null);
	}

	private static JSONObject error(String message, ExceptionLink exceptionLink) {
		JSONObject result = new JSONObject();
		JSONArray errors = new JSONArray();
		JSONObject error = new JSONObject();
		error.put("message", message);
		if (exceptionLink != null) {
			error.put("url", exceptionLink.getUrl());
			error.put("label", exceptionLink.getLabel());
		}
		errors.add(error);
		result.put("errors", errors);
		return result;
	}

	private static class ClientException extends Exception {

		private ExceptionLink link;

		ClientException(String message) {
			this(message, null);
		}

		ClientException(String message, ExceptionLink link) {
			super(message);
			this.link = link;
		}

		public ExceptionLink getLink() {
			return link;
		}
	}

	private static class ExceptionLink {

		private String url;
		private String label;

		ExceptionLink(String url, String label) {
			this.url = url;
			this.label = label;
		}

		public String getUrl() {
			return url;
		}

		public String getLabel() {
			return label;
		}
	}

	private static ResponseEntityList queryWorkspacesByName(OctaneClient octaneClient, String name, int limit) {
		return queryEntitiesByName(octaneClient, name, null, "workspaces", limit);
	}

	private static ResponseEntityList queryReleasesByName(OctaneClient octaneClient, String name, long workspaceId, int limit) {
		return queryEntitiesByName(octaneClient, name, workspaceId, "releases", limit);
	}

	private static ResponseEntityList queryTaxonomiesByName(OctaneClient octaneClient, String name, long workspaceId, int limit) {

		EntitiesService entityService = octaneClient.getEntitiesService();

		List<String> conditions = new LinkedList();
		conditions.add("!category={null}");
		if (!StringUtils.isEmpty(name)) {
			conditions.add(com.hp.octane.integrations.services.entities.QueryHelper.condition(EntityConstants.Base.NAME_FIELD, "*" + name + "*"));
			conditions.add("(" + com.hp.octane.integrations.services.entities.QueryHelper.condition("name", "*" + name + "*") + "||" + com.hp.octane.integrations.services.entities.QueryHelper.conditionRef("category", "name", "*" + name + "*") + ")");
		}

		String url = entityService.buildEntityUrl(workspaceId, "taxonomy_nodes", conditions, Arrays.asList(EntityConstants.Base.NAME_FIELD, EntityConstants.Taxonomy.CATEGORY_NAME), 0, limit, EntityConstants.Base.NAME_FIELD);
		ResponseEntityList result = entityService.getPagedEntities(url);
		return result;
	}

	private static ResponseEntityList queryEntitiesByName(OctaneClient octaneClient, String name, Long workspaceId, String collectionName, int limit) {
		EntitiesService entityService = octaneClient.getEntitiesService();

		List<String> conditions = new LinkedList();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(com.hp.octane.integrations.services.entities.QueryHelper.condition(EntityConstants.Base.NAME_FIELD, "*" + name + "*"));
		}
		String url = entityService.buildEntityUrl(workspaceId, collectionName, conditions, Arrays.asList(EntityConstants.Base.NAME_FIELD), 0, limit, EntityConstants.Base.NAME_FIELD);
		ResponseEntityList result = entityService.getPagedEntities(url);
		return result;
	}

	private static ResponseEntityList queryListItems(OctaneClient octaneClient, String logicalListName, String name, long workspaceId, int limit) {
		int myLimit = limit;
		EntitiesService entityService = octaneClient.getEntitiesService();
		List<String> conditions = new LinkedList();
		if (!StringUtils.isEmpty(name)) {
			//list node are not filterable by name
			//conditions.add(com.hp.octane.integrations.services.entities.QueryHelper.condition(EntityConstants.Base.NAME_FIELD, "*" + name + "*"));
			myLimit = 100;
		}
		if (!StringUtils.isEmpty(logicalListName)) {
			conditions.add(com.hp.octane.integrations.services.entities.QueryHelper.conditionRef("list_root", EntityConstants.Base.LOGICAL_NAME_FIELD, logicalListName));
		}

		String url = entityService.buildEntityUrl(workspaceId, "list_nodes", conditions, null, 0, myLimit, null);
		ResponseEntityList result = entityService.getPagedEntities(url);
		ResponseEntityList myResult = result;
		if (!StringUtils.isEmpty(name)) {
			List<Entity> data = result.getData().stream().filter(l -> l.getName().toLowerCase().contains(name.toLowerCase())).limit(limit).collect(Collectors.toList());
			myResult = (ResponseEntityList) dtoFactory.newDTO(ResponseEntityList.class).setData(data);
		}
		return myResult;
	}

	private static List<Entity> getWorkspacesById(OctaneClient client, Collection<?> itemIds) {
		return getEntitiesById(client, null, "workspaces", itemIds);
	}

	private static List<Entity> getListItemsById(OctaneClient client, Collection<?> itemIds, long workspaceId) {
		return getEntitiesById(client, workspaceId, EntityConstants.Lists.COLLECTION_NAME, itemIds);
	}

	private static List<Entity> getReleasesById(OctaneClient client, Collection<?> itemIds, long workspaceId) {
		return getEntitiesById(client, workspaceId, EntityConstants.Release.COLLECTION_NAME, itemIds);
	}

	private static List<Entity> getTaxonomiesById(OctaneClient client, Collection<?> itemIds, long workspaceId) {
		return getEntitiesById(client, workspaceId, EntityConstants.Taxonomy.COLLECTION_NAME, itemIds);
	}

	private static List<Taxonomy> convertTaxonomies(List<Entity> entities) {
		List<Taxonomy> taxonomies = new ArrayList<>();
		for (Entity entity : entities) {
			Taxonomy taxonomy = dtoFactory.newDTO(Taxonomy.class);
			taxonomy.setId(Long.parseLong(entity.getId())).setName(entity.getName());
			if (entity.containsField(EntityConstants.Taxonomy.CATEGORY_NAME)) {
				Taxonomy parent = dtoFactory.newDTO(Taxonomy.class);
				parent.setId(Long.parseLong(entity.getId())).setName(entity.getName());
				taxonomy.setParent(parent);
			}
			taxonomies.add(taxonomy);
		}
		return taxonomies;
	}

	private static List<Entity> getEntitiesById(OctaneClient octaneClient, Long workspaceId, String collectionName, Collection<?> itemIds) {
		return octaneClient.getEntitiesService().getEntitiesByIds(workspaceId, collectionName, itemIds);
	}

	private static List<Entity> getPipelineListNodeFieldsMetadata(OctaneClient octaneClient, long workspaceId) {
		List<String> conditions = new LinkedList<>();
		conditions.add(com.hp.octane.integrations.services.entities.QueryHelper.condition("entity_name", "pipeline_node"));
		conditions.add(com.hp.octane.integrations.services.entities.QueryHelper.conditionIn("name", Arrays.asList("test_tool_type", "test_level", "test_type", "test_framework"), false));

		EntitiesService entityService = octaneClient.getEntitiesService();
		String url = entityService.buildEntityUrl(workspaceId, "metadata/fields", conditions, null, 0, null, null);
		ResponseEntityList list = entityService.getPagedEntities(url);
		return list.getData();
	}

	private static JSONArray convertToJsonMetadata(List<Entity> listNodeFieldMetadataList) {
		JSONArray fieldMetadataArray = new JSONArray();
		for (Entity entity : listNodeFieldMetadataList) {
			Map<String, Object> fieldTypeData = (Map<String, Object>) entity.getField("field_type_data");
			List targets = (List) fieldTypeData.get("targets");
			Map<String, String> listNodeTarget = (Map<String, String>) targets.get(0);

			//find pipeline_tagging feature
			List<Map<String, Object>> fieldFeatures = (List<Map<String, Object>>) entity.getField("field_features");
			Map<String, Object> pipelineTaggingFeature = null;
			for (Map<String, Object> feature : fieldFeatures) {
				if (feature.get("name").equals("pipeline_tagging")) {
					pipelineTaggingFeature = feature;
				}
			}

			JSONObject fieldMetadataJSON = new JSONObject();
			fieldMetadataJSON.put("name", entity.getName());
			fieldMetadataJSON.put("listName", entity.getStringValue("label"));
			fieldMetadataJSON.put("logicalListName", listNodeTarget.get("logical_name"));
			fieldMetadataJSON.put("extensible", pipelineTaggingFeature.get("extensibility"));
			fieldMetadataJSON.put("multiValue", fieldTypeData.get("multiple"));
			fieldMetadataJSON.put("order", pipelineTaggingFeature.get("order"));
			fieldMetadataArray.add(fieldMetadataJSON);
		}
		return fieldMetadataArray;
	}
}
