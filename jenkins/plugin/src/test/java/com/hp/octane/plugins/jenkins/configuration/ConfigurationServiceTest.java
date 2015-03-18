// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.MqmRestClientFactory;
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
    private MqmRestClientFactory clientFactory;
    private MqmRestClient client;

    // NOTE: prefix should be mqm - bug in Jenkins test harness?
    private static final String formPrefix = "_";

    @Before
    public void init() throws Exception {
        client = Mockito.mock(MqmRestClient.class);
        clientFactory = Mockito.mock(MqmRestClientFactory.class);
        configurationService = ExtensionUtil.getInstance(rule, ConfigurationService.class);
        configurationService._setMqmRestClientFactory(clientFactory);

        HtmlPage configPage = rule.createWebClient().goTo("configure");
        HtmlForm form = configPage.getFormByName("config");

        form.getInputByName(formPrefix + ".location").setValueAttribute("http://localhost:8008/");
        form.getInputByName(formPrefix + ".domain").setValueAttribute("domain");
        form.getInputByName(formPrefix + ".project").setValueAttribute("project");
        form.getInputByName(formPrefix + ".username").setValueAttribute("username");
        form.getInputByName(formPrefix + ".password").setValueAttribute("password");
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
        Assert.assertEquals(formIn.getInputByName(formPrefix + ".location").getValueAttribute(), formOut.getInputByName(formPrefix + ".location").getValueAttribute());
        Assert.assertEquals(formIn.getInputByName(formPrefix + ".domain").getValueAttribute(), formOut.getInputByName(formPrefix + ".domain").getValueAttribute());
        Assert.assertEquals(formIn.getInputByName(formPrefix + ".project").getValueAttribute(), formOut.getInputByName(formPrefix + ".project").getValueAttribute());
        Assert.assertEquals(formIn.getInputByName(formPrefix + ".username").getValueAttribute(), formOut.getInputByName(formPrefix + ".username").getValueAttribute());
        // NOTE: password is actually empty (bug or security feature?)
        Assert.assertEquals(formIn.getInputByName(formPrefix + ".password").getValueAttribute(), formOut.getInputByName(formPrefix + ".password").getValueAttribute());
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void testCheckConfiguration() {
        Mockito.when(clientFactory.create("http://localhost:8088/", "domain1", "project1", "username1", "password1")).thenReturn(client);

        Mockito.when(client.login()).thenReturn(true);
        Mockito.when(client.createSession()).thenReturn(true);
        Mockito.when(client.checkDomainAndProject()).thenReturn(true);

        FormValidation validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.OK, validation.kind);
        Assert.assertTrue(validation.getMessage().contains("Connection successful"));

        Mockito.when(client.login()).thenReturn(false);

        validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
        Assert.assertTrue(validation.getMessage().contains("Unable to connect, check server location and user credentials"));

        Mockito.when(client.login()).thenReturn(true);
        Mockito.when(client.createSession()).thenReturn(false);

        validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
        Assert.assertTrue(validation.getMessage().contains("Unable to connect, session creation failed"));

        Mockito.when(client.createSession()).thenReturn(true);
        Mockito.when(client.checkDomainAndProject()).thenReturn(false);

        validation = configurationService.checkConfiguration("http://localhost:8088/", "domain1", "project1", "username1", "password1");
        Assert.assertEquals(FormValidation.Kind.ERROR, validation.kind);
        Assert.assertTrue(validation.getMessage().contains("Unable to connect, check your domain and project settings"));
    }
}
