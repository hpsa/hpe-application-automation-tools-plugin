package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.DomainProjectNotExistException;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.LoginErrorException;
import com.hp.mqm.client.exception.LoginException;
import com.hp.mqm.client.exception.RequestException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;

@Ignore
public class MqmRestClientImplTest {

    private static final String LOCATION = ConnectionProperties.getLocation();
    private static final String DOMAIN = ConnectionProperties.getDomain();
    private static final String PROJECT = ConnectionProperties.getProject();
    private static final String USERNAME = ConnectionProperties.getUsername();
    private static final String PASSWORD = ConnectionProperties.getPassword();
    private static final String PROXY_HOST = ConnectionProperties.getProxyHost();
    private static final Integer PROXY_PORT = ConnectionProperties.getProxyPort();

    private static final String CLIENT_TYPE = "test";

    public static final MqmConnectionConfig connectionConfig;

    static {
        connectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT
        );
        connectionConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(ConnectionProperties.getProxyUsername(), ConnectionProperties.getProxyPassword()));
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
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
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
                "http://invalidaddress", DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.login();
            fail("Login should failed because of bad credentials.");
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
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
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
                "http://invalidaddress", DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.tryToConnectProject();
            fail();
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
                LOCATION, "BadDomain123", PROJECT, USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.tryToConnectProject();
            fail();
        } catch (DomainProjectNotExistException e) {
            Assert.assertNotNull(e);
        } finally {
            client.release();
        }
        client.release();

        // bad project
        badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, "BadProject123", USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.tryToConnectProject();
            fail();
        } catch (DomainProjectNotExistException e) {
            Assert.assertNotNull(e);
        } finally {
            client.release();
        }
    }

    @Test
    public void testExecute_autoLogin() throws IOException {
        final String uri = LOCATION + "/rest/domains/" + DOMAIN + "/projects/" + PROJECT + "/defects?query=%7Bid%5B0%5D%7D";
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);

        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
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
                public Integer handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
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
                public Integer handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
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
    public void testPostTestResult() throws UnsupportedEncodingException, URISyntaxException {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        final File testResults = new File(this.getClass().getResource("TestResults.xml").toURI());
        try {
            client.postTestResult(testResults);
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
        } finally {
            client.release();
        }

        // invalid payload
        final File testResults2 = new File(this.getClass().getResource("TestResults2.xml").toURI());
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
}
