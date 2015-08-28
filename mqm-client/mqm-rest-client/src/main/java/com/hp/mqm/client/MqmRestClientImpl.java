package com.hp.mqm.client;

import com.hp.mqm.client.exception.ExceptionStackTraceParser;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.exception.ServerException;
import com.hp.mqm.client.internal.InputStreamSourceEntity;
import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListField;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TestResultStatus;
import com.hp.mqm.client.model.Workspace;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqmRestClientImpl extends AbstractMqmRestClient implements MqmRestClient {
	private static final Logger logger = Logger.getLogger(MqmRestClientImpl.class.getName());

    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	private static final String PREFIX_CI = "analytics/ci/";

	private static final String URI_TEST_RESULT_PUSH = PREFIX_CI + "test-results?skip-errors={0}";
	private static final String URI_TEST_RESULT_STATUS = PREFIX_CI + "test-results/{0}";
	private static final String URI_TEST_RESULT_LOG = URI_TEST_RESULT_STATUS + "/log";
	private static final String URI_JOB_CONFIGURATION = "analytics/ci/servers/{0}/jobs/{1}/configuration";
	private static final String URI_RELEASES = "releases";
	private static final String URI_WORKSPACES = "workspaces";
	private static final String URI_LIST_ITEMS = "list_nodes";
	private static final String URI_METADATA_FIELDS = "metadata/fields";
	private static final String URI_PUT_EVENTS = "analytics/ci/events";
    private static final String URI_TAXONOMY_NODES = "taxonomy_nodes";

	private static final String HEADER_ACCEPT = "Accept";

	private static final int DEFAULT_OFFSET = 0;
	private static final int DEFAULT_LIMIT = 100;

	/**
	 * Constructor for AbstractMqmRestClient.
	 *
	 * @param connectionConfig MQM connection configuration, Fields 'location', 'domain', 'project' and 'clientType' must not be null or empty.
	 */
	MqmRestClientImpl(MqmConnectionConfig connectionConfig) {
		super(connectionConfig);
	}

	@Override
	public long postTestResult(InputStreamSource inputStreamSource, boolean skipErrors) {
		return postTestResult(new InputStreamSourceEntity(inputStreamSource, ContentType.APPLICATION_XML), skipErrors);
	}

	@Override
	public long postTestResult(File testResultReport, boolean skipErrors) {
		return postTestResult(new FileEntity(testResultReport, ContentType.APPLICATION_XML), skipErrors);
	}

	@Override
	public TestResultStatus getTestResultStatus(long id) {
		HttpGet request = new HttpGet(createSharedSpaceInternalApiUri(URI_TEST_RESULT_STATUS, id));
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw createRequestException("Result status retrieval failed", response);
            }
            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = JSONObject.fromObject(json);
            Date until = null;
            if (jsonObject.has("until")) {
                try {
                    until = parseDatetime(jsonObject.getString("until"));
                } catch (ParseException e) {
                    throw new RequestException("Cannot obtain status", e);
                }
            }
            return new TestResultStatus(jsonObject.getString("status"), until);
        } catch (IOException e) {
            throw new RequestErrorException("Cannot obtain status.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
	}

	@Override
	public void getTestResultLog(long id, LogOutput output) {
        HttpGet request = new HttpGet(createSharedSpaceInternalApiUri(URI_TEST_RESULT_LOG, id));
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw createRequestException("Log retrieval failed", response);
            }
            output.setContentType(response.getFirstHeader("Content-type").getValue());
            InputStream is = response.getEntity().getContent();
            IOUtils.copy(is, output.getOutputStream());
            IOUtils.closeQuietly(is);
        } catch (IOException e) {
            throw new RequestErrorException("Cannot obtain log.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
	}

	@Override
	public JobConfiguration getJobConfiguration(String serverIdentity, String jobName) {
		HttpGet request = new HttpGet(createSharedSpaceInternalApiUri(URI_JOB_CONFIGURATION, serverIdentity, jobName));
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw createRequestException("Job configuration retrieval failed", response);
			}
			String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            try {
                JSONObject jsonObject = JSONObject.fromObject(json);
                List<Pipeline> pipelines = new LinkedList<Pipeline>();
                for (JSONObject relatedContext : getJSONObjectCollection(jsonObject, "data")) {
                    if ("pipeline".equals(relatedContext.getString("contextEntityType"))) {
                        pipelines.add(toPipeline(relatedContext));
                    } else {
                        logger.info("Context type '" + relatedContext.get("contextEntityType") + "' is not supported");
                    }
                }
                return new JobConfiguration(pipelines);
            } catch (JSONException e) {
                throw new RequestException("Failed to obtain job configuration", e);
            }
		} catch (IOException e) {
			throw new RequestErrorException("Cannot retrieve job configuration from MQM.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	@Override
	public Pipeline createPipeline(String serverIdentity, String projectName, String pipelineName, long workspaceId, Long releaseId, String structureJson, String serverJson) {
		HttpPost request = new HttpPost(createSharedSpaceInternalApiUri(URI_JOB_CONFIGURATION, serverIdentity, projectName));
		JSONObject pipelineObject = new JSONObject();
		pipelineObject.put("contextEntityType", "pipeline");
		pipelineObject.put("contextEntityName", pipelineName);
		pipelineObject.put("workspaceId", workspaceId);
		pipelineObject.put("releaseId", releaseId);
		pipelineObject.put("server", JSONObject.fromObject(serverJson));
		pipelineObject.put("structure", JSONObject.fromObject(structureJson));
		request.setEntity(new StringEntity(pipelineObject.toString(), ContentType.APPLICATION_JSON));
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw createRequestException("Pipeline creation failed", response);
			}
			String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return getPipelineByName(json, pipelineName, workspaceId);
		} catch (IOException e) {
			throw new RequestErrorException("Cannot create pipeline in MQM.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}


    @Override
    public Pipeline updatePipeline(String serverIdentity, String jobName, Pipeline pipeline) {
        HttpPut request = new HttpPut(createSharedSpaceInternalApiUri(URI_JOB_CONFIGURATION, serverIdentity, jobName));

        JSONObject pipelineObject = new JSONObject();
        pipelineObject.put("contextEntityType", "pipeline");
        pipelineObject.put("contextEntityId", pipeline.getId());
        pipelineObject.put("workspaceId", pipeline.getWorkspaceId());
        if (pipeline.getName() != null) {
            pipelineObject.put("contextEntityName", pipeline.getName());
        }
        if (pipeline.getReleaseId() != null) {
            if (pipeline.getReleaseId() == -1) {
                pipelineObject.put("releaseId", JSONNull.getInstance());
            } else  {
                pipelineObject.put("releaseId", pipeline.getReleaseId());
            }
        }
        if (pipeline.getTaxonomies() != null) {
            JSONArray taxonomies = taxonomiesArray(pipeline.getTaxonomies());
            pipelineObject.put("taxonomies", taxonomies);
        }

		if (pipeline.getFields() != null) {
			JSONObject listFields = listFieldsObject(pipeline.getFields());
			pipelineObject.put("listFields", listFields);
		}

        JSONArray data = new JSONArray();
        data.add(pipelineObject);
        JSONObject payload = new JSONObject();
        payload.put("data", data);

        request.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        request.setHeader(HEADER_ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw createRequestException("Pipeline update failed", response);
            }
            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return getPipelineById(json, pipeline.getId());
        } catch (IOException e) {
            throw new RequestErrorException("Cannot update pipeline.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private Date parseDatetime(String datetime) throws ParseException {
        return new SimpleDateFormat(DATETIME_FORMAT).parse(datetime);
    }

    private JSONArray taxonomiesArray(List<Taxonomy> taxonomies) {
        JSONArray ret = new JSONArray();
        for (Taxonomy taxonomy: taxonomies) {
            ret.add(fromTaxonomy(taxonomy));
        }
        return ret;
    }

	private JSONObject listFieldsObject(List<ListField> fields) {
		JSONObject ret = new JSONObject();
		for (ListField field : fields) {
			putListField(ret, field);
		}
		return ret;
	}

	private void putListField(JSONObject ret, ListField listField) {
		JSONArray valArray = new JSONArray();
		for (ListItem value : listField.getValues()) {
			JSONObject val = new JSONObject();
			if (value.getId() != null) {
				val.put("id", value.getId());
			} else {
				val.put("name", value.getName());
			}
			valArray.add(val);
		}
		ret.put(listField.getName(), valArray);
	}

//	private JSONObject listFieldsObject(List<Field> fields) {
//		JSONObject ret = new JSONObject();
//		for (Field field : fields) {
//			putListField(ret, field);
//		}
//		return ret;
//	}

//	private void putListField(JSONObject ret, Field field) {
//		JSONObject fieldJson = new JSONObject();
//		if (field.getId() != null) {
//			if (field.getId() != -1) {
//				fieldJson.put("id", field.getId());
//			} else {
//				//id of -1 value means unassigning the value => return empty array
//				ret.put(field.getParentName(), new JSONArray());
//				return;
//			}
//		} else {
//			fieldJson.put("name", field.getName());
//		}
//
//		if (ret.has(field.getParentName())) {
//			JSONArray tmp = ret.getJSONArray(field.getParentName());
//			tmp.add(fieldJson);
//		} else {
//			JSONArray tmp = new JSONArray();
//			tmp.add(fieldJson);
//			ret.put(field.getParentName(), tmp);
//		}
//	}

    private Taxonomy toTaxonomy(JSONObject t) {
        JSONObject parent = t.optJSONObject("parent");
		String name = t.has("name") ? t.getString("name") : null;
        if (parent != null) {
            return new Taxonomy(t.getLong("id"), name, toTaxonomy(parent));
        } else {
            return new Taxonomy(t.getLong("id"), name, null);
        }
    }

    private JSONObject fromTaxonomy(Taxonomy taxonomy) {
        JSONObject t = new JSONObject();
        if (taxonomy.getId() != null && taxonomy.getId() != 0) {
            t.put("id", taxonomy.getId());
        }
        if (taxonomy.getName() != null) {	//todo seems that name can be ommited in case that id exists
            t.put("name", taxonomy.getName());
        }
        if (taxonomy.getRoot() != null) {
            t.put("parent", fromTaxonomy(taxonomy.getRoot()));
        } else {
            t.put("parent", JSONNull.getInstance());
        }
        return t;
    }

    private Pipeline getPipelineByName(String json, String pipelineName, long workspaceId) {
        try {
            for (JSONObject item : getJSONObjectCollection(JSONObject.fromObject(json), "data")) {
                if (!"pipeline".equals(item.getString("contextEntityType"))) {
                    continue;
                }
                if (!item.getBoolean("pipelineRoot")) {
                    continue;
                }
                if (!pipelineName.equals(item.getString("contextEntityName"))) {
                    continue;
                }
                if (workspaceId != item.getLong("workspaceId")) {
                    continue;
                }
                return toPipeline(item);
            }
            throw new RequestException("Failed to obtain pipeline: item not found");
        } catch (JSONException e) {
            throw new RequestException("Failed to obtain pipeline", e);
        }
    }

    private Pipeline getPipelineById(String json, long pipelineId) {
        try {
            for (JSONObject item : getJSONObjectCollection(JSONObject.fromObject(json), "data")) {
                if (!"pipeline".equals(item.getString("contextEntityType"))) {
                    continue;
                }
                if (pipelineId != item.getLong("contextEntityId")) {
                    continue;
                }
                return toPipeline(item);
            }
            throw new RequestException("Failed to obtain pipeline: item not found");
        } catch (JSONException e) {
            throw new RequestException("Failed to obtain pipeline", e);
        }
    }

    private RequestException createRequestException(String message, HttpResponse response) {
        String description = null;
        String stackTrace = null;
        String errorCode = null;
        try {
            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = JSONObject.fromObject(json);
            if (jsonObject.has("error_code") && jsonObject.has("description")) {
                // exception response
                errorCode = jsonObject.getString("error_code");
                description = jsonObject.getString("description");
                // stack trace may not be present in production
                stackTrace = jsonObject.optString("stack_trace");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to determine failure message: ", e);
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "Unable to determine failure message: ", e);
        }

        ServerException cause = null;
        if (!StringUtils.isEmpty(stackTrace)) {
            try {
                Throwable parsedException = ExceptionStackTraceParser.parseException(stackTrace);
                cause = new ServerException("Exception thrown on server, see cause", parsedException);
            } catch (RuntimeException e) {
                // the parser is best-effort code, don't fail if anything goes wrong
                logger.log(Level.SEVERE, "Unable to parse server stacktrace: ", e);
            }
        }
        int statusCode = response.getStatusLine().getStatusCode();
        String reason = response.getStatusLine().getReasonPhrase();
        if (!StringUtils.isEmpty(errorCode)) {
            return new RequestException(message + "; error code: " + errorCode + "; description: " + description,
                    description, errorCode, statusCode, reason, cause);
        } else {
            return new RequestException(message + "; status code " + statusCode + "; reason " + reason,
                    description, errorCode, statusCode, reason, cause);
        }
    }

	private ListItem toListItem(JSONObject field) {
		Long id = null;
		String name = null;
		if (field.has("id")) {
			id = field.getLong("id");
		}
		if (field.has("name")) {
			name = field.getString("name");
		}
		return new ListItem(id, name, null);
	}

	private Pipeline toPipeline(JSONObject pipelineObject) {
		List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
		List<ListField> fields = new LinkedList<ListField>();

		if (pipelineObject.has("taxonomies")) {
			for (JSONObject taxonomy : getJSONObjectCollection(pipelineObject, "taxonomies")) {
                taxonomies.add(toTaxonomy(taxonomy));
			}
		}

		if (pipelineObject.has("listFields")) {
			JSONObject listFields = pipelineObject.getJSONObject("listFields");
			Iterator<?> keys = listFields.keys();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				if (listFields.get(key) instanceof JSONArray ) {
					List<ListItem> fieldValues = new LinkedList<ListItem>();
					for (JSONObject field : getJSONObjectCollection(listFields, key)) {
						fieldValues.add(toListItem(field));
					}
					fields.add(new ListField(key, fieldValues));
				}
			}
		}
		return new Pipeline(pipelineObject.getLong("contextEntityId"),
				pipelineObject.getString("contextEntityName"),
                pipelineObject.getBoolean("pipelineRoot"),
                pipelineObject.getLong("workspaceId"),
				pipelineObject.has("releaseId") && !pipelineObject.get("releaseId").equals(JSONNull.getInstance()) ? pipelineObject.getLong("releaseId") : null,
				pipelineObject.has("releaseName") && !pipelineObject.get("releaseName").equals(JSONNull.getInstance()) ? pipelineObject.getString("releaseName") : null,
				taxonomies, fields);
	}

	@Override
	public PagedList<Release> queryReleases(String name, long workspaceId, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		return getEntities(getEntityURI(URI_RELEASES, conditions, workspaceId, offset, limit, "name"), offset, new ReleaseEntityFactory());
	}

	@Override
	public Release getRelease(long releaseId, long workspaceId) {
		int offset = 0;
		int limit = 1;
		List<String> conditions = new LinkedList<String>();
		conditions.add(condition("id", String.valueOf(releaseId)));

		List<Release> releases = getEntities(getEntityURI(URI_RELEASES, conditions, workspaceId, offset, limit, null), offset, new ReleaseEntityFactory()).getItems();
		if (releases.size() != 1) {
			return null;
		}
		return releases.get(0);
	}

	@Override
	public PagedList<Workspace> queryWorkspaces(String name, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		return getEntities(getEntityURI(URI_WORKSPACES, conditions, null, offset, limit, "name"), offset, new WorkspaceEntityFactory());
	}

	@Override
	public List<Workspace> getWorkspaces(List<Long> workspaceIds) {
		if (workspaceIds == null || workspaceIds.size() == 0) {
			return new LinkedList<Workspace>();
		}
		if (workspaceIds.size() > DEFAULT_LIMIT) {
			throw new IllegalArgumentException("List of workspaceIds is too long. Only " + DEFAULT_LIMIT + " values are allowed.");
		}

		Set<Long> workspaceIdsSet = new LinkedHashSet<Long>(workspaceIds);
		StringBuilder conditionBuilder = new StringBuilder();
		for (Long workspaceId : workspaceIdsSet) {
			if (conditionBuilder.length() > 0) {
				conditionBuilder.append("||");
			}
			conditionBuilder.append("id=" + Long.toString(workspaceId));
		}
		return getEntities(getEntityURI(URI_WORKSPACES, Arrays.asList(conditionBuilder.toString()), null, DEFAULT_OFFSET, DEFAULT_LIMIT, null), DEFAULT_OFFSET, new WorkspaceEntityFactory()).getItems();
	}

	@Override
	public PagedList<Taxonomy> queryTaxonomyItems(Long taxonomyRootId, String name, long workspaceId, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		if (taxonomyRootId != null) {
			conditions.add(condition("taxonomy_root.id", String.valueOf(taxonomyRootId)));
		}
        conditions.add(condition("subtype", "taxonomy_item_node"));
		return getEntities(getEntityURI(URI_TAXONOMY_NODES, conditions, workspaceId, offset, limit, null), offset, new TaxonomyEntityFactory());
	}

	@Override
	public PagedList<Taxonomy> queryTaxonomyCategories(String name, long workspaceId, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
        conditions.add(condition("subtype", "taxonomy_category_node"));
		return getEntities(getEntityURI(URI_TAXONOMY_NODES, conditions, workspaceId, offset, limit, null), offset, new TaxonomyEntityFactory());
	}

    @Override
    public PagedList<Taxonomy> queryTaxonomies(String name, long workspaceId, int offset, int limit) {
        List<String> conditions = new LinkedList<String>();
        if (!StringUtils.isEmpty(name)) {
            conditions.add(condition("name", "*" + name + "*") + "||" + condition("taxonomy_root.name", "*" + name + "*"));
        }
        return getEntities(getEntityURI(URI_TAXONOMY_NODES, conditions, workspaceId, offset, limit, null), offset, new TaxonomyEntityFactory());
    }

	@Override
	public List<Taxonomy> getTaxonomies(List<Long> taxonomyIds, long workspaceId) {
		if (taxonomyIds == null || taxonomyIds.size() == 0) {
			return new LinkedList<Taxonomy>();
		}
		if (taxonomyIds.size() > DEFAULT_LIMIT) {
			throw new IllegalArgumentException("List of taxonomyIds is too long. Only " + DEFAULT_LIMIT + " values are allowed.");
		}

		Set<Long> taxonomyIdsSet = new LinkedHashSet<Long>(taxonomyIds);
		StringBuilder conditionBuilder = new StringBuilder();
		for (Long taxonomyId : taxonomyIdsSet) {
			if (conditionBuilder.length() > 0) {
				conditionBuilder.append("||");
			}
			conditionBuilder.append("id=" + Long.toString(taxonomyId));
		}
		return getEntities(getEntityURI(URI_TAXONOMY_NODES, Arrays.asList(conditionBuilder.toString()), workspaceId, DEFAULT_OFFSET, DEFAULT_LIMIT,
                null), DEFAULT_OFFSET, new TaxonomyEntityFactory()).getItems();
	}

    @Override
	public PagedList<ListItem> queryListItems(long listId, String name, long workspaceId, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		//todo uncomment this condition once cross filters work
//		conditions.add(condition("list_root.id", String.valueOf(listId)));
		return getEntities(getEntityURI(URI_LIST_ITEMS, conditions, workspaceId, offset, limit, null), offset, new ListItemEntityFactory());
	}
	@Override
	public List<ListItem> getListItems(List<Long> itemIds, long workspaceId) {
		if (itemIds == null || itemIds.size() == 0) {
			return new LinkedList<ListItem>();
		}
		if (itemIds.size() > DEFAULT_LIMIT) {
			throw new IllegalArgumentException("List of itemIds is too long. Only " + DEFAULT_LIMIT + " values are allowed.");
		}

		Set<Long> itemIdsSet = new LinkedHashSet<Long>(itemIds);
		StringBuilder conditionBuilder = new StringBuilder();
		for (Long itemId : itemIdsSet) {
			if (conditionBuilder.length() > 0) {
				conditionBuilder.append("||");
			}
			conditionBuilder.append("id=" + Long.toString(itemId));
		}
		return getEntities(getEntityURI(URI_LIST_ITEMS, Arrays.asList(conditionBuilder.toString()), workspaceId, DEFAULT_OFFSET, DEFAULT_LIMIT, null), DEFAULT_OFFSET, new ListItemEntityFactory()).getItems();
	}

	@Override
	public List<FieldMetadata> getFieldsMetadata(long workspaceId) {
		List<FieldMetadata> ret = new LinkedList<FieldMetadata>();

		List<String> conditions = new LinkedList<String>();
		conditions.add(condition("entity_name", "pipeline_node"));

		//loading all metadata fields
		PagedList<FieldMetadata> allFieldMetadata = getEntities(getEntityURI(URI_METADATA_FIELDS, conditions, workspaceId, DEFAULT_OFFSET, DEFAULT_LIMIT, null), DEFAULT_OFFSET, new FieldMetadataFactory());

		//filtering metadata fields to only values which we are interested in
		for (FieldMetadata fieldMetadata : allFieldMetadata.getItems()){
			if (fieldMetadata.isValid()) {
				ret.add(fieldMetadata);
			}
		}
		return ret;
	}

	private long postTestResult(HttpEntity entity, boolean skipErrors) {
		HttpPost request = new HttpPost(createSharedSpaceInternalApiUri(URI_TEST_RESULT_PUSH, skipErrors));
        request.setEntity(entity);
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
                throw createRequestException("Test result post failed", response);
			}
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONObject jsonObject = JSONObject.fromObject(json);
            return jsonObject.getLong("id");
        } catch (java.io.FileNotFoundException e) {
			throw new FileNotFoundException("Cannot find test result file.", e);
		} catch (IOException e) {
			throw new RequestErrorException("Cannot post test results to MQM.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

    @Override
	public boolean putEvents(String eventsJSON) {
		HttpPut request = new HttpPut(createSharedSpaceInternalApiUri(URI_PUT_EVENTS));
		request.setEntity(new StringEntity(eventsJSON, ContentType.APPLICATION_JSON));
		HttpResponse response = null;
		boolean result = true;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_TEMPORARY_REDIRECT) {
				// ad-hoc handling as requested by Jenkins Insight team
				HttpClientUtils.closeQuietly(response);
				login();
				response = execute(request);
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				logger.severe("put request failed while sending events: " + response.getStatusLine().getStatusCode());
				result = false;
			}
		} catch (Exception e) {
			logger.severe("put request failed while sending events: " + e.getClass().getName());
			result = false;
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
		return result;
	}

	private static class ListItemEntityFactory extends AbstractEntityFactory<ListItem> {

		@Override
		public ListItem doCreate(JSONObject entityObject) {
			JSONObject list_root = entityObject.optJSONObject("list_root");
			if (list_root != null) {
				return new ListItem(entityObject.getLong("id"), entityObject.getString("name"), doCreate(list_root));
			} else {
				return new ListItem(entityObject.getLong("id"), entityObject.getString("name"), null);
			}
		}
	}

	private static class TaxonomyEntityFactory extends AbstractEntityFactory<Taxonomy> {

		@Override
		public Taxonomy doCreate(JSONObject entityObject) {
            JSONObject taxonomy_root = entityObject.optJSONObject("category");
            if (taxonomy_root != null) {
                return new Taxonomy(entityObject.getLong("id"), entityObject.getString("name"), doCreate(taxonomy_root));
            } else {
                return new Taxonomy(entityObject.getLong("id"), entityObject.getString("name"), null);
            }
		}
	}

	private static class ReleaseEntityFactory extends AbstractEntityFactory<Release> {

		@Override
		public Release doCreate(JSONObject entityObject) {
			return new Release(entityObject.getLong("id"), entityObject.getString("name"));
		}
	}

	private static class WorkspaceEntityFactory extends AbstractEntityFactory<Workspace> {

		@Override
		public Workspace doCreate(JSONObject entityObject) {
			return new Workspace(entityObject.getLong("id"), entityObject.getString("name"));
		}
	}

	private static class FieldMetadataFactory extends AbstractEntityFactory<FieldMetadata> {

		@Override
		public FieldMetadata doCreate(JSONObject entityObject) {
			String name = null;
			String label = null;
			String logicalName = null;
			boolean multiple = false;
			boolean isExtensible = false;
			int order = 0;

			int mandatoryElementsFound = 0;

			if (entityObject.has("field_features")) {
				JSONArray fieldFeaturesArray = entityObject.getJSONArray("field_features");
				for (int i=0; i<fieldFeaturesArray.size(); i++) {
					JSONObject fieldFeature = fieldFeaturesArray.getJSONObject(i);
					if (fieldFeature.has("name") && fieldFeature.getString("name").equals("pipeline_tagging") && fieldFeature.has("extensibility") && fieldFeature.has("order")) {
						order = fieldFeature.getInt("order");
						isExtensible = fieldFeature.getBoolean("extensibility");
						mandatoryElementsFound++;
						break;
					}
				}
			}
			if (entityObject.has("name") && entityObject.has("label")) {
				name = entityObject.getString("name");
				label = entityObject.getString("label");
				mandatoryElementsFound++;
			}
			if (entityObject.has("field_type_data")) {
				JSONObject fieldTypeData = entityObject.getJSONObject("field_type_data");

				if (fieldTypeData.has("multiple") && fieldTypeData.has("target")) {
					multiple = fieldTypeData.getBoolean("multiple");

					JSONObject target = fieldTypeData.getJSONObject("target");
					if (target.has("logical_name")) {
						logicalName = target.getString("logical_name");
						mandatoryElementsFound++;
					}
				}
			}
			if (mandatoryElementsFound != 3) {
				return new FieldMetadata(null, null, null, false, false, 0);
			}

			return new FieldMetadata(name, label, logicalName, isExtensible, multiple, order);
		}
	}

	static abstract class AbstractEntityFactory<E> implements EntityFactory<E> {

		@Override
		public E create(String json) {
			JSONObject jsonObject = JSONObject.fromObject(json);
			return doCreate(jsonObject);
		}

		public abstract E doCreate(JSONObject entityObject);

	}
}
