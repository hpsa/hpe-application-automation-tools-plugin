package com.hp.mqm.client;

import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TestResultStatus;
import com.hp.mqm.client.model.Workspace;
import net.sf.json.JSONObject;

import java.io.File;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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
	 * @param skipErrors        try to continue if non-fatal issue occurs
	 * @return id of the post operation
	 */
	long postTestResult(InputStreamSource inputStreamSource, boolean skipErrors);


	/**
	 * Posts test results to MQM. Divide extra large test results into smaller files which will be posted individually
	 * (multiple invocation of this method) to avoid HTTP request timeout.
	 *
	 * @param testResultReport XML file with test reports
	 * @param skipErrors       try to continue if non-fatal issue occurs
	 * @throws com.hp.mqm.client.exception.FileNotFoundException
	 */
	long postTestResult(File testResultReport, boolean skipErrors);

	/**
	 * Checks if the test results are needed in NGA.
	 *
	 * @param serverIdentity The server id
	 * @param jobName       The job name
	 */
	Boolean isTestResultRelevant(String serverIdentity, String jobName);


	/**
	 * Get status of the test result post operation
	 *
	 * @param id ID of the post operation
	 * @return operation status
	 */
	TestResultStatus getTestResultStatus(long id);

	/**
	 * Get (error) log associated with the test result post operation
	 *
	 * @param id     ID of the post operation
	 * @param output structure to receive the log
	 */
	void getTestResultLog(long id, LogOutput output);

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
	 * Either <code>pipeline.*</code> value can be null (except for id). In that case, the value isn't updated.
	 * <p/>
	 * It is not possible to update the <code>pipeline.root</code> flag (value is ignored if specified).
	 * <p/>
	 * In order to dissociate pipeline from release, <code>releaseId</code> value -1 needs to be specified.
	 * <p/>
	 *
	 * @param serverIdentity identity of the server
	 * @param jobName        name of the job
	 * @param pipeline       pipeline structure
	 */
	Pipeline updatePipeline(String serverIdentity, String jobName, Pipeline pipeline);


	/**
	 * Delete Tests by pipeline nodes on the MQM server.
	 * <p/>
	 * <p/>
	 *
	 * @param jobName        name of the job
	 * @param pipelineId       pipeline id
	 * @param workspaceId 	 workspace id
	 */
	void deleteTestsFromPipelineNodes(String jobName, Long pipelineId, Long workspaceId);

	/**
	 * Query releases matching given name filter (using contains semantics).
	 * <p/>
	 * <p/>
	 * If <code>name</code> is not specified or empty, all releases are returned.
	 * <p/>
	 *
	 * @param name        release name filter (can be null or empty)
	 * @param workspaceId workspace
	 * @param offset      paging offset
	 * @param limit       paging limit
	 * @return releases matching given name
	 */
	PagedList<Release> queryReleases(String name, long workspaceId, int offset, int limit);

	/**
	 * Get release of given ID in given workspace
	 *
	 * @param releaseId
	 * @param workspaceId
	 * @return release, null if release does not exist
	 */
	Release getRelease(long releaseId, long workspaceId);

	/**
	 * Query workspaces matching given name filter (using contains semantics).
	 * <p/>
	 * <p/>
	 * If <code>name</code> is not specified or empty, all workspaces are returned.
	 * <p/>
	 *
	 * @param name   workspace name filter (can be null or empty)
	 * @param offset paging offset
	 * @param limit  paging limit
	 * @return workspaces matching given name
	 */
	PagedList<Workspace> queryWorkspaces(String name, int offset, int limit);

	/**
	 * Get workspaces of given IDs
	 *
	 * @param workspaceIds list of workspaceIds - maximum size of list is 100 values
	 * @return workspaces matching given IDs
	 */
	List<Workspace> getWorkspaces(List<Long> workspaceIds);

	/**
	 * Query taxonomies (including categories) matching given name (using contains semantics).
	 * <p/>
	 * <p/>
	 * If <code>name</code> is not specified or empty, all taxonomies are considered.
	 *
	 * @param name        taxonomy name filter (can be null or empty)
	 * @param workspaceId workspace
	 * @param offset      paging offset
	 * @param limit       paging limit
	 * @return taxonomies matching given name and type
	 */
	PagedList<Taxonomy> queryTaxonomies(String name, long workspaceId, int offset, int limit);

	/**
	 * Get taxonomies with given IDs
	 *
	 * @param taxonomyIds list of taxonomyIds - maximum size of list is 100 values
	 * @param workspaceId workspace
	 * @return taxonomies matching given IDs
	 */
	List<Taxonomy> getTaxonomies(List<Long> taxonomyIds, long workspaceId);

	/**
	 * Query list for items matching given name (using contains semantics).
	 * <p/>
	 * If <code>name</code> is not specified or empty, all items are considered.
	 *
	 * @param logicalListName logical name of a list to search in
	 * @param name            item name filter (can be null or empty)
	 * @param workspaceId     workspace
	 * @param offset          paging offset
	 * @param limit           paging limit
	 * @return list items matching given name filter
	 */
	PagedList<ListItem> queryListItems(String logicalListName, String name, long workspaceId, int offset, int limit);

	/**
	 * Get listItems of given IDs in given workspace
	 *
	 * @param itemIds
	 * @param workspaceId
	 * @return
	 */
	List<ListItem> getListItems(List<Long> itemIds, long workspaceId);

	/**
	 * Get metadata fields of given workspace
	 *
	 * @param workspaceId
	 * @return metadata fields which are supported (has field_features: pipeline_tagging)
	 */
	List<FieldMetadata> getFieldsMetadata(long workspaceId);

	/**
	 *
	 * @param entity
	 * @param skipErrors
	 * @param uftTests
	 * @param uftTestJson
	 * @param uftTestData
	 * @param serverURL
     * @return
	 * @throws UnsupportedEncodingException
     */


	JSONObject postTest(String uftTestJson, HashMap<String, String> uftTestData, String serverURL) throws UnsupportedEncodingException;

	/**
	 *
	 * @param testId
	 * @param resourceMtrAsJSON
	 * @param serverURL
	 * @throws UnsupportedEncodingException
     */


	void attachUFTParametersToTest(String testId, String resourceMtrAsJSON, String serverURL) throws UnsupportedEncodingException;

	/**
	 * Sends events list to MQM [PUT request].
	 * Data expected to be serialized in JSON form by the consumer.
	 * No exception is expected to be thrown.
	 *
	 * @param eventsJSON JSON serialized events list
	 * @return notifies the consumer about the final result of an action
	 */
	boolean putEvents(String eventsJSON);

	/**
	 * Retrieves tasks from service working in Abridged Connectivity Mode
	 *
	 * @param selfIdentity identity of the server
	 * @param selfType     ci server type
	 * @param selfLocation location of the server (URL)
	 * @param apiVersion
	 * @param sdkVersion
	 * @return
	 */
	String getAbridgedTasks(String selfIdentity, String selfType, String selfLocation, Integer apiVersion, String sdkVersion);

	/**
	 * Sends Result of the Abridged Task Invocation
	 *
	 * @param selfIdentity
	 * @param taskId
	 * @param contentJSON
	 * @return status code
	 */
	int putAbridgedResult(String selfIdentity, String taskId, String contentJSON);
}
