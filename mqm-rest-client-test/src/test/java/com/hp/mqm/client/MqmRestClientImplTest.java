package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.RequestException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.fail;

public class MqmRestClientImplTest {

    private static final String LOCATION = "http://localhost:8080/qcbin";
    private static final String DOMAIN = "DEFAULT";
    private static final String PROJECT = "new28";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "changeit";
    private static final String PROXY_HOST = null;
    private static final Integer PROXY_PORT = null;

    public static final MqmConnectionConfig connectionConfig = new MqmConnectionConfig(
            LOCATION, DOMAIN, PROJECT, USERNAME, PASSWORD, PROXY_HOST, PROXY_PORT
    );

    @Test
    public void testLoginLogout() {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        client.login();
        client.logout();

        // bad credentials
        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        try {
            client.login();
            fail("Login should failed because of bad credentials.");
        } catch (AuthenticationException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testAutoLogin() {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        Assert.assertTrue(client.checkDomainAndProject());
        client.release();

        MqmConnectionConfig badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, PROJECT, USERNAME, "xxxbadxxxpasswordxxx", PROXY_HOST, PROXY_PORT);
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
                LOCATION, "BadDomain123", PROJECT, USERNAME, PASSWORD, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        Assert.assertFalse(client.checkDomainAndProject());
        client.release();

        // bad project
        badConnectionConfig = new MqmConnectionConfig(
                LOCATION, DOMAIN, "BadProject123", USERNAME, PASSWORD, PROXY_HOST, PROXY_PORT);
        client = new MqmRestClientImpl(badConnectionConfig);
        Assert.assertFalse(client.checkDomainAndProject());
        client.release();
    }

    @Test
    public void testPostTestResult() throws UnsupportedEncodingException {
        MqmRestClientImpl client = new MqmRestClientImpl(connectionConfig);
        ByteArrayInputStream stream = new ByteArrayInputStream("<testResult></testResult>".getBytes("utf-8"));
        client.postTestResult(stream);
        client.release();

        // invalid payload
        stream = new ByteArrayInputStream("<testRResult></testRResult>".getBytes("utf-8"));
        try {
            client.postTestResult(stream);
            fail();
        } catch (RequestException e) {
            Assert.assertNotNull(e);
        }
        client.release();
    }
}
