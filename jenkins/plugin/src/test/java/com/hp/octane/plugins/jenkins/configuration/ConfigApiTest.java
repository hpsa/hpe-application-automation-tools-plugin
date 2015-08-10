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
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;

public class ConfigApiTest {

	@ClassRule
	static final public JenkinsRule rule = new JenkinsRule();
	static final private JenkinsRule.WebClient client = rule.createWebClient();

	@Before
	public void init() throws Exception {
		HtmlPage configPage = client.goTo("configure");
		HtmlForm form = configPage.getFormByName("config");
		form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8008/ui/?p=1001/1002");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.password").setValueAttribute("password");
		rule.submit(form);
	}

	@Test
	public void testRead() throws Exception {
		Page page = client.goTo("octane/configuration/read", "application/json");
		String configAsString = page.getWebResponse().getContentAsString();
		JSONObject config = JSONObject.fromObject(configAsString);
		Assert.assertEquals("http://localhost:8008", config.getString("location"));
		Assert.assertEquals("1001", config.getString("sharedSpace"));
		Assert.assertEquals("username", config.getString("username"));
		Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));
	}

	@Test
	public void testSave() throws Exception {
		URL url = client.createCrumbedUrl("octane/configuration/save");
		WebRequestSettings request = new WebRequestSettings(url);
		request.setHttpMethod(HttpMethod.POST);

		// basic scenario: location, shared space and credentials
		JSONObject config = new JSONObject();
		config.put("location", "http://localhost:8088");
		config.put("sharedSpace", "1001");
		config.put("username", "username1");
		config.put("password", "password1");
		request.setRequestBody(config.toString());
		Page page = client.getPage(request);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8088", "1001", "username1", "password1");
		Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));

		// location, shared space, no credentials
		config = new JSONObject();
		config.put("location", "http://localhost:8888");
		config.put("sharedSpace", "1002");
		request.setRequestBody(config.toString());
		page = client.getPage(request);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8888", "1002", "username1", "password1");
		Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));

		// location, shared space and username without password
		config = new JSONObject();
		config.put("location", "http://localhost:8882");
		config.put("sharedSpace", "1003");
		config.put("username", "username3");
		request.setRequestBody(config.toString());
		page = client.getPage(request);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8882", "1003", "username3", "");
		Assert.assertEquals(ServerIdentity.getIdentity(), config.getString("serverIdentity"));

		// uiLocation and identity
		config = new JSONObject();
		config.put("uiLocation", "http://localhost:8881/ui?p=1001/1002");
		config.put("serverIdentity", "2d2fa955-1d13-4d8c-947f-ab11c72bf850");
		request.setRequestBody(config.toString());
		page = client.getPage(request);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8881", "1001", "username3", "");
		Assert.assertEquals("2d2fa955-1d13-4d8c-947f-ab11c72bf850", config.getString("serverIdentity"));
		Assert.assertEquals("2d2fa955-1d13-4d8c-947f-ab11c72bf850", ServerIdentity.getIdentity());

		// requires POST
		request.setHttpMethod(HttpMethod.GET);
		try {
			client.getPage(request);
			Assert.fail("Only POST should be allowed");
		} catch (FailingHttpStatusCodeException ex) {
			// expected
		}
	}

	private void checkConfig(JSONObject config, String location, String sharedSpace, String username, String password) {
		// check values returned
		Assert.assertEquals(location, config.getString("location"));
		Assert.assertEquals(sharedSpace, config.getString("sharedSpace"));
		Assert.assertEquals(username, config.getString("username"));
		// check values stored
		ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
		Assert.assertEquals(location, serverConfiguration.location);
		Assert.assertEquals(sharedSpace, serverConfiguration.sharedSpace);
		Assert.assertEquals(username, serverConfiguration.username);
		Assert.assertEquals(password, serverConfiguration.password);
	}
}
