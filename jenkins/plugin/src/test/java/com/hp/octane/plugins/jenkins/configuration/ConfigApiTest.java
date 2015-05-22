// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;

public class ConfigApiTest {

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @Before
    public void init() throws Exception {
        HtmlPage configPage = rule.createWebClient().goTo("configure");
        HtmlForm form = configPage.getFormByName("config");
        form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8008/qcbin/ui/?workspace-id=1001&p=domain/project");
        form.getInputByName("_.username").setValueAttribute("username");
        form.getInputByName("_.password").setValueAttribute("password");
        rule.submit(form);
    }

    @Test
    public void testRead() throws Exception {
        JenkinsRule.WebClient cli = rule.createWebClient();
        Page page = cli.goTo("octane/configuration/read", "application/json");
        String configAsString = page.getWebResponse().getContentAsString();
        JSONObject config = JSONObject.fromObject(configAsString);
        Assert.assertEquals("http://localhost:8008/qcbin", config.getString("location"));
        Assert.assertEquals("domain", config.getString("domain"));
        Assert.assertEquals("project", config.getString("project"));
        Assert.assertEquals("username", config.getString("username"));
        Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));
    }

    @Test
    public void testSave() throws Exception {
        JenkinsRule.WebClient cli = rule.createWebClient();
        URL url = cli.createCrumbedUrl("octane/configuration/save");
        WebRequestSettings request = new WebRequestSettings(url);
        request.setHttpMethod(HttpMethod.POST);

        // basic scenario: location, domain, project and credentials
        JSONObject config = new JSONObject();
        config.put("location", "http://localhost:8088/qcbin");
        config.put("domain", "domain1");
        config.put("project", "project1");
        config.put("username", "username1");
        config.put("password", "password1");
        request.setRequestBody(config.toString());
        Page page = cli.getPage(request);
        config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        checkConfig(config, "http://localhost:8088/qcbin", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));

        // location, domain and project, no credentials
        config = new JSONObject();
        config.put("location", "http://localhost:8888/qcbin");
        config.put("domain", "domain2");
        config.put("project", "project2");
        request.setRequestBody(config.toString());
        page = cli.getPage(request);
        config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        checkConfig(config, "http://localhost:8888/qcbin", "domain2", "project2", "username1", "password1");
        Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));

        // location, domain, project and username without password
        config = new JSONObject();
        config.put("location", "http://localhost:8882/qcbin");
        config.put("domain", "domain3");
        config.put("project", "project3");
        config.put("username", "username3");
        request.setRequestBody(config.toString());
        page = cli.getPage(request);
        config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        checkConfig(config, "http://localhost:8882/qcbin", "domain3", "project3", "username3", "");
        Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));

        // uiLocation and identity
        config = new JSONObject();
        config.put("uiLocation", "http://localhost:8881/qcbin/ui?workspace-id=1001&p=domain4/project4");
        config.put("serverIdentity", "2d2fa955-1d13-4d8c-947f-ab11c72bf850");
        request.setRequestBody(config.toString());
        page = cli.getPage(request);
        config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        checkConfig(config, "http://localhost:8881/qcbin", "domain4", "project4", "username3", "");
        Assert.assertEquals("2d2fa955-1d13-4d8c-947f-ab11c72bf850", config.getString("serverIdentity"));
        Assert.assertEquals("2d2fa955-1d13-4d8c-947f-ab11c72bf850", ServerIdentity.getIdentity());

        // requires POST
        request.setHttpMethod(HttpMethod.GET);
        try {
            cli.getPage(request);
            Assert.fail("Only POST should be allowed");
        } catch (FailingHttpStatusCodeException ex) {
            // expected
        }
    }

    private void checkConfig(JSONObject config, String location, String domain, String project, String username, String password) {
        // check values returned
        Assert.assertEquals(location, config.getString("location"));
        Assert.assertEquals(domain, config.getString("domain"));
        Assert.assertEquals(project, config.getString("project"));
        Assert.assertEquals(username, config.getString("username"));
        // check values stored
        ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
        Assert.assertEquals(location, serverConfiguration.location);
        Assert.assertEquals(domain, serverConfiguration.domain);
        Assert.assertEquals(project, serverConfiguration.project);
        Assert.assertEquals(username, serverConfiguration.username);
        Assert.assertEquals(password, serverConfiguration.password);
    }
}
