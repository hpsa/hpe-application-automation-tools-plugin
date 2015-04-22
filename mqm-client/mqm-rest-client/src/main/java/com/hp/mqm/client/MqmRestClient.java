package com.hp.mqm.client;

import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.JobConfiguration;
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
     * @param  testResultReport XML file with test reports
     * @throws com.hp.mqm.client.exception.FileNotFoundException
     */
    void postTestResult(File testResultReport);

    // TODO: janotav: add javadoc

    JobConfiguration getJobConfiguration(String serverIdentity, String jobName);

    PagedList<Release> queryReleases(String name, int offset, int limit);

    PagedList<Taxonomy> queryTaxonomies(Integer taxonomyTypeId, String name, int offset, int limit);

    PagedList<TaxonomyType> queryTaxonomyTypes(String name, int offset, int limit);

    int createPipeline(String pipelineName, int releaseId, String structureJson, String serverJson);

    void updatePipelineMetadata(int pipelineId, String pipelineName, int releaseId);

    void updatePipelineTags(String serverIdentity, String jobName, int pipelineId, List<Taxonomy> taxonomies);

	/**
	 * Posts events list to MQM.
	 * Data expected to be serialized in JSON form by the consumer.
	 * No exception is expected to be thrown.
	 *
	 * @param eventsJSON JSON serialized events list
	 * @return notifies the consumer about the final result of an action
	 */
	boolean putEvents(String eventsJSON);
}
