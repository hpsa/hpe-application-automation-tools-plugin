// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.InvalidCredentialsException;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.client.MqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class TestDispatcherTest {

    private TestQueue queue;
    private TestDispatcher testDispatcher;
    private MqmRestClientFactory clientFactory;
    private MqmRestClient restClient;
    private RetryModel retryModel;

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    private FreeStyleProject project;

    @BeforeClass
    public static void initClass() {
        System.setProperty("MQM.TestDispatcher.Period", "100");
    }

    @Before
    public void init() throws Exception {
        restClient = Mockito.mock(MqmRestClient.class);
        clientFactory = Mockito.mock(MqmRestClientFactory.class);
        Mockito.when(clientFactory.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(restClient);

        testDispatcher = ExtensionUtil.getInstance(rule, TestDispatcher.class);
        testDispatcher._setMqmRestClientFactory(clientFactory);
        queue = new TestQueue();
        testDispatcher._setTestResultQueue(queue);
        waitForTicks(1); // needed to avoid occasional interaction with the client we just overrode (race condition)

        retryModel = ExtensionUtil.getInstance(rule, RetryModel.class);
        retryModel.success();

        project = rule.createFreeStyleProject("TestDispatcher");
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));

        // server needs to be configured in order for the processing to happen
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
    public void testDispatcher() throws Exception {
        mockRestClient(restClient, true, true, true);
        FreeStyleBuild build = executeBuild();
        waitForTicks(5);
        verifyRestClient(restClient, build, true, true, true);

        mockRestClient(restClient, true, true, true);
        FreeStyleBuild build2 = executeBuild();
        waitForTicks(5);
        verifyRestClient(restClient, build2, true, true, true);
        Assert.assertEquals(0, queue.size());
    }

    @Test
    public void testDispatcherBatch() throws Exception {
        mockRestClient(restClient, true, true, true);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        FreeStyleBuild build2 = project.scheduleBuild2(0).get();
        FreeStyleBuild build3 = project.scheduleBuild2(0).get();
        queue.add(Arrays.asList(build, build2, build3));
        waitForTicks(10);

        Mockito.verify(restClient).checkDomainAndProject();
        Mockito.verify(restClient).postTestResult(new File(build.getRootDir(), "mqmTests.xml"));
        Mockito.verify(restClient).postTestResult(new File(build2.getRootDir(), "mqmTests.xml"));
        Mockito.verify(restClient).postTestResult(new File(build3.getRootDir(), "mqmTests.xml"));
        Mockito.verifyNoMoreInteractions(restClient);
        Assert.assertEquals(0, queue.size());
    }

    @Test
    public void testDispatcherLoginFailure() throws Exception {
        mockRestClient(restClient, false, true, true);
        FreeStyleBuild build = executeBuild();
        waitForTicks(5);

        verifyRestClient(restClient, build, false, false, false);
        Mockito.reset(restClient);

        executeBuild();
        waitForTicks(5);

        // in quiet period
        Mockito.verifyNoMoreInteractions(restClient);
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void testDispatcherSessionFailure() throws Exception {
        mockRestClient(restClient, true, false, true);
        FreeStyleBuild build = executeBuild();
        waitForTicks(5);

        verifyRestClient(restClient, build, true, false, false);
        Mockito.reset(restClient);

        executeBuild();
        waitForTicks(5);

        // in quiet period
        Mockito.verifyNoMoreInteractions(restClient);
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void testDispatcherProjectFailure() throws Exception {
        mockRestClient(restClient, true, true, false);
        FreeStyleBuild build = executeBuild();
        waitForTicks(5);

        verifyRestClient(restClient, build, true, true, false);
        Mockito.reset(restClient);

        executeBuild();
        waitForTicks(5);

        // in quiet period
        Mockito.verifyNoMoreInteractions(restClient);
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void testDispatcherBodyFailure() throws Exception {
        mockRestClient(restClient, true, true, true);
        FreeStyleBuild build = executeBuild();
        waitForTicks(5);

        verifyRestClient(restClient, build, true, true, true);
        mockRestClient(restClient, true, true, true);
        FreeStyleBuild build2 = executeBuild();
        waitForTicks(5);

        verifyRestClient(restClient, build2, true, true, true);
        Assert.assertEquals(0, queue.size());
    }

    private void waitForTicks(int n) throws InterruptedException {
        long current;
        synchronized (queue) {
            current = queue.getTicks();
        }
        long target = current + n;
        for (int i = 0; i < 500; i++) {
            synchronized (queue) {
                current = queue.getTicks();
                if (current >= target) {
                    return;
                }
            }
            Thread.sleep(10);
        }
        Assert.fail("Timed out: ticks: expected=" + target + "; actual=" + current);
    }

    private FreeStyleBuild executeBuild() throws ExecutionException, InterruptedException {
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        queue.add(build.getProject().getName(), build.getNumber());
        return build;
    }

    private void mockRestClient(MqmRestClient restClient, boolean login, boolean session, boolean project) throws IOException {
        Mockito.reset(restClient);
        if (!login) {
            Mockito.when(restClient.checkDomainAndProject()).thenThrow(new InvalidCredentialsException());
        } else if (!session) {
            Mockito.when(restClient.checkDomainAndProject()).thenThrow(new AuthenticationException());
        } else {
            Mockito.when(restClient.checkDomainAndProject()).thenReturn(project);
            if (project) {
                Mockito.doNothing().when(restClient).postTestResult(Mockito.argThat(new MqmTestsFileMatcher()));
            }
        }
    }

    private void verifyRestClient(MqmRestClient restClient, AbstractBuild build, boolean session, boolean project, boolean body) throws IOException {
        Mockito.verify(restClient).checkDomainAndProject();
        if (body) {
            Mockito.verify(restClient).postTestResult(new File(build.getRootDir(), "mqmTests.xml"));
        }
        Mockito.verifyNoMoreInteractions(restClient);
    }

    private static class TestQueue implements TestResultQueue {

        private LinkedList<QueueItem> queue = new LinkedList<QueueItem>();
        private long ticks;

        @Override
        public synchronized boolean isEmpty() {
            ++ticks;
            return queue.isEmpty();
        }

        @Override
        public synchronized QueueItem removeFirst() {
            return queue.removeFirst();
        }

        @Override
        public synchronized void add(String projectName, int buildNumber) {
            queue.add(new QueueItem(projectName, buildNumber));
        }

        public synchronized void add(Collection<? extends AbstractBuild> builds) {
            for (AbstractBuild build: builds) {
                queue.add(new QueueItem(build.getProject().getName(), build.getNumber()));
            }
        }

        private synchronized int size() {
            return queue.size();
        }

        public synchronized long getTicks() {
            return ticks;
        }
    }

    private static class MqmTestsFileMatcher extends BaseMatcher<File> {
        @Override
        public boolean matches(Object o) {
            return o instanceof File && ((File) o).getName().endsWith("mqmTests.xml");
        }
        @Override
        public void describeTo(Description description) {
        }
    }
}
