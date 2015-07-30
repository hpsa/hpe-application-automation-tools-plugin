package com.hp.mqm.client;

import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.internal.InputStreamSourceEntity;
import com.hp.mqm.client.model.Field;
import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TaxonomyType;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MqmRestClientImpl extends AbstractMqmRestClient implements MqmRestClient {
	private static final Logger logger = Logger.getLogger(MqmRestClientImpl.class.getName());

	private static final String WORKSPACE_FRAGMENT = "workspace-id={workspace}";
	private static final String PAGING_FRAGMENT = "offset={offset}&limit={limit}";

	//TODO: v2 will be removed after change on the server
	private static final String URI_PUSH_TEST_RESULT_PUSH = "test-results/v2";
	private static final String URI_JOB_CONFIGURATION = "analytics/ci/servers/{0}/jobs/{1}/configuration";
	private static final String URI_RELEASES = "releases?" + PAGING_FRAGMENT;
	private static final String URI_LIST_ITEMS = "list_nodes?" + PAGING_FRAGMENT;
	private static final String URI_TAXONOMIES = "taxonomies?" + WORKSPACE_FRAGMENT + "&" + PAGING_FRAGMENT;
	private static final String URI_TAXONOMY_TYPES = "taxonomy-types?" + WORKSPACE_FRAGMENT + "&" + PAGING_FRAGMENT;
	private static final String URI_PIPELINES = "cia/pipelines?fetchStructure=false";
	private static final String URI_PIPELINES_TAGS = "cia/pipelines/{server}/jobconfig/{job}?" + WORKSPACE_FRAGMENT;
	private static final String URI_PUT_EVENTS = "analytics/ci/events";

	private static final String HEADER_ACCEPT = "Accept";

	/**
	 * Constructor for AbstractMqmRestClient.
	 *
	 * @param connectionConfig MQM connection configuration, Fields 'location', 'domain', 'project' and 'clientType' must not be null or empty.
	 */
	MqmRestClientImpl(MqmConnectionConfig connectionConfig) {
		super(connectionConfig);
	}

	@Override
	public void postTestResult(InputStreamSource inputStreamSource) {
		HttpPost request = new HttpPost(createProjectApiUri(URI_PUSH_TEST_RESULT_PUSH));
		request.setEntity(new InputStreamSourceEntity(inputStreamSource, ContentType.APPLICATION_XML));
		postTestResult(request);
	}

	@Override
	public void postTestResult(File testResultReport) {
		HttpPost request = new HttpPost(createProjectApiUri(URI_PUSH_TEST_RESULT_PUSH));
		request.setEntity(new FileEntity(testResultReport, ContentType.APPLICATION_XML));
		postTestResult(request);
	}

	@Override
	public JobConfiguration getJobConfiguration(String serverIdentity, String jobName) {
		HttpGet request = new HttpGet(createSharedSpaceInternalApiUri(URI_JOB_CONFIGURATION, serverIdentity, jobName));
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RequestException("Job configuration retrieval failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
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
	public long createPipeline(String serverIdentity, String projectName, String pipelineName, long workspaceId, long releaseId, String structureJson, String serverJson) {
		HttpPost request = new HttpPost(createSharedSpaceInternalApiUri(URI_JOB_CONFIGURATION, serverIdentity, projectName));
		JSONObject pipelineObject = new JSONObject();
		pipelineObject.put("contextEntityType", "pipeline");
		pipelineObject.put("contextEntityName", pipelineName);
		pipelineObject.put("workspaceId", workspaceId <= 0 ? 1002L : workspaceId);
		pipelineObject.put("releaseId", releaseId <= 0 ? null : releaseId);
		pipelineObject.put("server", JSONObject.fromObject(serverJson));
		pipelineObject.put("structure", JSONObject.fromObject(structureJson));
		request.setEntity(new StringEntity(pipelineObject.toString(), ContentType.APPLICATION_JSON));
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw new RequestException("Pipeline creation failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
			}
			String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
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
					return item.getLong("contextEntityId");
				}
				throw new RequestException("Failed to obtain pipeline id: created item not found");
			} catch (JSONException e) {
				throw new RequestException("Failed to obtain pipeline id", e);
			}
		} catch (IOException e) {
			throw new RequestErrorException("Cannot create pipeline in MQM.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	@Override
	public void updatePipelineMetadata(String serverIdentity, String projectName, long pipelineId, String pipelineName, Long releaseId) {
		HttpPut request = new HttpPut(createSharedSpaceInternalApiUri(URI_JOB_CONFIGURATION, serverIdentity, projectName));

        JSONObject pipelineObject = new JSONObject();
        pipelineObject.put("contextEntityType", "pipeline");
        pipelineObject.put("contextEntityId", pipelineId);
        if (pipelineName != null) {
            pipelineObject.put("contextEntityName", pipelineName);
        }
        if (releaseId != null) {
            if (releaseId == -1) {
                pipelineObject.put("releaseId", JSONNull.getInstance());
            } else  {
                pipelineObject.put("releaseId", releaseId);
            }
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
				throw new RequestException("Pipeline metadata update failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
			}
		} catch (IOException e) {
			throw new RequestErrorException("Cannot update pipeline metadata.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	@Override
	public Pipeline updatePipelineTags(String serverIdentity, String jobName, long pipelineId, List<Taxonomy> taxonomies, List<Field> fields) {
		HttpPost request = new HttpPost(createProjectApiUriMap(URI_PIPELINES_TAGS, serverParams(serverIdentity, jobName, DEFAULT_WORKSPACE)));

		JSONObject pipelineObject = new JSONObject();
		pipelineObject.put("pipelineId", pipelineId);

		JSONArray taxonomiesArray = new JSONArray();
		for (Taxonomy taxonomy : taxonomies) {
			JSONObject taxonomyObject = new JSONObject();
			if (taxonomy.getId() != null) {
				taxonomyObject.put("id", taxonomy.getId());
				taxonomiesArray.add(taxonomyObject);
				continue;
			}
			if (StringUtils.isEmpty(taxonomy.getName())) {
				throw new IllegalArgumentException("Either taxonomy id or name needs to be specified");
			}
			taxonomyObject.put("name", taxonomy.getName());
			if (taxonomy.getTaxonomyTypeId() != null) {
				taxonomyObject.put("typeId", taxonomy.getTaxonomyTypeId());
				taxonomiesArray.add(taxonomyObject);
				continue;
			}
			if (StringUtils.isEmpty(taxonomy.getTaxonomyTypeName())) {
				throw new IllegalArgumentException("Either taxonomy typeId or typeName needs to be specified");
			}
			taxonomyObject.put("typeName", taxonomy.getTaxonomyTypeName());
			taxonomiesArray.add(taxonomyObject);
		}
		pipelineObject.put("taxonomies", taxonomiesArray);

		JSONArray tagsArray = new JSONArray();
		for (Field field : fields) {
			JSONObject fieldObject = new JSONObject();
			if (field.getId() != null) {
				fieldObject.put("id", field.getId());
			} else {
				fieldObject.put("parentId", field.getParentId());
				fieldObject.put("name", field.getName());
			}
			tagsArray.add(fieldObject);
		}
		pipelineObject.put("tags", tagsArray);

		JSONArray pipelines = new JSONArray();
		pipelines.add(pipelineObject);

		JSONObject pipelinesObject = new JSONObject();
		pipelinesObject.put("pipelines", pipelines);

		request.setEntity(new StringEntity(pipelinesObject.toString(), ContentType.APPLICATION_JSON));
		request.setHeader(HEADER_ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw new RequestException("Pipeline tags update failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
			}
			String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JSONObject jsonObject = JSONObject.fromObject(json);

			for (JSONObject relatedPipeline : getJSONObjectCollection(jsonObject, "relatedPipelines")) {
				Pipeline pipeline = toPipeline(relatedPipeline);
				if (pipeline.getId() == pipelineId) {
					return pipeline;
				}
			}

			throw new RequestException("Updated pipeline not found in the response");
		} catch (IOException e) {
			throw new RequestErrorException("Cannot update pipeline tags.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	private List<FieldMetadata> getFieldsMetadata(JSONObject metadata) {
		List<FieldMetadata> fields = new LinkedList<FieldMetadata>();
		for (JSONObject fieldObject : getJSONObjectCollection(metadata, "lists")) {
			fields.add(toFieldMetadata(fieldObject));
		}
		return fields;
	}

	private Field toField(JSONObject field) {
		return new Field(field.getInt("id"),
				field.getString("name"),
				field.getInt("parentId"),
				field.getString("parentName"),
				field.getString("parentLogicalName"));
	}

	private FieldMetadata toFieldMetadata(JSONObject field) {
		return new FieldMetadata(
				field.getInt("id"),
				field.getString("name"),
				field.getString("logicalName"),
				field.getBoolean("openList"),
				field.getBoolean("multiValueList"));
	}

	private Pipeline toPipeline(JSONObject pipelineObject) {
		List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
		List<Field> fields = new LinkedList<Field>();

		if (pipelineObject.has("taxonomies")) {
			for (JSONObject taxonomy : getJSONObjectCollection(pipelineObject, "taxonomies")) {
				taxonomies.add(new Taxonomy(taxonomy.getLong("id"),
						taxonomy.getJSONObject("parent").getLong("id"),
						taxonomy.getString("name"),
						taxonomy.getJSONObject("parent").getString("name")));
			}
		}
		if (pipelineObject.has("tags")) {
			for (JSONObject field : getJSONObjectCollection(pipelineObject, "tags")) {
				fields.add(toField(field));
			}
		}
		return new Pipeline(pipelineObject.getLong("contextEntityId"),
				pipelineObject.getString("contextEntityName"),
                pipelineObject.getBoolean("pipelineRoot"),
				pipelineObject.getLong("workspaceId"),
				pipelineObject.has("releaseId") && !pipelineObject.get("releaseId").equals(JSONNull.getInstance()) ? pipelineObject.getLong("releaseId") : null,
				taxonomies, fields);
	}

	@Override
	public PagedList<Release> queryReleases(String name, long workspaceId, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		return getEntities(getEntityURI(URI_RELEASES, conditions, workspaceId, offset, limit), offset, new ReleaseEntityFactory());
	}

	@Override
	public PagedList<Taxonomy> queryTaxonomies(Long taxonomyTypeId, String name, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		if (taxonomyTypeId != null) {
			conditions.add(condition("taxonomy-type-id", String.valueOf(taxonomyTypeId)));
		}
		return new PagedList<Taxonomy>(new ArrayList<Taxonomy>(), 0, 0);
		//return getEntities(getEntityURI(URI_TAXONOMIES, conditions, offset, limit), offset, new TaxonomyEntityFactory());
	}

	@Override
	public PagedList<TaxonomyType> queryTaxonomyTypes(String name, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		return new PagedList<TaxonomyType>(new ArrayList<TaxonomyType>(), 0, 0);
		//return getEntities(getEntityURI(URI_TAXONOMY_TYPES, conditions, offset, limit), offset, new TaxonomyTypeEntityFactory());
	}

	@Override
	public PagedList<ListItem> queryListItems(int listId, String name, long workspaceId, int offset, int limit) {
		List<String> conditions = new LinkedList<String>();
		if (!StringUtils.isEmpty(name)) {
			conditions.add(condition("name", "*" + name + "*"));
		}
		conditions.add(condition("list_root.id", String.valueOf(listId)));
		return getEntities(getEntityURI(URI_LIST_ITEMS, conditions, workspaceId, offset, limit), offset, new ListItemEntityFactory());
	}

	private void postTestResult(HttpUriRequest request) {
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw new RequestException("Test result posting failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
			}
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

	private Map<String, Object> serverParams(String serverIdentity, String jobName, int workspaceId) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("server", serverIdentity);
		params.put("job", jobName);
		params.put("workspace", workspaceId);
		return params;
	}

	private static class ListItemEntityFactory extends AbstractEntityFactory<ListItem> {

		@Override
		public ListItem doCreate(JSONObject entityObject) {
			return new ListItem(
					entityObject.getInt("id"),
					entityObject.getString("name"));
		}
	}

	private static class TaxonomyEntityFactory extends AbstractEntityFactory<Taxonomy> {

		@Override
		public Taxonomy doCreate(JSONObject entityObject) {
			return new Taxonomy(
					entityObject.getLong("id"),
					entityObject.getLong("taxonomy-type-id"),
					entityObject.getString("name"),
					null);
		}
	}

	private static class TaxonomyTypeEntityFactory extends AbstractEntityFactory<TaxonomyType> {

		@Override
		public TaxonomyType doCreate(JSONObject entityObject) {
			return new TaxonomyType(entityObject.getLong("id"), entityObject.getString("name"));
		}
	}

	private static class ReleaseEntityFactory extends AbstractEntityFactory<Release> {

		@Override
		public Release doCreate(JSONObject entityObject) {
			return new Release(entityObject.getLong("id"), entityObject.getString("name"));
		}
	}

	private static abstract class AbstractEntityFactory<E> implements EntityFactory<E> {

		@Override
		public E create(String json) {
			JSONObject jsonObject = JSONObject.fromObject(json);
			return doCreate(jsonObject);
		}

		public abstract E doCreate(JSONObject entityObject);

	}
}
