package com.hp.octane.plugins.jenkins.configuration;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.Messages;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import hudson.util.FormValidation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class ConfigurationServiceTest {

	@ClassRule
	public static final JenkinsRule rule = new JenkinsRule();
	private final JenkinsRule.WebClient jClient = rule.createWebClient();

	private ConfigurationService configurationService;
	private JenkinsMqmRestClientFactory clientFactory;
	private MqmRestClient client;

	@Before
	public void init() throws Exception {
		client = Mockito.mock(MqmRestClient.class);
		clientFactory = Mockito.mock(JenkinsMqmRestClientFactory.class);
		configurationService = ExtensionUtil.getInstance(rule, ConfigurationService.class);
		configurationService._setMqmRestClientFactory(clientFactory);

		HtmlPage configPage = jClient.goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8008/ui/?p=1001/1002");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.secretPassword").setValueAttribute("password");
		rule.submit(form);
	}

	@Test
	public void testGetServerConfiguration() throws Exception {
		ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
		Assert.assertEquals("http://localhost:8008", configuration.location);
		Assert.assertEquals("1001", configuration.sharedSpace);
		Assert.assertEquals("username", configuration.username);
		Assert.assertEquals("password", configuration.password);
	}

	@Test
	public void testConfigurationRoundTrip() throws Exception {
		HtmlForm formIn = jClient.goTo("configure").getFormByName("config");
		rule.submit(formIn);
		HtmlForm formOut = jClient.goTo("configure").getFormByName("config");
		Assert.assertEquals(formIn.getInputByName("_.uiLocation").getValueAttribute(), formOut.getInputByName("_.uiLocation").getValueAttribute());
		Assert.assertEquals(formIn.getInputByName("_.username").getValueAttribute(), formOut.getInputByName("_.username").getValueAttribute());
		// NOTE: password is actually empty (bug or security feature?)
		Assert.assertEquals(formIn.getInputByName("_.secretPassword").getValueAttribute(), formOut.getInputByName("_.secretPassword").getValueAttribute());
	}

	@Test
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	public void testCheckConfiguration() {
		Mockito.when(clientFactory.obtainTemp("http://localhost:8088/", "1001", "username1", "password1")).thenReturn(client);

		// valid configuration
		Mockito.doNothing().when(client).validateConfiguration();

		FormValidation validation = configurationService.checkConfiguration("http://localhost:8088/", "1001", "username1", "password1");
		Assert.assertEquals(FormValidation.Kind.OK, validation.kind);
		Assert.assertTrue(validation.getMessage().contains("Connection successful"));

		// authentication failed
		Mockito.doThrow(new AuthenticationException()).when(client).validateConfiguration();

		validation = configurationService.checkConfiguration("http://localhost:8088/", "1001", "username1", "password1");
		Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
		Assert.assertTrue(validation.getMessage().contains(Messages.AuthenticationFailure()));

		// domain project does not exists
		Mockito.doThrow(new SharedSpaceNotExistException()).when(client).validateConfiguration();

		validation = configurationService.checkConfiguration("http://localhost:8088/", "1001", "username1", "password1");
		Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
		Assert.assertTrue(validation.getMessage().contains(Messages.ConnectionSharedSpaceInvalid()));
	}
}
