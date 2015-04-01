// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqmRestClientImpl implements MqmRestClient {

    private static Logger logger = Logger.getLogger(MqmRestClientImpl.class.getName());

    private static final String DOMAIN_PROJECT_CHECK_RESOURCE = "defects?query=%7Bid%5B0%5D%7D";

    private final HttpClient httpClient;

    private final String location;
    private final String domain;
    private final String project;
    private final String username;
    private final String password;

    public MqmRestClientImpl(String location, String domain, String project, String username, String password) {
        this.location = location;
        this.domain = domain;
        this.project = project;
        this.username = username;
        this.password = password;

        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        List<String> authPrefs = new ArrayList<String>(2);
        authPrefs.add(AuthPolicy.DIGEST);
        authPrefs.add(AuthPolicy.BASIC);
        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
    }

    @Override
    public boolean login() {
        String authPoint = location +  "/authentication-point/alm-authenticate"; // NON-NLS

        PostMethod method = new PostMethod(authPoint);
        try {
            method.setRequestEntity(new StringRequestEntity(createAuthenticationXml(), "application/xml", "UTF-8"));
            int code = httpClient.executeMethod(method);
            if (code == 200) {
                return true;
            } else {
                logger.warning("Authentication failed: code=" + code + "; reason=" + method.getStatusLine().getReasonPhrase());
                return false;
            }
        } catch (IllegalArgumentException e) {
            // e.g. host parameter is null
            logger.log(Level.WARNING, "Authentication failed", e);
            return false;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Authentication failed", e);
            return false;
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public boolean createSession() {
        PostMethod method = new PostMethod(location + "/rest/site-session");
        try {
            method.setRequestEntity(new StringRequestEntity(createSessionXml(), "application/xml", "UTF-8"));
            int code = httpClient.executeMethod(method);
            if (code == 201) {
                return true;
            } else {
                logger.log(Level.WARNING, "Session creation failed: code=" + code + "; reason=" + method.getStatusLine().getReasonPhrase());
                return false;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Session creation failed", e);
            return false;
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public boolean checkDomainAndProject() {
        GetMethod method = new GetMethod(projectPrefix() + "/" + DOMAIN_PROJECT_CHECK_RESOURCE);
        try {
            int code = httpClient.executeMethod(method);
            if (code == 200) {
                return true;
            } else {
                logger.log(Level.WARNING, "Domain and project check failed: code=" + code + "; reason=" + method.getStatusLine().getReasonPhrase());
                return false;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Domain and project check failed", e);
            return false;
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public int post(String projectPath, File file, String contentType) throws IOException {
        PostMethod method = new PostMethod(apiProjectPrefix() + "/" + projectPath);
        try {
            method.setRequestEntity(new FileRequestEntity(file, contentType));
            return httpClient.executeMethod(method);
        } finally {
            method.releaseConnection();
        }
    }

    private String projectPrefix() {
        return location + "/rest/domains/" + domain + "/projects/" + project;
    }

    private String apiProjectPrefix() {
        return location + "/api/domains/" + domain + "/projects/" + project;
    }

    private String createAuthenticationXml() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("alm-authentication");
        root.addElement("user").addText(StringUtils.defaultIfEmpty(username, ""));
        root.addElement("password").addText(StringUtils.defaultIfEmpty(password, ""));
        return document.asXML();
    }

    private String createSessionXml() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("session-parameters");
        root.addElement("client-type").addText("octane-jenkins-plugin");
        root.addElement("time-out").addText("6");
        return document.asXML();
    }
}
