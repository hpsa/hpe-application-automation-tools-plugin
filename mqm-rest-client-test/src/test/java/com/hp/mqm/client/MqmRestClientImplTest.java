package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationErrorException;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.RequestException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.fail;

public class MqmRestClientImplTest {

    private static final String LOCATION = ConnectionProperties.getLocation();
    private static final String DOMAIN = ConnectionProperties.getDomain();
    private static final String PROJECT = ConnectionProperties.getProject();
    private static final String USERNAME = ConnectionProperties.getUsername();
    private static final String PASSWORD = ConnectionProperties.getPassword();
    private static final String PROXY_HOST = ConnectionProperties.getProxyHost();
    private static final Integer PROXY_PORT = ConnectionProperties.getProxyPort();

    private static final String CLIENT_TYPE = "test";

    public static final MqmConnectionConfig connectionConfig = new MqmConnectionConfig(
            LOCATION, DOMAIN, PROJECT, USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT
    );

    @Test
    public void testLoginLogout() {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        client.login();
        client.logout();

        // bad credentials
        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.login();
            fail("Login should failed because of bad credentials.");
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
            client.login();
            fail("Login should failed because of bad credentials.");
        } catch (AuthenticationErrorException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testCheckLogin() {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        Assert.assertTrue(client.checkLogin());

        // bad credentials
        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        Assert.assertFalse(client.checkLogin());

        // bad location
        badConnectionConfig = new MqmConnectionConfig(
                "http://invalidaddress", DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            Assert.assertFalse(client.checkLogin());
            fail();
        } catch (AuthenticationErrorException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testAutoLogin() {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        Assert.assertTrue(client.checkDomainAndProject());
        client.release();

        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.checkDomainAndProject();
            fail();
        } catch (AuthenticationException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testCheckDomainAndProject() {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        client.login();
        Assert.assertTrue(client.checkDomainAndProject());
        client.logout();

        // test autologin
        Assert.assertTrue(client.checkDomainAndProject());
        client.release();

        // bad domain
        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, "BadDomain123", PROJECT, USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        Assert.assertFalse(client.checkDomainAndProject());
        client.release();

        // bad project
        badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, "BadProject123", USERNAME, PASSWORD, CLIENT_TYPE, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        Assert.assertFalse(client.checkDomainAndProject());
        client.release();
    }

    @Test
    public void testPostTestResult() throws UnsupportedEncodingException {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        ByteArrayInputStream stream = new ByteArrayInputStream("<testResult></testResult>".getBytes("utf-8"));
        try {
            client.postTestResult(stream);
        } finally {
            client.release();
        }

        // invalid payload
        stream = new ByteArrayInputStream("<testResult><build><test duration=\"hola\"></build></testResult>".getBytes("utf-8"));
        try {
            client.postTestResult(stream);
            fail();
        } catch (RequestException e) {
            Assert.assertNotNull(e);
        } finally {
            client.release();
        }
    }
}
