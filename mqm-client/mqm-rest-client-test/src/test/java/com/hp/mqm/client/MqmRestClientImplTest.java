package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.LoginErrorException;
import com.hp.mqm.client.exception.LoginException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.mqm.client.model.FieldMetadata;
import com.hp.mqm.client.model.JobConfiguration;
import com.hp.mqm.client.model.ListField;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Pipeline;
import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TestResultStatus;
import com.hp.mqm.client.model.TestRun;
import com.hp.mqm.client.model.Workspace;
import com.hp.mqm.org.apache.http.client.methods.HttpPost;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import com.hp.mqm.org.apache.http.HttpResponse;
import com.hp.mqm.org.apache.http.HttpStatus;
import com.hp.mqm.org.apache.http.client.ResponseHandler;
import com.hp.mqm.org.apache.http.client.methods.HttpGet;
import com.hp.mqm.org.apache.http.client.utils.HttpClientUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.fail;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
    public static final String NONUSER = "nonuser"; // special user that is rejected by the mock portal

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
				LOCATION, SHARED_SPACE, "nonuser", "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.login();
			fail("Login should failed because of bad credentials.");
		} catch (LoginException e) {
			Assert.assertNotNull(e);
		} finally {
			client.releaseQuietly();
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
	public void testTryToConnectSharedSpace() {
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
		try {
			client.tryToConnectSharedSpace();
		} finally {
			client.release();
		}

		// bad credentials
		MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, NONUSER, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
		client = new MqmRestClientImpl(badConnectionConfig);
		try {
			client.tryToConnectSharedSpace();
			fail();
		} catch (AuthenticationException e) {
			Assert.assertNotNull(e);
		} finally {
			client.releaseQuietly();
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
				LOCATION, "BadSharedSpace123", USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
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
	public void testExecute_autoLogin() throws IOException {
		final String uri = LOCATION + "/api/shared_spaces/" + SHARED_SPACE + "/workspaces/" + WORKSPACE + "/defects?query=%22id=0%22";
		MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);

		MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
				LOCATION, SHARED_SPACE, NONUSER, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
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
			response = client.execute(new HttpPost(LOCATION + "/" + AbstractMqmRestClient.URI_LOGOUT));
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
		Assert.assertEquals(WORKSPACE, pipeline.getWorkspaceId());
        Assert.assertTrue(pipeline.isRoot());
		Assert.assertTrue(pipeline.getTaxonomies().isEmpty());
		Assert.assertTrue(!pipeline.getFields().isEmpty());
		for (ListField field : pipeline.getFields()) {
			Assert.assertTrue(field.getValues().isEmpty());
		}
		Assert.assertEquals(1, jobConfiguration.getWorkspacePipelinesMap().size());
		Assert.assertEquals(Long.valueOf(WORKSPACE), jobConfiguration.getWorkspacePipelinesMap().keySet().iterator().next());
		Assert.assertEquals(pipeline, jobConfiguration.getWorkspacePipelinesMap().get(WORKSPACE).get(0));
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
	@Ignore // disabled until defect #2556 is fixed
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
		Taxonomy chrome = new Taxonomy(null, "Chrome" + timestamp, new Taxonomy(null, "Browser" + timestamp, null));
        taxonomies.add(chrome);
        pipeline.setTaxonomies(taxonomies);

		List<ListField> listFields = new LinkedList<ListField>();
		ListField testFramework = new ListField("test_framework", Arrays.asList(getSingleListItem("JUnit")));
		listFields.add(testFramework);
		pipeline.setFields(listFields);

        // update name, release, assign new tags

        Pipeline updatedPipeline = client.updatePipeline(serverIdentity, jobName, pipeline);
        Assert.assertEquals(pipelineName + "New", updatedPipeline.getName());
        Assert.assertEquals(release2.getId(), updatedPipeline.getReleaseId());
		Assert.assertEquals(WORKSPACE, updatedPipeline.getWorkspaceId());
        Assert.assertEquals(1, updatedPipeline.getTaxonomies().size());
        Taxonomy taxonomy = updatedPipeline.getTaxonomies().get(0);
        Assert.assertNotNull(taxonomy.getId());
		assertTaxonomies(Arrays.asList(taxonomy.getId()), Arrays.asList(chrome));
		assertListFields(pipeline.getFields(), updatedPipeline.getFields());

        Pipeline updatedPipeline2 = getSinglePipeline(serverIdentity, jobName);
        Assert.assertEquals(pipelineName + "New", updatedPipeline2.getName());
        Assert.assertEquals(release2.getId(), updatedPipeline2.getReleaseId());
        Taxonomy taxonomy2 = updatedPipeline2.getTaxonomies().get(0);
        Assert.assertNotNull(taxonomy2.getId());
		assertTaxonomies(Arrays.asList(taxonomy2.getId()), Arrays.asList(chrome));

        taxonomies.clear();
        taxonomies.add(taxonomy);
		Taxonomy firefox = new Taxonomy(null, "Firefox" + timestamp, new Taxonomy(taxonomy.getRoot().getId(), "Browser" + timestamp, null));
        taxonomies.add(firefox);
        pipeline.setTaxonomies(taxonomies);

		listFields.clear();
		listFields.add(testFramework);
		ListField testType = new ListField("test_type", Arrays.asList(getSingleListItem("Sanity"), getSingleListItem("End to End")));
		listFields.add(testType);
		pipeline.setFields(listFields);

        // assign both anew and existing tags
        updatedPipeline = client.updatePipeline(serverIdentity, jobName, pipeline);
        Assert.assertEquals(2, updatedPipeline.getTaxonomies().size());
        taxonomy2 = updatedPipeline.getTaxonomies().get(0);
        Taxonomy taxonomy3 = updatedPipeline.getTaxonomies().get(1);
		Assert.assertNotNull(taxonomy2.getId());
		Assert.assertNotNull(taxonomy3.getId());
		assertTaxonomies(Arrays.asList(taxonomy2.getId(), taxonomy3.getId()), Arrays.asList(chrome, firefox));
		assertListFields(pipeline.getFields(), updatedPipeline.getFields());

        updatedPipeline2 = getSinglePipeline(serverIdentity, jobName);
        Assert.assertEquals(2, updatedPipeline2.getTaxonomies().size());
		taxonomy2 = updatedPipeline2.getTaxonomies().get(0);
		taxonomy3 = updatedPipeline2.getTaxonomies().get(1);
		Assert.assertNotNull(taxonomy2.getId());
		Assert.assertNotNull(taxonomy3.getId());
		assertTaxonomies(Arrays.asList(taxonomy2.getId(), taxonomy3.getId()), Arrays.asList(chrome, firefox));
		assertListFields(pipeline.getFields(), updatedPipeline2.getFields());

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
	public void testGetRelease() throws IOException {
		long timestamp = System.currentTimeMillis();
		String releaseName = "Release" + timestamp;
		Release release = testSupportClient.createRelease(releaseName, WORKSPACE);

		Release rel = client.getRelease(release.getId(), WORKSPACE);

		Assert.assertEquals(release.getId(), rel.getId());
		Assert.assertEquals(release.getName(), rel.getName());
	}

	@Test
	public void testGetReleaseNotFound() throws IOException {
		Release rel = client.getRelease(-1, WORKSPACE);	//release with id -1 shall not exist
		Assert.assertNull(rel);
	}

	@Test
	public void testQueryWorkspaces() throws IOException {
		long timestamp = System.currentTimeMillis();
		String workspaceName = "Workspace" + timestamp;
		Workspace workspace = testSupportClient.createWorkspace(workspaceName);

		PagedList<Workspace> workspaces = client.queryWorkspaces(null, 0, 100);
		Assert.assertTrue(workspaces.getItems().size() > 0);

		workspaces = client.queryWorkspaces(workspaceName, 0, 100);
		Assert.assertEquals(1, workspaces.getItems().size());
		Assert.assertEquals(workspace.getId(), workspaces.getItems().get(0).getId());
		Assert.assertEquals(workspace.getName(), workspaces.getItems().get(0).getName());
	}

	@Test
	public void testGetWorkspaces() throws IOException {
		long timestamp = System.currentTimeMillis();
		String workspaceName1 = "Workspace" + timestamp;
		Workspace workspace1 = testSupportClient.createWorkspace(workspaceName1);
		timestamp = System.currentTimeMillis();
		String workspaceName2 = "Workspace" + timestamp;
		Workspace workspace2 = testSupportClient.createWorkspace(workspaceName2);

		List<Long> workspaceIds = Arrays.asList(workspace1.getId(), workspace2.getId());
		ArrayList<Workspace> items = new ArrayList<Workspace>(client.getWorkspaces(workspaceIds));

		Collections.sort(items, new Comparator<Workspace>() {
			@Override
			public int compare(Workspace left, Workspace right) {
				return (int) (left.getId() - right.getId());
			}
		});

		Assert.assertEquals(2, items.size());
		Assert.assertEquals(workspace1.getName(), items.get(0).getName());
		Assert.assertEquals(workspace1.getId(), items.get(0).getId());
		Assert.assertEquals(workspace2.getName(), items.get(1).getName());
		Assert.assertEquals(workspace2.getId(), items.get(1).getId());
	}

    @Test
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
				return (int) (left.getId() - right.getId());
			}
		});
        Assert.assertEquals(2, items.size());
        Assert.assertEquals(taxonomyType.getName(), items.get(0).getName());
        Assert.assertEquals(taxonomyType.getId(), items.get(0).getId());
        Assert.assertEquals(taxonomy.getName(), items.get(1).getName());
        Assert.assertEquals(taxonomy.getId(), items.get(1).getId());
    }

	@Test
	public void testGetTaxonomies() throws IOException {
		long timestamp = System.currentTimeMillis();
		String typeName1 = "TaxonomyType" + timestamp;
		Taxonomy taxonomyType1 = testSupportClient.createTaxonomyCategory(typeName1, WORKSPACE);
		Taxonomy taxonomy1 = testSupportClient.createTaxonomyItem(taxonomyType1.getId(), "Taxonomy" + timestamp, WORKSPACE);

		timestamp = System.currentTimeMillis();
		String typeName2 = "TaxonomyType" + timestamp;
		Taxonomy taxonomyType2 = testSupportClient.createTaxonomyCategory(typeName2, WORKSPACE);
		Taxonomy taxonomy2 = testSupportClient.createTaxonomyItem(taxonomyType2.getId(), "Taxonomy" + timestamp, WORKSPACE);

		List<Long> taxonomyIds = new LinkedList<Long>(Arrays.asList(taxonomy1.getId(), taxonomy2.getId()));
		ArrayList<Taxonomy> items = new ArrayList<Taxonomy>(client.getTaxonomies(taxonomyIds, WORKSPACE));

		Collections.sort(items, new Comparator<Taxonomy>() {
			@Override
			public int compare(Taxonomy left, Taxonomy right) {
				return (int) (left.getId() - right.getId());
			}
		});

		Assert.assertEquals(2, items.size());
		Assert.assertEquals(taxonomy1.getName(), items.get(0).getName());
		Assert.assertEquals(taxonomy1.getId(), items.get(0).getId());
		Assert.assertNotNull(items.get(0).getRoot());
		Assert.assertEquals(taxonomyType1.getName(), items.get(0).getRoot().getName());
		Assert.assertEquals(taxonomyType1.getId(), items.get(0).getRoot().getId());

		Assert.assertEquals(taxonomy2.getName(), items.get(1).getName());
		Assert.assertEquals(taxonomy2.getId(), items.get(1).getId());
		Assert.assertNotNull(items.get(1).getRoot());
		Assert.assertEquals(taxonomyType2.getName(), items.get(1).getRoot().getName());
		Assert.assertEquals(taxonomyType2.getId(), items.get(1).getRoot().getId());
	}

	@Test
	public void testQueryListItems() {
		PagedList<ListItem> toolTypeList = client.queryListItems(null, "Testing_Tool_Type", WORKSPACE, 0, 100);
		Assert.assertEquals(1, toolTypeList.getItems().size());
		Assert.assertEquals("Testing_Tool_Type", toolTypeList.getItems().get(0).getName());

		PagedList<ListItem> items = client.queryListItems(toolTypeList.getItems().get(0).getLogicalName(), null, WORKSPACE, 0, 100);
		Assert.assertTrue(items.getItems().size() > 0);

		// get longest name to ensure single match of the contains operator
        ArrayList<ListItem> list = new ArrayList<ListItem>(items.getItems());
        Collections.sort(list, new Comparator<ListItem>() {
			@Override
			public int compare(ListItem left, ListItem right) {
				return right.getName().length() - left.getName().length();
			}
		});

		items = client.queryListItems(toolTypeList.getItems().get(0).getLogicalName(), list.get(0).getName(), WORKSPACE, 0, 100);
		Assert.assertEquals(1, items.getItems().size());
	}

	@Test
	public void testGetListItems() {
		List<ListItem> items1 = testSupportClient.queryListItems("JUnit", WORKSPACE, 0, 100).getItems();
		Assert.assertEquals(1, items1.size());
		ListItem junit = items1.get(0);
		Assert.assertEquals("JUnit", junit.getName());
		Assert.assertNotNull(junit.getRoot());

		List<ListItem> items2 = testSupportClient.queryListItems("Acceptance", WORKSPACE, 0, 100).getItems();
		Assert.assertEquals(1, items2.size());
		ListItem acceptance = items2.get(0);
		Assert.assertEquals("Acceptance", acceptance.getName());
		Assert.assertNotNull(acceptance.getRoot());

		ArrayList<ListItem> expectedItems = new ArrayList<ListItem>(Arrays.asList(junit, acceptance));
		List<Long> expectedItemsIds = new LinkedList<Long>(Arrays.asList(junit.getId(), acceptance.getId()));

		ArrayList<ListItem> items = new ArrayList<ListItem>(client.getListItems(expectedItemsIds, WORKSPACE));
		Assert.assertTrue(items.size() > 0);

		Comparator<ListItem> comparator = new Comparator<ListItem>() {
			@Override
			public int compare(ListItem left, ListItem right) {
				return (int) (left.getId() - right.getId());
			}
		};
		Collections.sort(items, comparator);
		Collections.sort(expectedItems, comparator);

		Assert.assertEquals(2, items.size());
		Assert.assertEquals(expectedItems.get(0).getName(), items.get(0).getName());
		Assert.assertEquals(expectedItems.get(0).getId(), items.get(0).getId());
		Assert.assertNotNull(items.get(0).getRoot());
		Assert.assertEquals(expectedItems.get(0).getRoot().getName(), items.get(0).getRoot().getName());
		Assert.assertEquals(expectedItems.get(0).getRoot().getId(), items.get(0).getRoot().getId());

		Assert.assertEquals(expectedItems.get(1).getName(), items.get(1).getName());
		Assert.assertEquals(expectedItems.get(1).getId(), items.get(1).getId());
		Assert.assertNotNull(items.get(1).getRoot());
		Assert.assertEquals(expectedItems.get(1).getRoot().getName(), items.get(1).getRoot().getName());
		Assert.assertEquals(expectedItems.get(1).getRoot().getId(), items.get(1).getRoot().getId());
	}

	@Test
	public void testGetFieldsMetadata() {
		List<FieldMetadata> fieldsMetadataList = client.getFieldsMetadata(WORKSPACE);
		Assert.assertEquals(4, fieldsMetadataList.size());

		final List<String> supportedMetadataFields = new LinkedList<String>(Arrays.asList("test_framework", "test_tool_type", "test_level", "test_type"));
		for (FieldMetadata fieldsMetadata : fieldsMetadataList) {
			Assert.assertTrue(supportedMetadataFields.contains(fieldsMetadata.getName()));
		}
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
		Assert.assertEquals(1, jobConfiguration.getWorkspacePipelinesMap().keySet().size());
        return jobConfiguration.getRelatedPipelines().get(0);
    }

	private ListItem getSingleListItem(String name) {
		List<ListItem> listItems = testSupportClient.queryListItems(name, WORKSPACE, 0, 10).getItems();
		Assert.assertEquals(1, listItems.size());
		return listItems.get(0);
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

	private void assertTaxonomies(List<Long> ids, List<Taxonomy> taxonomies) {
		List<Taxonomy> foundTaxonomies = client.getTaxonomies(ids, WORKSPACE);
		Assert.assertEquals(ids.size(), foundTaxonomies.size());
		int matched = 0;

		for (Taxonomy taxonomy : foundTaxonomies) {
			for (Taxonomy tax : taxonomies) {
				if (tax.getName().equals(taxonomy.getName()) && tax.getRoot().getName().equals(taxonomy.getRoot().getName())) {
					matched++;
				}
			}
		}
		Assert.assertEquals(ids.size(), matched);
	}

	private void assertListFields(List<ListField> expectedFields, List<ListField> actualFields) {
		int matched = 0;
		for (ListField actual : actualFields) {
			boolean expectedFound = false;
			for (ListField expected : expectedFields) {
				if (actual.getName().equals(expected.getName())) {
					Assert.assertNotNull(actual.getValues());
					Assert.assertEquals(expected.getValues().size(), actual.getValues().size());

					List<Long> ids = new LinkedList<Long>();
					for (ListItem item : actual.getValues()) {
						ids.add(item.getId());
					}
					assertListFieldValues(ids, expected.getValues());
					expectedFound = true;
					matched++;
				}
			}
			if (!expectedFound) {	//if there is no expected ListField, assert, that the actual has empty values
				Assert.assertTrue(actual.getValues().isEmpty());
			}
		}
		Assert.assertEquals(expectedFields.size(), matched);
	}

	private void assertListFieldValues(List<Long> ids, List<ListItem> listItems) {
		List<ListItem> foundItems = client.getListItems(ids, WORKSPACE);
		Assert.assertEquals(ids.size(), foundItems.size());
		int matched = 0;

		for (ListItem item : foundItems) {
			for (ListItem listItem : listItems) {
				if (listItem.getName().equals(item.getName())) {
					matched++;
				}
			}
		}
		Assert.assertEquals(ids.size(), matched);
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
