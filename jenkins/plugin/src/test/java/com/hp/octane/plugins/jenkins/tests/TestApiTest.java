// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.client.TestEventPublisher;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import java.io.StringReader;

public class TestApiTest {

    private JenkinsMqmRestClientFactory clientFactory;
    private MqmRestClient restClient;
    private TestQueue queue;
    private TestDispatcher testDispatcher;
    private AbstractBuild build;

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @Before
    public void init() throws Exception {
        restClient = Mockito.mock(MqmRestClient.class);

        clientFactory = Mockito.mock(JenkinsMqmRestClientFactory.class);
        Mockito.when(clientFactory.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(restClient);

        testDispatcher = ExtensionUtil.getInstance(rule, TestDispatcher.class);
        testDispatcher._setMqmRestClientFactory(clientFactory);
        queue = new TestQueue();
        testDispatcher._setTestResultQueue(queue);
        TestListener testListener = ExtensionUtil.getInstance(rule, TestListener.class);
        testListener._setTestResultQueue(queue);
        queue.waitForTicks(1); // needed to avoid occasional interaction with the client we just overrode (race condition)

        TestEventPublisher testEventPublisher = new TestEventPublisher();
        RetryModel retryModel = new RetryModel(testEventPublisher);
        testDispatcher._setRetryModel(retryModel);
        testDispatcher._setEventPublisher(testEventPublisher);

        // server needs to be configured in order for the processing to happen
        HtmlPage configPage = rule.createWebClient().goTo("configure");
        HtmlForm form = configPage.getFormByName("config");
        form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8008/ui/?p=1001");
        form.getInputByName("_.username").setValueAttribute("username");
        form.getInputByName("_.password").setValueAttribute("password");
        rule.submit(form);

        FreeStyleProject project = rule.createFreeStyleProject("test-api-test");
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        build = TestUtils.runAndCheckBuild(project);
    }

    @Test
    public void testXml() throws Exception {
        JenkinsRule.WebClient cli = rule.createWebClient();
        Page testResults = cli.goTo("job/test-api-test/" + build.getNumber() + "/octane/tests/xml", "application/xml");
        TestUtils.matchTests(new TestResultIterable(new StringReader(testResults.getWebResponse().getContentAsString())), build.getStartTimeInMillis(), TestUtils.helloWorldTests);
    }

    @Test
    public void testAudit() throws Exception {
        // make sure dispatcher logic was executed
        queue.waitForTicks(2);

        JenkinsRule.WebClient cli = rule.createWebClient();
        Page auditLog = cli.goTo("job/test-api-test/" + build.getNumber() + "/octane/tests/audit", "application/json");
        JSONArray audits = JSONArray.fromObject(auditLog.getWebResponse().getContentAsString());
        Assert.assertEquals(1, audits.size());
        JSONObject audit = audits.getJSONObject(0);
        Assert.assertTrue(audit.getBoolean("success"));
        Assert.assertEquals("http://localhost:8008", audit.getString("location"));
        Assert.assertEquals("1001", audit.getString("sharedSpace"));
        Assert.assertNotNull(audit.getString("date"));
    }
}
