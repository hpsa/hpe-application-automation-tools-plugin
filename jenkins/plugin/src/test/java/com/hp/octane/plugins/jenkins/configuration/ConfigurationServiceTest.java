// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.DomainProjectNotExistException;
import com.hp.mqm.client.exception.SessionCreationException;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.Messages;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import hudson.util.FormValidation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class ConfigurationServiceTest {

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    private ConfigurationService configurationService;
    private JenkinsMqmRestClientFactory clientFactory;
    private MqmRestClient client;

    @Before
    public void init() throws Exception {
        client = Mockito.mock(MqmRestClient.class);
        clientFactory = Mockito.mock(JenkinsMqmRestClientFactory.class);
        configurationService = ExtensionUtil.getInstance(rule, ConfigurationService.class);
        configurationService._setMqmRestClientFactory(clientFactory);

        HtmlPage configPage = rule.createWebClient().goTo("configure");
        HtmlForm form = configPage.getFormByName("config");

        form.getInputByName("_.location").setValueAttribute("http://localhost:8008/");
        form.getInputByName("_.domain").setValueAttribute("domain");
        form.getInputByName("_.project").setValueAttribute("project");
        form.getInputByName("_.username").setValueAttribute("username");
        form.getInputByName("_.password").setValueAttribute("password");
        rule.submit(form);
    }

    @Test
    public void testGetServerConfiguration() throws Exception {
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        Assert.assertEquals("http://localhost:8008/", configuration.location);
        Assert.assertEquals("domain", configuration.domain);
        Assert.assertEquals("project", configuration.project);
        Assert.assertEquals("username", configuration.username);
        Assert.assertEquals("password", configuration.password);
    }

    @Test
    public void testConfigurationRoundTrip() throws Exception {
        JenkinsRule.WebClient webClient = rule.createWebClient();
        HtmlForm formIn = webClient.goTo("configure").getFormByName("config");
        rule.submit(formIn);
        HtmlForm formOut = webClient.goTo("configure").getFormByName("config");
        Assert.assertEquals(formIn.getInputByName("_.location").getValueAttribute(), formOut.getInputByName("_.location").getValueAttribute());
        Assert.assertEquals(formIn.getInputByName("_.domain").getValueAttribute(), formOut.getInputByName("_.domain").getValueAttribute());
        Assert.assertEquals(formIn.getInputByName("_.project").getValueAttribute(), formOut.getInputByName("_.project").getValueAttribute());
        Assert.assertEquals(formIn.getInputByName("_.username").getValueAttribute(), formOut.getInputByName("_.username").getValueAttribute());
        // NOTE: password is actually empty (bug or security feature?)
        Assert.assertEquals(formIn.getInputByName("_.password").getValueAttribute(), formOut.getInputByName("_.password").getValueAttribute());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void testCheckConfiguration() {
        Mockito.when(clientFactory.create("http://localhost:8088/", "domain1", "project1", "username1", "password1")).thenReturn(client);

        // valid configuration
        Mockito.doNothing().when(client).tryToConnectProject();

        FormValidation validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.OK, validation.kind);
        Assert.assertTrue(validation.getMessage().contains("Connection successful"));

        // authentication failed
        Mockito.doThrow(new AuthenticationException()).when(client).tryToConnectProject();

        validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
        Assert.assertTrue(validation.getMessage().contains(Messages.AuthenticationFailure()));

        // cannot create session
        Mockito.doThrow(new SessionCreationException()).when(client).tryToConnectProject();

        validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
        Assert.assertTrue(validation.getMessage().contains(Messages.SessionCreationFailure()));

        // domain project does not exists
        Mockito.doThrow(new DomainProjectNotExistException()).when(client).tryToConnectProject();

        validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
        Assert.assertTrue(validation.getMessage().contains(Messages.ConnectionDomainProjectInvalid()));
    }
}
