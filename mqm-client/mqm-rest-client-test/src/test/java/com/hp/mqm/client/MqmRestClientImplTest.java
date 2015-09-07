package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.LoginErrorException;
import com.hp.mqm.client.exception.LoginException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.model.Field;
import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TestResultStatus;
import com.hp.mqm.client.model.TestRun;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.fail;

public class MqmRestClientImplTest {

	private static final String LOCATION = ConnectionProperties.getLocation();
	private static final String SHARED_SPACE = ConnectionProperties.getSharedSpace();
    private static final long WORKSPACE = ConnectionProperties.getWorkspaceId();
	private static final String USERNAME = ConnectionProperties.getUsername();
	private static final String PASSWORD = ConnectionProperties.getPassword();
	private static final String PROXY_HOST = ConnectionProperties.getProxyHost();
	private static final Integer PROXY_PORT = ConnectionProperties.getProxyPort();

	private static final String CLIENT_TYPE = "test";

	public static final MqmConnectionConfig connectionConfig;

	public enum JIEventType {
		QUEUED,
		STARTED,
		FINISHED;
	}

	static {
		connectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT
		);
		if (ConnectionProperties.getProxyUsername() != null) {
			connectionConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(ConnectionProperties.getProxyUsername(), ConnectionProperties.getProxyPassword()));
		}
	}

	private MqmRestClientImpl client;
	private TestSupportClient testSupportClient;

	@Before
	public void init() {
		client = new MqmRestClientImpl(connectionConfig);
		testSupportClient = new TestSupportClient(connectionConfig);
	}

	@Test
    @Ignore // pending server-side authentication
	public void testLoginLogout() throws InterruptedException {
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
		client.login();
		client.logout();

		// login twice should not cause exception
		client.login();
		client.login();

		// logout twice should not cause exception
		client.logout();
		client.logout();

		// bad credentials
		MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.login();
			fail("Login should failed because of bad credentials.");
		} catch (LoginException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}

		// bad location
		badConnectionConfig = new MqmConnectionConfig(
				"http://invalidaddress", SHARED_SPACE, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.login();
			fail("Login should failed because of bad credentials.");
		} catch (LoginException e) {
			// when proxied
			Assert.assertNotNull(e);
		} catch (LoginErrorException e) {
			Assert.assertNotNull(e);
		}
	}

	@Test
    @Ignore // pending server-side authentication
	public void testTryToConnectSharedSpace() {
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
		try {
			client.tryToConnectSharedSpace();
		} finally {
			client.release();
		}

		// bad credentials
		MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.tryToConnectSharedSpace();
			fail();
		} catch (AuthenticationException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}

		// bad location
		badConnectionConfig = new MqmConnectionConfig(
				"http://invalidaddress", SHARED_SPACE, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.tryToConnectSharedSpace();
			fail();
		} catch (LoginException e) {
			// when proxied
			Assert.assertNotNull(e);
		} catch (LoginErrorException e) {
			Assert.assertNotNull(e);
		}

		client = new MqmRestClientImpl(connectionConfig);
		client.login();
		try {
			client.tryToConnectSharedSpace();
		} finally {
			client.release();
		}

		// test autologin
		try {
			client.logout();
			client.tryToConnectSharedSpace();
		} finally {
			client.release();
		}
		client.release();

		// bad domain
		badConnectionConfig = new MqmConnectionConfig(
				LOCATION, "BadDomain123", USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.tryToConnectSharedSpace();
			fail();
		} catch (SharedSpaceNotExistException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}
		client.release();
	}

	@Test
    @Ignore // pending server-side authentication
	public void testExecute_autoLogin() throws IOException {
		//  TODO: this should do the ping against workspaces
		final String uri = LOCATION + "/api/shared_spaces/" + SHARED_SPACE + "/workspace/1234567/defects?query=%7Bid%5B0%5D%7D";
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);

		MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		MqmRestClientImpl invalidClient = new MqmRestClientImpl(badConnectionConfig);

		// test method execute
		HttpResponse response = null;
		loginLogout(client);
		try {
			response = client.execute(new HttpGet(uri));
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} finally {
			HttpClientUtils.closeQuietly(response);
			client.release();
		}

		try {
			response = invalidClient.execute(new HttpGet(uri));
			fail();
		} catch (LoginException e) {
			Assert.assertNotNull(e);
		} finally {
			HttpClientUtils.closeQuietly(response);
			client.release();
		}

		// test method execute with response handler
		loginLogout(client);
		try {
			int status = client.execute(new HttpGet(uri), new ResponseHandler<Integer>() {
				@Override
				public Integer handleResponse(HttpResponse response) throws IOException {
					return response.getStatusLine().getStatusCode();
				}
			});
			Assert.assertEquals(HttpStatus.SC_OK, status);
		} finally {
			HttpClientUtils.closeQuietly(response);
			client.release();
		}

		try {
			int status = invalidClient.execute(new HttpGet(uri), new ResponseHandler<Integer>() {
				@Override
				public Integer handleResponse(HttpResponse response) throws IOException {
					return response.getStatusLine().getStatusCode();
				}
			});
			fail();
		} catch (LoginException e) {
			Assert.assertNotNull(e);
		} finally {
			HttpClientUtils.closeQuietly(response);
			client.release();
		}
	}

	private void loginLogout(MqmRestClientImpl client) throws IOException {
		client.login();
		HttpResponse response = null;
		try {
			response = client.execute(new HttpGet(LOCATION + "/" + AbstractMqmRestClient.URI_LOGOUT));
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	@Test
	public void testPostTestResult() throws IOException, URISyntaxException, InterruptedException {
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);

		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		// create release and pipeline
		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);
		String pipelineName = "Pipeline" + timestamp;
		final JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		long pipelineId = client.createPipeline(serverIdentity, jobName, pipelineName, WORKSPACE, release.getId(), structure.toString(), server.toString()).getId();
		Assert.assertTrue(pipelineId > 0);
		Thread.sleep(1000);

		putJenkinsInsightEvent(jobName, server, JIEventType.QUEUED, 0);
		putJenkinsInsightEvent(jobName, server, JIEventType.STARTED, 1000);
		putJenkinsInsightEvent(jobName, server, JIEventType.FINISHED, 1000);

		boolean buildExists = false;
		for (int i = 0; i < 30; i++) {
			if ((buildExists = testSupportClient.checkBuild(serverIdentity, jobName, 1, WORKSPACE))) {
				break;
			}
			Thread.sleep(1000);
		}
		Assert.assertTrue("JI build not created", buildExists);

		String testResultsXml = ResourceUtils.readContent("TestResults.xml")
				.replaceAll("%%%SERVER_IDENTITY%%%", serverIdentity)
				.replaceAll("%%%TIMESTAMP%%%", String.valueOf(timestamp))
				.replaceAll("%%%JOB_NAME%%%", jobName);
		final File testResults = File.createTempFile(getClass().getSimpleName(), "");
		testResults.deleteOnExit();
		FileUtils.write(testResults, testResultsXml);
		try {
			long id = client.postTestResult(testResults, false);
            assertPublishResult(id, "success");
		} finally {
			client.release();
		}

		PagedList<TestRun> pagedList = testSupportClient.queryTestRuns("testOne" + timestamp, WORKSPACE, 0, 50);
		Assert.assertEquals(1, pagedList.getItems().size());
		Assert.assertEquals("testOne" + timestamp, pagedList.getItems().get(0).getName());

        // try to re-push the same content using InputStreamSource

		try {
            long id = client.postTestResult(new InputStreamSource() {
				@Override
				public InputStream getInputStream() {
					try {
						return new FileInputStream(testResults);
					} catch (java.io.FileNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}, false);
            assertPublishResult(id, "success");
		} finally {
			client.release();
		}

		// try content that fails unless skip-errors is specified

        String testResultsErrorXml = ResourceUtils.readContent("TestResultsError.xml")
                .replaceAll("%%%SERVER_IDENTITY%%%", serverIdentity)
                .replaceAll("%%%TIMESTAMP%%%", String.valueOf(timestamp))
                .replaceAll("%%%JOB_NAME%%%", jobName);
        final File testResultsError = File.createTempFile(getClass().getSimpleName(), "");
        testResults.deleteOnExit();
        FileUtils.write(testResultsError, testResultsErrorXml);
        try {
            long id = client.postTestResult(testResultsError, false);
            assertPublishResult(id, "failed");
        } finally {
            client.release();
        }

        // and verify that if succeeds partially with skip-errors=true

        try {
            long id = client.postTestResult(testResultsError, true);
            assertPublishResult(id, "warning");
        } finally {
            client.release();
        }

		// invalid payload
		final File testResults2 = new File(this.getClass().getResource("TestResults2.xmlx").toURI());
		try {
			client.postTestResult(testResults2, false);
			fail();
		} catch (RequestException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}
		try {
			client.postTestResult(new InputStreamSource() {
				@Override
				public InputStream getInputStream() {
					try {
						return new FileInputStream(testResults);
					} catch (java.io.FileNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}, false);
		} catch (RequestException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}
		testResults.delete();

		// test "file does not exist"
		final File file = new File("abcdefghchijklmn.xml");
		try {
			client.postTestResult(file, false);
			fail();
		} catch (FileNotFoundException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}
	}

	@Test
	public void testGetJobConfiguration() throws IOException {
		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		// there should be no pipeline
		JobConfiguration jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertTrue(jobConfiguration.getRelatedPipelines().isEmpty());

		// create release and pipeline
		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		long pipelineId = client.createPipeline(serverIdentity, jobName, pipelineName, WORKSPACE, release.getId(), structure.toString(), server.toString()).getId();
		Assert.assertTrue(pipelineId > 0);

		// verify job configuration
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		Pipeline pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals(pipelineName, pipeline.getName());
        Assert.assertTrue(pipeline.isRoot());
		Assert.assertTrue(pipeline.getTaxonomies().isEmpty());
		Assert.assertTrue(pipeline.getFields().isEmpty());
	}

	@Test
	public void testCreatePipeline() throws IOException {
		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		long pipelineId = client.createPipeline(serverIdentity, jobName, pipelineName, WORKSPACE, release.getId(), structure.toString(), server.toString()).getId();
		Assert.assertTrue(pipelineId > 0);
	}

	@Test
	public void testUpdatePipelineMetadata() throws IOException {
		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		long pipelineId = client.createPipeline(serverIdentity, jobName, pipelineName, WORKSPACE, release.getId(), structure.toString(), server.toString()).getId();
		Assert.assertTrue(pipelineId > 0);

		Release release2 = testSupportClient.createRelease(releaseName + "New", WORKSPACE);
		client.updatePipelineMetadata(serverIdentity, jobName, pipelineId, pipelineName + "New", WORKSPACE, release2.getId());
		JobConfiguration jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		Pipeline pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals((long) release2.getId(), (long) pipeline.getReleaseId());
		Assert.assertEquals(pipelineName + "New", pipeline.getName());

		// no release ID update
		client.updatePipelineMetadata(serverIdentity, jobName, pipelineId, pipelineName, WORKSPACE, null);
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals((long) release2.getId(), (long) pipeline.getReleaseId());
		Assert.assertEquals(pipelineName, pipeline.getName());

		// no pipeline name update
		client.updatePipelineMetadata(serverIdentity, jobName, pipelineId, null, WORKSPACE, release.getId());
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals((long) release.getId(), (long) pipeline.getReleaseId());
		Assert.assertEquals(pipelineName, pipeline.getName());

		// clear release update
		client.updatePipelineMetadata(serverIdentity, jobName, pipelineId, null, WORKSPACE, -1L);
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertNull(pipeline.getReleaseId());
		Assert.assertEquals(pipelineName, pipeline.getName());
	}

	@Test
    @Ignore // pending server-side support
	public void testUpdatePipelineTags() throws IOException {
//		String serverIdentity = UUID.randomUUID().toString();
//		long timestamp = System.currentTimeMillis();
//		String jobName = "Job" + timestamp;
//
//		String releaseName = "Release" + timestamp;
//		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);
//		String pipelineName = "Pipeline" + timestamp;
//		JSONObject server = ResourceUtils.readJson("server.json");
//		server.put("instanceId", serverIdentity);
//		server.put("url", "http://localhost:8080/jenkins" + timestamp);
//		JSONObject structure = ResourceUtils.readJson("structure.json");
//		structure.put("name", jobName);
//		long pipelineId = client.createPipeline(serverIdentity, jobName, pipelineName, WORKSPACE, release.getId(), structure.toString(), server.toString()).getId();
//		Assert.assertTrue(pipelineId > 0);
//
//		JobConfiguration jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
//		List<FieldMetadata> fieldMetadata = null; //TODO: jobConfiguration.getFieldMetadata();
//		int frameworkId = getListIdByLogicalName(fieldMetadata, "hp.qc.test-framework");
//		int toolTypeId = getListIdByLogicalName(fieldMetadata, "hp.qc.test-tool-type");
//		int testTypeId = getListIdByLogicalName(fieldMetadata, "hp.qc.test-new-type");
//		int acceptanceId = getListItemIdByName(testTypeId, "Acceptance");
//		int sanityId = getListItemIdByName(testTypeId, "Sanity");
//
//		// assign new tags
//		List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
//		taxonomies.add(new Taxonomy(null, null, "Chrome" + timestamp, "Browser" + timestamp));
//		LinkedList<Field> fields = new LinkedList<Field>();
//		fields.add(new Field(null, "JUnit" + timestamp, frameworkId, "Framework", "hp.qc.test-framework"));
//		Pipeline pipeline = client.updatePipelineTags(serverIdentity, jobName, pipelineId, taxonomies, fields);
//		Assert.assertEquals(1, pipeline.getTaxonomies().size());
//		Taxonomy taxonomy = pipeline.getTaxonomies().get(0);
//		Assert.assertNotNull(taxonomy.getId());
//		Assert.assertNotNull(taxonomy.getTaxonomyTypeId());
//		Assert.assertEquals("Chrome" + timestamp, taxonomy.getName());
//		Assert.assertEquals("Browser" + timestamp, taxonomy.getTaxonomyTypeName());
//		Assert.assertEquals(1, pipeline.getFields().size());
//		Field field = pipeline.getFields().get(0);
//		Assert.assertNotNull(field.getId());
//		Assert.assertEquals("JUnit" + timestamp, field.getName());
//		Assert.assertEquals("Framework", field.getParentName());
//
//		// assign both new and existing
//		taxonomies.clear();
//		taxonomies.add(taxonomy);
//		taxonomies.add(new Taxonomy(null, taxonomy.getTaxonomyTypeId(), "Firefox" + timestamp, "Browser" + timestamp));
//		fields.clear();
//		fields.add(field);
//		fields.add(new Field(null, "Selenium" + timestamp, toolTypeId, "Testing Tool Type", "hp.qc.test-tool-type"));
//		pipeline = client.updatePipelineTags(serverIdentity, jobName, pipelineId, taxonomies, fields);
//		Assert.assertEquals(2, pipeline.getTaxonomies().size());
//		Taxonomy taxonomy2 = getTaxonomyByName(pipeline.getTaxonomies(), "Firefox" + timestamp);
//		Assert.assertNotNull(taxonomy2.getId());
//		Assert.assertEquals(taxonomy.getTaxonomyTypeId(), taxonomy2.getTaxonomyTypeId());
//		Assert.assertEquals("Firefox" + timestamp, taxonomy2.getName());
//		Assert.assertEquals("Browser" + timestamp, taxonomy2.getTaxonomyTypeName());
//		Taxonomy taxonomy3 = getTaxonomyByName(pipeline.getTaxonomies(), "Chrome" + timestamp);
//		Assert.assertEquals(taxonomy.getId(), taxonomy3.getId());
//		Assert.assertEquals(taxonomy.getTaxonomyTypeId(), taxonomy3.getTaxonomyTypeId());
//		Assert.assertEquals(taxonomy.getName(), taxonomy3.getName());
//		Assert.assertEquals(taxonomy.getTaxonomyTypeName(), taxonomy3.getTaxonomyTypeName());
//		Assert.assertEquals(2, pipeline.getFields().size());
//		Field field2 = getFieldByName(pipeline.getFields(), "Selenium" + timestamp);
//		Assert.assertNotNull(field2.getId());
//		Assert.assertEquals("hp.qc.test-tool-type", field2.getParentLogicalName());
//		Field field3 = getFieldByName(pipeline.getFields(), "JUnit" + timestamp);
//		Assert.assertEquals(field.getId(), field3.getId());
//		Assert.assertEquals(field.getName(), field3.getName());
//		Assert.assertEquals(field.getParentName(), field3.getParentName());
//		Assert.assertEquals(field.getParentLogicalName(), field3.getParentLogicalName());
//		Assert.assertEquals(field.getParentId(), field3.getParentId());
//
//		// assign multiple field
//		taxonomies.clear();
//		fields.clear();
//		fields.add(new Field(acceptanceId, "Acceptance", testTypeId, "Test Type", "hp.qc.test-new-type"));
//		fields.add(new Field(sanityId, "Sanity", testTypeId, "Test Type", "hp.qc.test-new-type"));
//		pipeline = client.updatePipelineTags(serverIdentity, jobName, pipelineId, taxonomies, fields);
//		Assert.assertEquals(0, pipeline.getTaxonomies().size());
//		Assert.assertEquals(2, pipeline.getFields().size());
//		Field field4 = getFieldByName(pipeline.getFields(), "Acceptance");
//		Assert.assertEquals(acceptanceId, (int) field4.getId());
//		Field field5 = getFieldByName(pipeline.getFields(), "Sanity");
//		Assert.assertEquals(sanityId, (int) field5.getId());
	}

    @Test
	@Ignore // test needs to be rewritten - server now only returns IDs
    public void testUpdatePipeline() throws IOException {
        String serverIdentity = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        String jobName = "Job" + timestamp;

        String releaseName = "Release" + timestamp;
        Release release = testSupportClient.createRelease(releaseName, WORKSPACE);
        String pipelineName = "Pipeline" + timestamp;
        JSONObject server = ResourceUtils.readJson("server.json");
        server.put("instanceId", serverIdentity);
        server.put("url", "http://localhost:8080/jenkins" + timestamp);
        JSONObject structure = ResourceUtils.readJson("structure.json");
        structure.put("name", jobName);
        Pipeline pipeline = client.createPipeline(serverIdentity, jobName, pipelineName, WORKSPACE, release.getId(), structure.toString(), server.toString());
        Assert.assertTrue(pipeline.getId()> 0);

        Release release2 = testSupportClient.createRelease(releaseName + "New", WORKSPACE);
        pipeline.setName(pipelineName + "New");
        pipeline.setReleaseId(release2.getId());

        List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
        taxonomies.add(new Taxonomy(null, "Chrome" + timestamp, new Taxonomy(null, "Browser" + timestamp, null)));
        pipeline.setTaxonomies(taxonomies);

        // TODO: add field tags when available

        // update name, release, assign new tags

        Pipeline updatedPipeline = client.updatePipeline(serverIdentity, jobName, pipeline);
        Assert.assertEquals(pipelineName + "New", updatedPipeline.getName());
        Assert.assertEquals(release2.getId(), updatedPipeline.getReleaseId());
        Assert.assertEquals(1, updatedPipeline.getTaxonomies().size());
        Taxonomy taxonomy = updatedPipeline.getTaxonomies().get(0);
        Assert.assertNotNull(taxonomy.getId());
        Assert.assertNotNull(taxonomy.getRoot().getId());
        Assert.assertEquals("Chrome" + timestamp, taxonomy.getName());
        Assert.assertEquals("Browser" + timestamp, taxonomy.getRoot().getName());

        Pipeline updatedPipeline2 = getSinglePipeline(serverIdentity, jobName);
        Assert.assertEquals(pipelineName + "New", updatedPipeline2.getName());
        Assert.assertEquals(release2.getId(), updatedPipeline2.getReleaseId());
        Taxonomy taxonomy2 = updatedPipeline2.getTaxonomies().get(0);
        Assert.assertNotNull(taxonomy2.getId());
        Assert.assertNotNull(taxonomy2.getRoot().getId());
        Assert.assertEquals("Chrome" + timestamp, taxonomy2.getName());
        Assert.assertEquals("Browser" + timestamp, taxonomy2.getRoot().getName());

        taxonomies.clear();
        taxonomies.add(taxonomy);
        taxonomies.add(new Taxonomy(null, "Firefox" + timestamp, new Taxonomy(taxonomy.getRoot().getId(), "Browser" + timestamp, null)));
        pipeline.setTaxonomies(taxonomies);

        // assign both anew and existing tags
        updatedPipeline = client.updatePipeline(serverIdentity, jobName, pipeline);
        Assert.assertEquals(2, updatedPipeline.getTaxonomies().size());
        taxonomy2 = getTaxonomyByName(updatedPipeline.getTaxonomies(), "Firefox" + timestamp);
        Assert.assertNotNull(taxonomy2.getId());
        Assert.assertEquals(taxonomy.getRoot().getId(), taxonomy2.getRoot().getId());
        Assert.assertEquals("Firefox" + timestamp, taxonomy2.getName());
        Assert.assertEquals("Browser" + timestamp, taxonomy2.getRoot().getName());
        Taxonomy taxonomy3 = getTaxonomyByName(updatedPipeline.getTaxonomies(), "Chrome" + timestamp);
        assertTaxonomyEquals(taxonomy, taxonomy3);

        updatedPipeline2 = getSinglePipeline(serverIdentity, jobName);
        Assert.assertEquals(2, updatedPipeline2.getTaxonomies().size());
        taxonomy2 = getTaxonomyByName(updatedPipeline2.getTaxonomies(), "Firefox" + timestamp);
        Assert.assertNotNull(taxonomy2.getId());
        Assert.assertEquals(taxonomy.getRoot().getId(), taxonomy2.getRoot().getId());
        Assert.assertEquals("Firefox" + timestamp, taxonomy2.getName());
        Assert.assertEquals("Browser" + timestamp, taxonomy2.getRoot().getName());
        taxonomy3 = getTaxonomyByName(updatedPipeline2.getTaxonomies(), "Chrome" + timestamp);
        assertTaxonomyEquals(taxonomy, taxonomy3);

        // unset release
        pipeline.setReleaseId(-1l);
        updatedPipeline = client.updatePipeline(serverIdentity, jobName, pipeline);
        Assert.assertNull(updatedPipeline.getReleaseId());

        updatedPipeline2 = getSinglePipeline(serverIdentity, jobName);
        Assert.assertNull(updatedPipeline2.getReleaseId());
    }

	@Test
	public void testQueryReleases() throws IOException {
		long timestamp = System.currentTimeMillis();
		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);

		PagedList<Release> releases = client.queryReleases(null, WORKSPACE, 0, 100);
		Assert.assertTrue(releases.getItems().size() > 0);

		releases = client.queryReleases(releaseName, WORKSPACE, 0, 100);
		Assert.assertEquals(1, releases.getItems().size());
		Assert.assertEquals(release.getId(), releases.getItems().get(0).getId());
		Assert.assertEquals(release.getName(), releases.getItems().get(0).getName());
	}

	@Test
    @Ignore // pending server-side support for taxonomy_root.id cross-filter
	public void testQueryTaxonomyItems() throws IOException {
		long timestamp = System.currentTimeMillis();
		String typeName = "TaxonomyType" + timestamp;
		Taxonomy taxonomyType = testSupportClient.createTaxonomyCategory(typeName, WORKSPACE);
		Taxonomy taxonomy = testSupportClient.createTaxonomyItem(taxonomyType.getId(), "Taxonomy" + timestamp, WORKSPACE);

		PagedList<Taxonomy> taxonomies = client.queryTaxonomyItems(null, null, WORKSPACE, 0, 100);
		Assert.assertTrue(taxonomies.getItems().size() > 0);

		taxonomies = client.queryTaxonomyItems(taxonomyType.getId(), null, WORKSPACE, 0, 100);
		Assert.assertEquals(1, taxonomies.getItems().size());
		Assert.assertEquals(taxonomy.getName(), taxonomies.getItems().get(0).getName());

		taxonomies = client.queryTaxonomyItems(taxonomyType.getId(), taxonomy.getName(), WORKSPACE, 0, 100);
		Assert.assertEquals(1, taxonomies.getItems().size());
		Assert.assertEquals(taxonomy.getName(), taxonomies.getItems().get(0).getName());
	}

	@Test
	public void testQueryTaxonomyCategories() throws IOException {
		long timestamp = System.currentTimeMillis();
		String typeName = "TaxonomyType" + timestamp;
		Taxonomy taxonomyType = testSupportClient.createTaxonomyCategory(typeName, WORKSPACE);

		PagedList<Taxonomy> taxonomyTypes = client.queryTaxonomyCategories(null, WORKSPACE, 0, 100);
		Assert.assertTrue(taxonomyTypes.getItems().size() > 0);

		taxonomyTypes = client.queryTaxonomyCategories(taxonomyType.getName(), WORKSPACE, 0, 100);
		Assert.assertEquals(1, taxonomyTypes.getItems().size());
		Assert.assertEquals(taxonomyType.getName(), taxonomyTypes.getItems().get(0).getName());
	}

    @Test
	@Ignore // pending server-side support for taxonomy_root.name cross-filter
    public void testQueryTaxonomies() throws IOException {
        long timestamp = System.currentTimeMillis();
        String typeName = "TaxonomyType" + timestamp;
        Taxonomy taxonomyType = testSupportClient.createTaxonomyCategory(typeName, WORKSPACE);
        Taxonomy taxonomy = testSupportClient.createTaxonomyItem(taxonomyType.getId(), "Taxonomy" + timestamp, WORKSPACE);

        PagedList<Taxonomy> taxonomies = client.queryTaxonomies(null, WORKSPACE, 0, 100);
        Assert.assertTrue(taxonomies.getItems().size() > 0);

        taxonomies = client.queryTaxonomies("Taxonomy" + timestamp, WORKSPACE, 0, 100);
        Assert.assertEquals(1, taxonomies.getItems().size());
        Assert.assertEquals(taxonomy.getName(), taxonomies.getItems().get(0).getName());

        taxonomies = client.queryTaxonomies("TaxonomyType" + timestamp, WORKSPACE, 0, 100);
        List<Taxonomy> items = new ArrayList<Taxonomy>(taxonomies.getItems());
        Collections.sort(items, new Comparator<Taxonomy>() {
            @Override
            public int compare(Taxonomy left, Taxonomy right) {
                return (int)(left.getId() - right.getId());
            }
        });
        Assert.assertEquals(2, items.size());
        Assert.assertEquals(taxonomyType.getName(), items.get(0).getName());
        Assert.assertEquals(taxonomyType.getId(), items.get(0).getId());
        Assert.assertEquals(taxonomy.getName(), items.get(1).getName());
        Assert.assertEquals(taxonomy.getId(), items.get(1).getId());
    }

	@Test
    @Ignore // pending server-side support for list_root.id cross-filter
	public void testQueryListItems() {
		PagedList<ListItem> toolTypeList = client.queryListItems(0, "Testing_Tool_Type", WORKSPACE, 0, 100);
		Assert.assertEquals(1, toolTypeList.getItems().size());
		Assert.assertEquals("Testing_Tool_Type", toolTypeList.getItems().get(0).getName());

		PagedList<ListItem> items = client.queryListItems(toolTypeList.getItems().get(0).getId(), null, WORKSPACE, 0, 100);
		Assert.assertTrue(items.getItems().size() > 0);

		// get longest name to ensure single match of the contains operator
        ArrayList<ListItem> list = new ArrayList<ListItem>(items.getItems());
        Collections.sort(list, new Comparator<ListItem>() {
            @Override
            public int compare(ListItem left, ListItem right) {
                return right.getName().length() - left.getName().length();
            }
        });

		items = client.queryListItems(toolTypeList.getItems().get(0).getId(), list.get(0).getName(), WORKSPACE, 0, 100);
		Assert.assertEquals(1, items.getItems().size());
	}

    @Test
    public void testGetTestResultStatus() throws IOException {
        String testResultsXml = ResourceUtils.readContent("TestResults2.xml");
        File testResults = File.createTempFile(getClass().getSimpleName(), "");
        testResults.deleteOnExit();
        FileUtils.write(testResults, testResultsXml);
        long id = client.postTestResult(testResults, false);
        TestResultStatus resultStatus = client.getTestResultStatus(id);
        Assert.assertTrue("queued".equals(resultStatus.getStatus()) ||
                "running".equals(resultStatus.getStatus()) ||
                "failed".equals(resultStatus.getStatus()));
    }

    @Test
    public void testGetTestResultLog() throws IOException, InterruptedException {
        String testResultsXml = ResourceUtils.readContent("TestResults2.xml");
        File testResults = File.createTempFile(getClass().getSimpleName(), "");
        testResults.deleteOnExit();
        FileUtils.write(testResults, testResultsXml);
        long id = client.postTestResult(testResults, false);
        assertPublishResult(id, "failed");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        client.getTestResultLog(id, new SimpleLog(baos));
        String body = baos.toString("UTF-8");
        Assert.assertTrue(body.contains("status: failed\n"));
        Assert.assertTrue(body.contains("\n\nBuild reference {server: server; buildType: buildType; buildSid: 1} not resolved\n"));
    }

    @Test
    public void testErrorHandling() {
        try {
            client.getTestResultStatus(1234567890l);
            Assert.fail("should have failed");
        } catch (RequestException e) {
            Assert.assertEquals("Result status retrieval failed; error code: testbox.not_found; description: QueueItem id=1234567890 does not exist", e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Assert.assertTrue(sw.toString().contains("Caused by: com.hp.mqm.testbox.exception.ItemNotFoundException: QueueItem id=1234567890 does not exist"));
        }

        try {
            client.getTestResultLog(1234567890l, new SimpleLog(new ByteArrayOutputStream()));
            Assert.fail("should have failed");
        } catch (RequestException e) {
            Assert.assertEquals("Log retrieval failed; error code: testbox.not_found; description: QueueItem id=1234567890 does not exist", e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Assert.assertTrue(sw.toString().contains("Caused by: com.hp.mqm.testbox.exception.ItemNotFoundException: QueueItem id=1234567890 does not exist"));
        }
    }

    private Pipeline getSinglePipeline(String serverIdentity, String jobName) {
        JobConfiguration jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
        Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
        return jobConfiguration.getRelatedPipelines().get(0);
    }

	private int getListItemIdByName(int listId, String name) {
		List<ListItem> items = client.queryListItems(listId, name, WORKSPACE, 0, 1).getItems();
		Assert.assertEquals(1, items.size());
		return items.get(0).getId();
	}

	private int getListIdByLogicalName(List<FieldMetadata> metadata, String name) {
		for (FieldMetadata field : metadata) {
			if (name.equals(field.getLogicalListName())) {
				return field.getListId();
			}
		}
		Assert.fail("Field not found");
		throw new IllegalStateException();
	}

	private Taxonomy getTaxonomyByName(List<Taxonomy> taxonomies, String name) {
		for (Taxonomy taxonomy : taxonomies) {
			if (name.equals(taxonomy.getName())) {
				return taxonomy;
			}
		}
		Assert.fail("Taxonomy not found");
		throw new IllegalStateException();
	}

	private Field getFieldByName(List<Field> fields, String name) {
		for (Field field : fields) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		Assert.fail("Field not found");
		throw new IllegalStateException();
	}

	private void putJenkinsInsightEvent(String jobName, JSONObject server, JIEventType type, long delay) throws IOException, InterruptedException {
		JSONObject queued = ResourceUtils.readJson(type.name().toLowerCase() + ".json");
		queued.getJSONArray("events").getJSONObject(0).put("project", jobName);
		queued.put("server", server);
		client.putEvents(queued.toString());
		if (delay > 0) {
			Thread.sleep(delay);
		}
	}

    private void assertTaxonomyEquals(Taxonomy left, Taxonomy right) {
        Assert.assertEquals(left.getId(), right.getId());
        Assert.assertEquals(left.getName(), right.getName());
        if (left.getRoot() != null && right.getRoot() != null) {
            assertTaxonomyEquals(left.getRoot(), right.getRoot());
        } else {
            Assert.assertEquals(left.getRoot(), right.getRoot());
        }
    }

	private void assertPublishResult(long id, String expectedStatus) throws InterruptedException {
		String status = "";
		for (int i = 0; i < 100; i++) {
			TestResultStatus testResultStatus = client.getTestResultStatus(id);
			status = testResultStatus.getStatus();
			if (!"running".equals(status) && !"queued".equals(status)) {
				break;
			}
			Thread.sleep(100);
		}
		Assert.assertEquals("Publish not finished with expected status", expectedStatus, status);
	}

    private static class SimpleLog implements LogOutput {

        private ByteArrayOutputStream baos;

        private SimpleLog(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return baos;
        }
        @Override
        public void setContentType(String contentType) {
            Assert.assertEquals("text/plain", contentType);
        }
    }
}
