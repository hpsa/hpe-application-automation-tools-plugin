package com.hp.mqm.clt;

import com.hp.mqm.clt.model.PagedList;
import com.hp.mqm.clt.tests.TestResultPushStatus;
import com.hp.mqm.clt.model.TestRun;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class RestClientTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static final String LOCATION = ConnectionProperties.getLocation();
	private static final int SHARED_SPACE = ConnectionProperties.getSharedSpaceId();
    private static final int WORKSPACE = ConnectionProperties.getWorkspaceId();
	private static final String USERNAME = ConnectionProperties.getUsername();
	private static final String PASSWORD = ConnectionProperties.getPassword();
    private static final String PROXY_HOST = ConnectionProperties.getProxyHost();
    private static final Integer PROXY_PORT = ConnectionProperties.getProxyPort();

	private Settings testClientSettings;
	private TestSupportClient testSupportClient;

	@Before
	public void init() {
		testSupportClient = new TestSupportClient(getDefaultSettings());
        testClientSettings = getDefaultSettings();
    }

    @After
    public void cleanup() throws IOException {
        testSupportClient.release();
    }

	@Test
    @Ignore // pending server-side authentication
	public void testLoginLogout() throws InterruptedException, IOException {
		RestClient client = new RestClient(testClientSettings);
		client.login();
		client.logout();

		// login twice should not cause exception
		client.login();
		client.login();

		// logout twice should not cause exception
		client.logout();
		client.logout();

		// bad credentials
        testClientSettings.setPassword("xxxbadxxxpasswordxxx");
		try {
			client.login();
			Assert.fail("Login should failed because of bad credentials.");
		} catch (IOException e) {
			Assert.assertNotNull(e);
		} finally {
			client.release();
		}

        // bad location
        testClientSettings.setServer("http://invalidaddress");
        try {
            client.login();
            Assert.fail("Login should failed because of bad location.");
        } catch (IOException e) {
            Assert.assertNotNull(e);
        } finally {
            client.release();
        }
	}

    @Test
    public void testPostTestResult() throws IOException, URISyntaxException, InterruptedException, ValidationException {
        RestClient client = new RestClient(testClientSettings);
        long timestamp = System.currentTimeMillis();

        // invalid payload
        final File testResultsInvalidPayload = new File(this.getClass().getResource("TestResult.xmlx").toURI());
        try {
            client.postTestResult(new FileEntity(testResultsInvalidPayload, ContentType.APPLICATION_XML));
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        } finally {
            client.release();
        }

        // test "file does not exist"
        final File nonExistingFile = new File("abcdefghchijklmn.xml");
        try {
            client.postTestResult(new FileEntity(nonExistingFile, ContentType.APPLICATION_XML));
            Assert.fail();
        } catch (FileNotFoundException e) {
            Assert.assertNotNull(e);
        } finally {
            client.release();
        }

        String testResultXml = ResourceUtils.readContent("TestResult.xml")
                .replaceAll("%%%TIMESTAMP%%%", String.valueOf(timestamp));
        final File testResult = temporaryFolder.newFile();
        FileUtils.write(testResult, testResultXml);
        try {
            long id = client.postTestResult(new FileEntity(testResult, ContentType.APPLICATION_XML));
            assertPublishResult(id, "success");
        } finally {
            client.release();
        }
        PagedList<TestRun> pagedList = testSupportClient.queryTestRuns("testOne" + timestamp, 0, 50);
        Assert.assertEquals(1, pagedList.getItems().size());
        Assert.assertEquals("testOne" + timestamp, pagedList.getItems().get(0).getName());
    }

    @Test
	public void testPostTestResult_skipErrors() throws IOException, URISyntaxException, InterruptedException, ValidationException {
		RestClient client = new RestClient(testClientSettings);
		long timestamp = System.currentTimeMillis();

        // try content that fails unless skip-errors is specified
		String testResultXmlInvalidRelease = ResourceUtils.readContent("TestResultWithRelease.xml")
				.replaceAll("%%%RELEASE_REF%%%", String.valueOf(Integer.MAX_VALUE))
				.replaceAll("%%%TIMESTAMP%%%", String.valueOf(timestamp));
		final File testResultInvalidRelease = temporaryFolder.newFile();
		FileUtils.write(testResultInvalidRelease, testResultXmlInvalidRelease);
		try {
			long id = client.postTestResult(new FileEntity(testResultInvalidRelease, ContentType.APPLICATION_XML));
            assertPublishResult(id, "failed");
		} finally {
			client.release();
		}

        PagedList<TestRun> pagedList = testSupportClient.queryTestRuns("testTwo" + timestamp, 0, 50);
		Assert.assertEquals(0, pagedList.getItems().size());

        // push the same content, but with skip-errors set to true
        testClientSettings.setSkipErrors(true);
        try {
            long id = client.postTestResult(new FileEntity(testResultInvalidRelease, ContentType.APPLICATION_XML));
            assertPublishResult(id, "warning");
        } finally {
            client.release();
        }

        pagedList = testSupportClient.queryTestRuns("testTwo" + timestamp, 0, 50);
        Assert.assertEquals(1, pagedList.getItems().size());
        Assert.assertEquals("testTwo" + timestamp, pagedList.getItems().get(0).getName());
	}

	private void assertPublishResult(long id, String expectedStatus) throws InterruptedException {
		String status = "";
		for (int i = 0; i < 100; i++) {
			TestResultPushStatus testResultPushStatus = testSupportClient.getTestResultStatus(id);
			status = testResultPushStatus.getStatus();
			if (!"running".equals(status) && !"queued".equals(status)) {
				break;
			}
			Thread.sleep(100);
		}
		Assert.assertEquals("Publish not finished with expected status", expectedStatus, status);
	}

    private Settings getDefaultSettings() {
        Settings settings = new Settings();
        settings.setServer(LOCATION);
        settings.setSharedspace(SHARED_SPACE);
        settings.setWorkspace(WORKSPACE);
        settings.setUser(USERNAME);
        settings.setPassword(PASSWORD);
        settings.setProxyHost(PROXY_HOST);
        settings.setProxyPort(PROXY_PORT);
        if (ConnectionProperties.getProxyUsername() != null) {
            settings.setProxyUser(ConnectionProperties.getProxyUsername());
            settings.setProxyPassword(ConnectionProperties.getProxyPassword());
        }
        return settings;
    }
}
