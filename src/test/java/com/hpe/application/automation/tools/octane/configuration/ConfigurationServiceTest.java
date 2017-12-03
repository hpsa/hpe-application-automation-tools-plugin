/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.octane.configuration;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hpe.application.automation.tools.octane.tests.ExtensionUtil;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hpe.application.automation.tools.octane.Messages;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607","squid:S2701","squid:S2698"})
public class ConfigurationServiceTest {

	@ClassRule
	public static final JenkinsRule rule = new JenkinsRule();
	private final JenkinsRule.WebClient jClient = rule.createWebClient();

	private ConfigurationParser configurationParser;
	private JenkinsMqmRestClientFactory clientFactory;
	private MqmRestClient client;
	private Secret password;

	@Before
	public void init() throws Exception {
		client = Mockito.mock(MqmRestClient.class);
		clientFactory = Mockito.mock(JenkinsMqmRestClientFactory.class);
		configurationParser = ExtensionUtil.getInstance(rule, ConfigurationParser.class);
		configurationParser._setMqmRestClientFactory(clientFactory);
		password = Secret.fromString("password");

		HtmlPage configPage = jClient.goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8008/ui/?p=1001/1002");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.password").setValueAttribute("password");
		rule.submit(form);
	}

	@Test
	public void testGetServerConfiguration() throws Exception {
		ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
		Assert.assertEquals("http://localhost:8008", configuration.location);
		Assert.assertEquals("1001", configuration.sharedSpace);
		Assert.assertEquals("username", configuration.username);
		Assert.assertEquals(password, configuration.password);
	}

	@Test
	public void testConfigurationRoundTrip() throws Exception {
		HtmlForm formIn = jClient.goTo("configure").getFormByName("config");
		rule.submit(formIn);
		HtmlForm formOut = jClient.goTo("configure").getFormByName("config");
		Assert.assertEquals(formIn.getInputByName("_.uiLocation").getValueAttribute(), formOut.getInputByName("_.uiLocation").getValueAttribute());
		Assert.assertEquals(formIn.getInputByName("_.username").getValueAttribute(), formOut.getInputByName("_.username").getValueAttribute());
		// NOTE: password is actually empty (bug or security feature?)
		Assert.assertEquals(formIn.getInputByName("_.password").getValueAttribute(), formOut.getInputByName("_.password").getValueAttribute());
	}

	@Test
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	public void testCheckConfiguration() {
		Mockito.when(clientFactory.obtainTemp("http://localhost:8088/", "1001", "username1", password)).thenReturn(client);

		// valid configuration
		Mockito.doNothing().when(client).validateConfiguration();

		FormValidation validation = configurationParser.checkConfiguration("http://localhost:8088/", "1001", "username1", password);
		Assert.assertEquals(FormValidation.Kind.OK, validation.kind);
		Assert.assertTrue(validation.getMessage().contains("Connection successful"));

		// authentication failed
		Mockito.doThrow(new AuthenticationException()).when(client).validateConfiguration();

		validation = configurationParser.checkConfiguration("http://localhost:8088/", "1001", "username1", password);
		Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
		Assert.assertTrue(validation.getMessage().contains(Messages.AuthenticationFailure()));

		// domain project does not exists
		Mockito.doThrow(new SharedSpaceNotExistException()).when(client).validateConfiguration();

		validation = configurationParser.checkConfiguration("http://localhost:8088/", "1001", "username1", password);
		Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
		Assert.assertTrue(validation.getMessage().contains(Messages.ConnectionSharedSpaceInvalid()));
	}
}
