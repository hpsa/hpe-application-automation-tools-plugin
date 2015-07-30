package com.hp.mqm.client;

import com.hp.mqm.client.model.Field;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TaxonomyType;

import java.io.File;
import java.util.List;

/**
 * Client for connection to MQM public API. It wraps whole http communication with MQM server. Client handles login automatically.
 * When client is not intended to use anymore or for a long time, method {@link #release()} must be called.
 * <p/>
 * <p>
 * All methods can throw {@link com.hp.mqm.client.exception.RequestException} when unexpected result is returned from
 * MQM server and {@link com.hp.mqm.client.exception.RequestErrorException} in case of IO error or error in the HTTP protocol.
 * <p/>
 * <p/>
 * <p>
 * Because client cares about login automatically all methods (except {@link #release()}) can
 * throw {@link com.hp.mqm.client.exception.LoginException} (as a special case of RequestException) in case authentication failure and
 * {@link com.hp.mqm.client.exception.LoginErrorException} (as a special case of RequestErrorException) in case of IO error or
 * error in the HTTP protocol during authentication.
 * </p>
 */
public interface MqmRestClient extends BaseMqmRestClient {

	/**
	 * Posts test results to MQM. Test results can be large data and therefore be aware to keep it in memory.
	 * Also divide extra large test results into smaller parts which will be posted individually
	 * (multiple invocation of this method) to avoid HTTP request timeout.
	 * <p/>
	 * InputStream obtained from InputStreamSource is automatically closed after all data are read.
	 *
	 * @param inputStreamSource input stream source with test results in MQM XML format.
	 */
	void postTestResult(InputStreamSource inputStreamSource);

	/**
	 * Posts test results to MQM. Divide extra large test results into smaller files which will be posted individually
	 * (multiple invocation of this method) to avoid HTTP request timeout.
	 *
	 * @param testResultReport XML file with test reports
	 * @throws com.hp.mqm.client.exception.FileNotFoundException
	 */
	void postTestResult(File testResultReport);

	/**
	 * Retrieve job configuration from MQM server. If given job doesn't participate in any pipeline, "empty"
	 * object is returned (never null).
	 *
	 * @param serverIdentity server identifier
	 * @param jobName        job name
	 * @return job configuration
	 */
	JobConfiguration getJobConfiguration(String serverIdentity, String jobName);

	/**
	 * Create pipeline on the MQM server.
	 *
	 * @param serverIdentity server identity, the one provided by the plugin
	 * @param projectName    root job name
	 * @param pipelineName   name of the pipeline
	 * @param workspaceId    workspace ID that the pipeline should be assigned to
	 * @param releaseId      release the pipeline will belong to
	 * @param structureJson  pipeline descriptor (structure defined by Jenkins Insight)
	 * @param serverJson     server descriptor (structure defined by Jenkins Insight)
	 * @return created pipeline id
	 */
	Pipeline createPipeline(String serverIdentity, String projectName, String pipelineName, long workspaceId, Long releaseId, String structureJson, String serverJson);

	/**
	 * Update pipeline metadata on the MQM server.
	 * <p/>
	 * <p/>
	 * Either <code>pipelineName</code> or <code>releaseId</code> can be null. In that case, the value isn't updated.
	 * <p/>
	 * <p/>
	 * In order to dissociate pipeline from release, <code>releaseId</code> value -1 needs to be specified.
	 * <p/>
     *
     * @deprecated use {@link #updatePipeline(String, String, Pipeline)} instead
	 *
	 * @param pipelineId   pipeline ID
	 * @param pipelineName new pipeline name (can be null)
	 * @param releaseId    new release ID (can be null)
	 */
    @Deprecated
	void updatePipelineMetadata(String serverIdentity, String projectName, long pipelineId, String pipelineName, Long releaseId);

	/**
	 * Update tags associated with the pipeline. Both "taxonomy" and "field" tags are updated.
	 * <p/>
	 * <p/>
	 * Tags specified with this call replace any existing tags currently specified for given pipeline. Both taxonomy and
	 * field tags can have null IDs, in which case they are first created on the server and then associated to the
	 * pipeline.
	 * <p/>
     *
     * @deprecated use {@link #updatePipeline(String, String, Pipeline)} instead
	 *
	 * @param serverIdentity identity of the server
	 * @param jobName        name of the job
	 * @param pipelineId     pipeline ID
	 * @param taxonomies     list of "taxonomy" tags associated with the pipeline
	 * @param fields         list of "fields" tags associated with the pipeline
	 * @return pipeline structure
	 */
    @Deprecated
	Pipeline updatePipelineTags(String serverIdentity, String jobName, long pipelineId, List<Taxonomy> taxonomies, List<Field> fields);

    /**
     * Update pipeline metadata on the MQM server.
     * <p/>
     * <p/>
     * Either <code>pipeline.*</code> value can be null (except for id). In that case, the value isn't updated.
     * <p/>
     * It is not possible to update the <code>pipeline.root</code> flag (value is ignored if specified).
     * <p/>
     * In order to dissociate pipeline from release, <code>releaseId</code> value -1 needs to be specified.
     * <p/>
     *
     * @param serverIdentity identity of the server
     * @param jobName        name of the job
     * @param pipeline      pipeline structure
     */
    Pipeline updatePipeline(String serverIdentity, String jobName, Pipeline pipeline);

	/**
	 * Query releases matching given name filter (using contains semantics).
	 * <p/>
	 * <p/>
	 * If <code>name</code> is not specified or empty, all releases are returned.
	 * <p/>
	 *
	 * @param name   release name filter (can be null or empty)
	 * @param offset paging offset
	 * @param limit  paging limit
	 * @return releases matching given name
	 */
	PagedList<Release> queryReleases(String name, long workspaceId, int offset, int limit);

	/**
	 * Query taxonomies matching given name filter (using contains semantics) and taxonomy type.
	 * <p/>
	 * <p/>
	 * If <code>name</code> is not specified or empty, all taxonomies are considered.
	 * <p/>
	 * If <code>taxonomyTypeId</code> is specified, only taxonomies of given type are considered.
	 *
	 * @param taxonomyTypeId taxonomy type (can be null)
	 * @param name           taxonomy name filter (can be null or empty)
	 * @param offset         paging offset
	 * @param limit          paging limit
	 * @return taxonomies matching given name and type
	 */
	PagedList<Taxonomy> queryTaxonomies(Long taxonomyTypeId, String name, long workspaceId, int offset, int limit);

	/**
	 * Query taxonomy types matching given name filter (using contains semantics).
	 * <p/>
	 * If <code>name</code> is not specified or empty, all taxonomy types are considered.
	 *
	 * @param name   taxonomy type name filter (can be null or empty)
	 * @param offset paging offset
	 * @param limit  paging limit
	 * @return taxonomy types matching given name filter
	 */
	PagedList<TaxonomyType> queryTaxonomyTypes(String name, long workspaceId, int offset, int limit);

	/**
	 * Query list for items matching given name (using contains semantics).
	 * <p/>
	 * If <code>name</code> is not specified or empty, all items are considered.
	 *
	 * @param listId list id
	 * @param name   item name filter (can be null or empty)
	 * @param offset paging offset
	 * @param limit  paging limit
	 * @return list items matching given name filter
	 */
	PagedList<ListItem> queryListItems(int listId, String name, long workspaceId, int offset, int limit);

	/**
	 * Sends events list to MQM [PUT request].
	 * Data expected to be serialized in JSON form by the consumer.
	 * No exception is expected to be thrown.
	 *
	 * @param eventsJSON JSON serialized events list
	 * @return notifies the consumer about the final result of an action
	 */
	boolean putEvents(String eventsJSON);
}
