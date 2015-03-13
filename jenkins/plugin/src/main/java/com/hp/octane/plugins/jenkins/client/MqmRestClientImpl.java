// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MqmRestClientImpl implements MqmRestClient {

    private String location;
    private final String username;
    private final String password;

    public MqmRestClientImpl(String location, String username, String password) {
        this.location = location;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean login() {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        List<String> authPrefs = new ArrayList<String>(2);
        authPrefs.add(AuthPolicy.DIGEST);
        authPrefs.add(AuthPolicy.BASIC);
        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

        String authPoint = location +  "/authentication-point/alm-authenticate"; // NON-NLS

        try {
            PostMethod authenticate = new PostMethod(authPoint);
            authenticate.setRequestEntity(new StringRequestEntity(createAuthenticationXml(), "application/xml", "UTF-8"));
            int code = httpClient.executeMethod(authenticate);
            if (code == 200) {
                return true;
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private String createAuthenticationXml() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("alm-authentication");
        root.addElement("user").addText(username);
        root.addElement("password").addText(password);
        return document.asXML();
    }
}
