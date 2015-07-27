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
import com.hp.mqm.client.model.TaxonomyType;
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
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.fail;

public class MqmRestClientImplTest {

	private static final String LOCATION = ConnectionProperties.getLocation();
	private static final String SHARED_SPACE = ConnectionProperties.getSharedSpace();
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
	public void testTryToConnectProject() {
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
		try {
			client.tryToConnectProject();
		} finally {
			client.release();
		}

		// bad credentials
		MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.tryToConnectProject();
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
			client.tryToConnectProject();
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
			client.tryToConnectProject();
		} finally {
			client.release();
		}

		// test autologin
		try {
			client.logout();
			client.tryToConnectProject();
		} finally {
			client.release();
		}
		client.release();

		// bad domain
		badConnectionConfig = new MqmConnectionConfig(
				LOCATION, "BadDomain123", USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.tryToConnectProject();
			fail();
		} catch (SharedSpaceNotExistException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}
		client.release();
	}

	@Test
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
		Release release = testSupportClient.createRelease(releaseName);
		String pipelineName = "Pipeline" + timestamp;
		final JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		int pipelineId = client.createPipeline("", jobName, pipelineName, 1001L, release.getId(), structure.toString(), server.toString());
		Assert.assertTrue(pipelineId > 0);
		Thread.sleep(1000);

		putJenkinsInsightEvent(jobName, server, JIEventType.QUEUED, 0);
		putJenkinsInsightEvent(jobName, server, JIEventType.STARTED, 1000);
		putJenkinsInsightEvent(jobName, server, JIEventType.FINISHED, 1000);

		boolean buildExists = false;
		for (int i = 0; i < 30; i++) {
			if ((buildExists = testSupportClient.checkBuild(serverIdentity, jobName, 1))) {
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
			client.postTestResult(testResults);
		} finally {
			client.release();
		}

		PagedList<TestRun> pagedList = testSupportClient.queryTestRuns("testOne" + timestamp, 0, 50);
		Assert.assertEquals(1, pagedList.getItems().size());
		Assert.assertEquals("testOne" + timestamp, pagedList.getItems().get(0).getName());

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
			});
		} finally {
			client.release();
		}

		// invalid payload
		final File testResults2 = new File(this.getClass().getResource("TestResults2.xmlx").toURI());
		try {
			client.postTestResult(testResults2);
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
			});
		} catch (RequestException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}
		testResults.delete();

		// test "file does not exist"
		final File file = new File("abcdefghchijklmn.xml");
		try {
			client.postTestResult(file);
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
		Assert.assertNull(jobConfiguration.getJobId());
		Assert.assertNull(jobConfiguration.getJobName());
		Assert.assertFalse(jobConfiguration.isPipelineRoot());
		Assert.assertTrue(jobConfiguration.getRelatedPipelines().isEmpty());
		Assert.assertTrue(jobConfiguration.getFieldMetadata().size() > 0);

		// create release and pipeline
		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		int pipelineId = client.createPipeline("", jobName, pipelineName, 1001L, release.getId(), structure.toString(), server.toString());
		Assert.assertTrue(pipelineId > 0);

		// verify job configuration
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertNotNull(jobConfiguration.getJobId());
		Assert.assertEquals(jobName, jobConfiguration.getJobName());
		Assert.assertTrue(jobConfiguration.isPipelineRoot());
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		Pipeline pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals(pipelineName, pipeline.getName());
		Assert.assertTrue(pipeline.getTaxonomies().isEmpty());
		Assert.assertTrue(pipeline.getFields().isEmpty());
		Assert.assertTrue(jobConfiguration.getFieldMetadata().size() > 0);
	}

	@Test
	public void testCreatePipeline() throws IOException {
		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		int pipelineId = client.createPipeline("", jobName, pipelineName, 1001L, release.getId(), structure.toString(), server.toString());
		Assert.assertTrue(pipelineId > 0);
	}

	@Test
	public void testUpdatePipelineMetadata() throws IOException {
		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		int pipelineId = client.createPipeline("", jobName, pipelineName, 1001L, release.getId(), structure.toString(), server.toString());
		Assert.assertTrue(pipelineId > 0);

		Release release2 = testSupportClient.createRelease(releaseName + "New");
		client.updatePipelineMetadata(pipelineId, pipelineName + "New", release2.getId());
		JobConfiguration jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		Pipeline pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals(release2.getId(), pipeline.getReleaseId());
		Assert.assertEquals(pipelineName + "New", pipeline.getName());

		// no release ID update
		client.updatePipelineMetadata(pipelineId, pipelineName, null);
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals(release2.getId(), pipeline.getReleaseId());
		Assert.assertEquals(pipelineName, pipeline.getName());

		// no pipeline name update
		client.updatePipelineMetadata(pipelineId, null, release.getId());
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals(release.getId(), pipeline.getReleaseId());
		Assert.assertEquals(pipelineName, pipeline.getName());

		// clear release update
		client.updatePipelineMetadata(pipelineId, null, -1L);
		jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		Assert.assertEquals(1, jobConfiguration.getRelatedPipelines().size());
		pipeline = jobConfiguration.getRelatedPipelines().get(0);
		Assert.assertEquals(-1, (long) pipeline.getReleaseId());
		Assert.assertEquals(pipelineName, pipeline.getName());
	}

	@Test
	public void testUpdatePipelineTags() throws IOException {
		String serverIdentity = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis();
		String jobName = "Job" + timestamp;

		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName);
		String pipelineName = "Pipeline" + timestamp;
		JSONObject server = ResourceUtils.readJson("server.json");
		server.put("instanceId", serverIdentity);
		server.put("url", "http://localhost:8080/jenkins" + timestamp);
		JSONObject structure = ResourceUtils.readJson("structure.json");
		structure.put("name", jobName);
		int pipelineId = client.createPipeline("", jobName, pipelineName, 1001L, release.getId(), structure.toString(), server.toString());
		Assert.assertTrue(pipelineId > 0);

		JobConfiguration jobConfiguration = client.getJobConfiguration(serverIdentity, jobName);
		List<FieldMetadata> fieldMetadata = jobConfiguration.getFieldMetadata();
		int frameworkId = getListIdByLogicalName(fieldMetadata, "hp.qc.test-framework");
		int toolTypeId = getListIdByLogicalName(fieldMetadata, "hp.qc.test-tool-type");
		int testTypeId = getListIdByLogicalName(fieldMetadata, "hp.qc.test-new-type");
		int acceptanceId = getListItemIdByName(testTypeId, "Acceptance");
		int sanityId = getListItemIdByName(testTypeId, "Sanity");

		// assign new tags
		List<Taxonomy> taxonomies = new LinkedList<Taxonomy>();
		taxonomies.add(new Taxonomy(null, null, "Chrome" + timestamp, "Browser" + timestamp));
		LinkedList<Field> fields = new LinkedList<Field>();
		fields.add(new Field(null, "JUnit" + timestamp, frameworkId, "Framework", "hp.qc.test-framework"));
		Pipeline pipeline = client.updatePipelineTags(serverIdentity, jobName, pipelineId, taxonomies, fields);
		Assert.assertEquals(1, pipeline.getTaxonomies().size());
		Taxonomy taxonomy = pipeline.getTaxonomies().get(0);
		Assert.assertNotNull(taxonomy.getId());
		Assert.assertNotNull(taxonomy.getTaxonomyTypeId());
		Assert.assertEquals("Chrome" + timestamp, taxonomy.getName());
		Assert.assertEquals("Browser" + timestamp, taxonomy.getTaxonomyTypeName());
		Assert.assertEquals(1, pipeline.getFields().size());
		Field field = pipeline.getFields().get(0);
		Assert.assertNotNull(field.getId());
		Assert.assertEquals("JUnit" + timestamp, field.getName());
		Assert.assertEquals("Framework", field.getParentName());

		// assign both new and existing
		taxonomies.clear();
		taxonomies.add(taxonomy);
		taxonomies.add(new Taxonomy(null, taxonomy.getTaxonomyTypeId(), "Firefox" + timestamp, "Browser" + timestamp));
		fields.clear();
		fields.add(field);
		fields.add(new Field(null, "Selenium" + timestamp, toolTypeId, "Testing Tool Type", "hp.qc.test-tool-type"));
		pipeline = client.updatePipelineTags(serverIdentity, jobName, pipelineId, taxonomies, fields);
		Assert.assertEquals(2, pipeline.getTaxonomies().size());
		Taxonomy taxonomy2 = getTaxonomyByName(pipeline.getTaxonomies(), "Firefox" + timestamp);
		Assert.assertNotNull(taxonomy2.getId());
		Assert.assertEquals(taxonomy.getTaxonomyTypeId(), taxonomy2.getTaxonomyTypeId());
		Assert.assertEquals("Firefox" + timestamp, taxonomy2.getName());
		Assert.assertEquals("Browser" + timestamp, taxonomy2.getTaxonomyTypeName());
		Taxonomy taxonomy3 = getTaxonomyByName(pipeline.getTaxonomies(), "Chrome" + timestamp);
		Assert.assertEquals(taxonomy.getId(), taxonomy3.getId());
		Assert.assertEquals(taxonomy.getTaxonomyTypeId(), taxonomy3.getTaxonomyTypeId());
		Assert.assertEquals(taxonomy.getName(), taxonomy3.getName());
		Assert.assertEquals(taxonomy.getTaxonomyTypeName(), taxonomy3.getTaxonomyTypeName());
		Assert.assertEquals(2, pipeline.getFields().size());
		Field field2 = getFieldByName(pipeline.getFields(), "Selenium" + timestamp);
		Assert.assertNotNull(field2.getId());
		Assert.assertEquals("hp.qc.test-tool-type", field2.getParentLogicalName());
		Field field3 = getFieldByName(pipeline.getFields(), "JUnit" + timestamp);
		Assert.assertEquals(field.getId(), field3.getId());
		Assert.assertEquals(field.getName(), field3.getName());
		Assert.assertEquals(field.getParentName(), field3.getParentName());
		Assert.assertEquals(field.getParentLogicalName(), field3.getParentLogicalName());
		Assert.assertEquals(field.getParentId(), field3.getParentId());

		// assign multiple field
		taxonomies.clear();
		fields.clear();
		fields.add(new Field(acceptanceId, "Acceptance", testTypeId, "Test Type", "hp.qc.test-new-type"));
		fields.add(new Field(sanityId, "Sanity", testTypeId, "Test Type", "hp.qc.test-new-type"));
		pipeline = client.updatePipelineTags(serverIdentity, jobName, pipelineId, taxonomies, fields);
		Assert.assertEquals(0, pipeline.getTaxonomies().size());
		Assert.assertEquals(2, pipeline.getFields().size());
		Field field4 = getFieldByName(pipeline.getFields(), "Acceptance");
		Assert.assertEquals(acceptanceId, (int) field4.getId());
		Field field5 = getFieldByName(pipeline.getFields(), "Sanity");
		Assert.assertEquals(sanityId, (int) field5.getId());
	}

	@Test
	public void testQueryReleases() throws IOException {
		long timestamp = System.currentTimeMillis();
		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName);

		PagedList<Release> releases = client.queryReleases(null, 0, 100);
		Assert.assertTrue(releases.getItems().size() > 0);

		releases = client.queryReleases(releaseName, 0, 100);
		Assert.assertEquals(1, releases.getItems().size());
		Assert.assertEquals(release.getId(), releases.getItems().get(0).getId());
		Assert.assertEquals(release.getName(), releases.getItems().get(0).getName());
	}

	@Test
	public void testQueryTaxonomies() throws IOException {
		long timestamp = System.currentTimeMillis();
		String typeName = "TaxonomyType" + timestamp;
		TaxonomyType taxonomyType = testSupportClient.createTaxonomyType(typeName);
		Taxonomy taxonomy = testSupportClient.createTaxonomy(taxonomyType.getId(), "Taxonomy" + timestamp);

		PagedList<Taxonomy> taxonomies = client.queryTaxonomies(null, null, 0, 100);
		Assert.assertTrue(taxonomies.getItems().size() > 0);

		taxonomies = client.queryTaxonomies(taxonomyType.getId(), null, 0, 100);
		Assert.assertEquals(1, taxonomies.getItems().size());
		Assert.assertEquals(taxonomy.getName(), taxonomies.getItems().get(0).getName());

		taxonomies = client.queryTaxonomies(taxonomyType.getId(), taxonomy.getName(), 0, 100);
		Assert.assertEquals(1, taxonomies.getItems().size());
		Assert.assertEquals(taxonomy.getName(), taxonomies.getItems().get(0).getName());
	}

	@Test
	public void testQueryTaxonomyTypes() throws IOException {
		long timestamp = System.currentTimeMillis();
		String typeName = "TaxonomyType" + timestamp;
		TaxonomyType taxonomyType = testSupportClient.createTaxonomyType(typeName);

		PagedList<TaxonomyType> taxonomyTypes = client.queryTaxonomyTypes(null, 0, 100);
		Assert.assertTrue(taxonomyTypes.getItems().size() > 0);

		taxonomyTypes = client.queryTaxonomyTypes(taxonomyType.getName(), 0, 100);
		Assert.assertEquals(1, taxonomyTypes.getItems().size());
		Assert.assertEquals(taxonomyType.getName(), taxonomyTypes.getItems().get(0).getName());
	}

	@Test
	public void testQueryListItems() {
		PagedList<ListItem> toolTypeList = client.queryListItems(0, "Testing Tool", 0, 100);
		Assert.assertEquals(1, toolTypeList.getItems().size());
		Assert.assertEquals("Testing Tool", toolTypeList.getItems().get(0).getName());

		PagedList<ListItem> items = client.queryListItems(toolTypeList.getItems().get(0).getId(), null, 0, 100);
		Assert.assertTrue(items.getItems().size() > 0);

		// get longest name to ensure single match of the contains operator
		String name = getLongestItemName(items.getItems());

		items = client.queryListItems(toolTypeList.getItems().get(0).getId(), name, 0, 100);
		Assert.assertEquals(1, items.getItems().size());
	}

	private String getLongestItemName(List<ListItem> items) {
		if (items.isEmpty()) {
			Assert.fail("No item found");
		}
		String name = "";
		for (ListItem item : items) {
			if (item.getName().length() > name.length()) {
				name = item.getName();
			}
		}
		return name;
	}

	private int getListItemIdByName(int listId, String name) {
		List<ListItem> items = client.queryListItems(listId, name, 0, 1).getItems();
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
}
