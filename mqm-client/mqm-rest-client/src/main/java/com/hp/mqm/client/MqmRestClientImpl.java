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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MqmRestClientImpl extends AbstractMqmRestClient implements MqmRestClient {

    private static final String PAGING_FRAGMENT = "?offset={0}&limit={1}";
    private static final String FILTERING_FRAGMENT = "&query=%22({2}%3D%27{3}%27)%22";

    private static final String URI_PUSH_TEST_RESULT_PUSH = "test-results/v1";
    private static final String URI_SERVER_JOB_CONFIG = "cia/servers/{0}/jobconfig/{1}";
    private static final String URI_RELEASES = "releases" + PAGING_FRAGMENT;
    private static final String URI_TAXONOMIES = "taxonomys" + PAGING_FRAGMENT; // TODO: janotav: typo on server should be fixed
    private static final String URI_TAXONOMY_TYPES = "taxonomy-types" + PAGING_FRAGMENT;


    /**
     * Constructor for AbstractMqmRestClient.
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
                List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
                for (JSONObject taxonomy: getJSONObjectCollection(relatedPipeline, "taxonomyTags")) {
                    // TODO: janotav: key names to be specified
                    taxonomies.add(new Taxonomy(taxonomy.getInt("id"),
                            taxonomy.getInt("parentId"),
                            taxonomy.getString("value"),
                            taxonomy.getString("parentValue")));
                }
                pipelines.add(new Pipeline(relatedPipeline.getInt("pipelineId"),
                        relatedPipeline.getString("pipelineName"),
                        relatedPipeline.getInt("releaseId"),
                        // TODO: janotav: releaseName not defined
                        "Name of " + relatedPipeline.getInt("releaseId"),
                        relatedPipeline.getString("rootJobName"),
                        taxonomies));
            }
            return new JobConfiguration(jsonObject.getInt("jobId"),
                    jsonObject.getString("jobName"),
                    jsonObject.getBoolean("isPipelineRoot"),
                    pipelines);
        } catch (IOException e) {
            throw new RequestErrorException("Cannot retrieve job configuration from MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static Collection<JSONObject> getJSONObjectCollection(JSONObject object, String key) {
        JSONArray array = object.getJSONArray(key);
        return (Collection<JSONObject>)array.subList(0, array.size());
    }

    private URI getEntityURI(String collection, String filterKey, String filterValue, int offset, int limit) {
        if (!StringUtils.isEmpty(filterValue)) {
            return createProjectApiUri(collection + FILTERING_FRAGMENT, offset, limit, filterKey, escapeQueryValue(filterValue));
        } else {
            return createProjectApiUri(collection, offset, limit);
        }
    }

    @Override
    public PagedList<Release> getReleases(String name, int offset, int limit) {
        return getEntities(getEntityURI(URI_RELEASES, "name", name, offset, limit), offset, new EntityFactory<Release>() {
            @Override
            public Release create(JSONObject entityObject) {
                return new Release(entityObject.getInt("id"), entityObject.getString("name"));
            }
        });
    }

    @Override
    public PagedList<Taxonomy> getTaxonomies(String name, int offset, int limit) {
        return getEntities(getEntityURI(URI_TAXONOMIES, "name", name, offset, limit), offset, new EntityFactory<Taxonomy>() {
            @Override
            public Taxonomy create(JSONObject entityObject) {
                return new Taxonomy(
                        entityObject.getInt("id"),
                        entityObject.getInt("taxonomy-type-id"),
                        entityObject.getString("name"),
                        null);
            }
        });
    }

    @Override
    public PagedList<TaxonomyType> getTaxonomyTypes(String name, int offset, int limit) {
        return getEntities(getEntityURI(URI_TAXONOMY_TYPES, "name", name, offset, limit), offset, new EntityFactory<TaxonomyType>() {
            @Override
            public TaxonomyType create(JSONObject entityObject) {
                return new TaxonomyType(entityObject.getInt("id"), entityObject.getString("name"));
            }
        });
    }

    private static String escapeQueryValue(String value) {
        return value.replaceAll("([\"'])", "\\\\$1");
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

    private interface EntityFactory<E> {

        E create(JSONObject entityObject);

    }
}
