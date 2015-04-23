package com.hp.mqm.client;

import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.internal.InputStreamSourceEntity;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TaxonomyType;
import net.sf.json.JSONArray;
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
import java.util.logging.Logger;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MqmRestClientImpl extends AbstractMqmRestClient implements MqmRestClient {
	private static final Logger logger = Logger.getLogger(MqmRestClientImpl.class.getName());

    private static final String PAGING_FRAGMENT = "?offset={0}&limit={1}";
    private static final String FILTERING_FRAGMENT = "&query={2}";

    private static final String URI_PUSH_TEST_RESULT_PUSH = "test-results/v1";
    private static final String URI_SERVER_JOB_CONFIG = "cia/servers/{0}/jobconfig/{1}";
    private static final String URI_RELEASES = "releases-mqm" + PAGING_FRAGMENT;
    private static final String URI_TAXONOMIES = "taxonomies" + PAGING_FRAGMENT;
    private static final String URI_TAXONOMY_TYPES = "taxonomy-types" + PAGING_FRAGMENT;
    private static final String URI_PIPELINES = "cia/pipelines?fetchStructure=false";
    private static final String URI_PIPELINES_METADATA = "cia/pipelines/{0}/metadata";
    private static final String URI_PIPELINES_TAGS = "cia/pipelines/{0}/jobconfig/{1}";
	private static final String URI_PUT_EVENTS = "cia/events";

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
        HttpGet request = new HttpGet(createProjectApiUri(URI_SERVER_JOB_CONFIG, serverIdentity, jobName));
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RequestException("Job configuration retrieval failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
            }
            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = JSONObject.fromObject(json);
            List<Pipeline> pipelines = new LinkedList<Pipeline>();
            for (JSONObject relatedPipeline: getJSONObjectCollection(jsonObject, "relatedPipelines")) {
                pipelines.add(toPipeline(relatedPipeline));
            }
            if (jsonObject.containsKey("jobId")) {
                return new JobConfiguration(jsonObject.getInt("jobId"),
                        jsonObject.getString("jobName"),
                        jsonObject.getBoolean("isPipelineRoot"),
                        pipelines);
            } else {
                return new JobConfiguration(jsonObject.getBoolean("isPipelineRoot"), pipelines);
            }
        } catch (IOException e) {
            throw new RequestErrorException("Cannot retrieve job configuration from MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public int createPipeline(String pipelineName, int releaseId, String structureJson, String serverJson) {
        HttpPost request = new HttpPost(createProjectApiUri(URI_PIPELINES));
        JSONObject pipelineObject = new JSONObject();
        pipelineObject.put("name", pipelineName);
        pipelineObject.put("releaseId", releaseId);
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
            JSONObject jsonObject = JSONObject.fromObject(json);
            return jsonObject.getInt("id");
        } catch (IOException e) {
            throw new RequestErrorException("Cannot post test results to MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void updatePipelineMetadata(int pipelineId, String pipelineName, int releaseId) {
        HttpPut request = new HttpPut(createProjectApiUri(URI_PIPELINES_METADATA, pipelineId));
        JSONObject pipelineObject = new JSONObject();
        pipelineObject.put("pipelineId", pipelineId);
        pipelineObject.put("name", pipelineName);
        pipelineObject.put("releaseId", releaseId);
        request.setEntity(new StringEntity(pipelineObject.toString(), ContentType.APPLICATION_JSON));
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
    public Pipeline updatePipelineTags(String serverIdentity, String jobName, int pipelineId, List<Taxonomy> taxonomies) {
        HttpPost request = new HttpPost(createProjectApiUri(URI_PIPELINES_TAGS, serverIdentity, jobName));

        JSONObject pipelineObject = new JSONObject();
        pipelineObject.put("pipelineId", pipelineId);

        JSONArray taxonomiesArray = new JSONArray();
        for (Taxonomy taxonomy: taxonomies) {
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
        // TODO: janotav: not implemented
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

            for (JSONObject relatedPipeline: getJSONObjectCollection(jsonObject, "relatedPipelines")) {
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

    private Pipeline toPipeline(JSONObject pipelineObject) {
        List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
        for (JSONObject taxonomy: getJSONObjectCollection(pipelineObject, "taxonomyTags")) {
            taxonomies.add(new Taxonomy(taxonomy.getInt("taxonomyId"),
                    taxonomy.getInt("taxonomyTypeId"),
                    taxonomy.getString("taxonomyValue"),
                    taxonomy.getString("taxonomyType"))); // TODO: janotav: naming not symmetric
        }
        return new Pipeline(pipelineObject.getInt("pipelineId"),
                pipelineObject.getString("pipelineName"),
                pipelineObject.getInt("releaseId"),
                // TODO: janotav: releaseName not defined
                "Name of " + pipelineObject.getInt("releaseId"),
                pipelineObject.getString("rootJobName"),
                taxonomies);
    }

    private static Collection<JSONObject> getJSONObjectCollection(JSONObject object, String key) {
        JSONArray array = object.getJSONArray(key);
        return (Collection<JSONObject>)array.subList(0, array.size());
    }

    private URI getEntityURI(String collection, List<String> conditions, int offset, int limit) {
        if (!conditions.isEmpty()) {
            StringBuffer expr = new StringBuffer();
            for (String condition: conditions) {
                if (expr.length() > 0) {
                    expr.append(";");
                }
                expr.append(condition);
            }
            return createProjectApiUri(collection + FILTERING_FRAGMENT, offset, limit, "\"" + expr.toString() + "\"");
        } else {
            return createProjectApiUri(collection, offset, limit);
        }
    }

    @Override
    public PagedList<Release> queryReleases(String name, int offset, int limit) {
        List<String> conditions = new LinkedList<String>();
        if (!StringUtils.isEmpty(name)) {
            conditions.add(condition("name", "*" + name + "*"));
        }
        return getEntities(getEntityURI(URI_RELEASES, conditions, offset, limit), offset, new ReleaseEntityFactory());
    }

    @Override
    public PagedList<Taxonomy> queryTaxonomies(Integer taxonomyTypeId, String name, int offset, int limit) {
        List<String> conditions = new LinkedList<String>();
        if (!StringUtils.isEmpty(name)) {
            conditions.add(condition("name", "*" + name + "*"));
        }
        if (taxonomyTypeId != null) {
            conditions.add(condition("taxonomy-type-id", String.valueOf(taxonomyTypeId)));
        }
        return getEntities(getEntityURI(URI_TAXONOMIES, conditions, offset, limit), offset, new TaxonomyEntityFactory());
    }

    @Override
    public PagedList<TaxonomyType> queryTaxonomyTypes(String name, int offset, int limit) {
        List<String> conditions = new LinkedList<String>();
        if (!StringUtils.isEmpty(name)) {
            conditions.add(condition("name", "*" + name + "*"));
        }
        return getEntities(getEntityURI(URI_TAXONOMY_TYPES, conditions, offset, limit), offset, new TaxonomyTypeEntityFactory());
    }

    private String condition(String name, String value) {
        return name + "='" + escapeQueryValue(value) + "'";
    }

    private static String escapeQueryValue(String value) {
        return value.replaceAll("(\\\\)", "$1$1").replaceAll("([\"'])", "\\\\$1");
    }

    private <E> PagedList<E> getEntities(URI uri, int offset, EntityFactory<E> factory) {
        HttpGet request = new HttpGet(uri);
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RequestException("Entity retrieval failed with status code " + response.getStatusLine().getStatusCode() + " and reason " + response.getStatusLine().getReasonPhrase());
            }
            String entitiesJson = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject entities = JSONObject.fromObject(entitiesJson);

            LinkedList<E> items = new LinkedList<E>();
            for (JSONObject entityObject: getJSONObjectCollection(entities, "data")) {
                items.add(factory.create(entityObject));
            }
            return new PagedList<E>(items, offset, entities.getInt("total-count"));
        } catch (IOException e) {
            throw new RequestErrorException("Cannot retrieve entities from MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
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
        HttpPut request = new HttpPut(createProjectApiUri(URI_PUT_EVENTS));
        request.setEntity(new StringEntity(eventsJSON, ContentType.APPLICATION_JSON));
        HttpResponse response = null;
        boolean result = true;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.severe("put request failed while sending events: " + response.getStatusLine().getStatusCode());
                result = false;
            }
        } catch (Exception e) {
            logger.severe("put request failed while sending events" + e.getClass().getName());
            result = false;
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        return result;
    }

    private static class TaxonomyEntityFactory implements EntityFactory<Taxonomy> {

        @Override
        public Taxonomy create(JSONObject entityObject) {
            return new Taxonomy(
                    entityObject.getInt("id"),
                    entityObject.getInt("taxonomy-type-id"),
                    entityObject.getString("name"),
                    null);
        }
    }

    private static class TaxonomyTypeEntityFactory implements EntityFactory<TaxonomyType> {

        @Override
        public TaxonomyType create(JSONObject entityObject) {
            return new TaxonomyType(entityObject.getInt("id"), entityObject.getString("name"));
        }
    }

    private static class ReleaseEntityFactory implements EntityFactory<Release> {

        @Override
        public Release create(JSONObject entityObject) {
            return new Release(entityObject.getInt("id"), entityObject.getString("name"));
        }
    }

    private interface EntityFactory<E> {

        E create(JSONObject entityObject);

    }
}
