// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.DomainProjectNotExistException;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.LoginException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestDispatcher extends SafeLoggingAsyncPeriodWork {

    private static Logger logger = Logger.getLogger(TestDispatcher.class.getName());

    public static final String TEST_AUDIT_FILE = "mqmTests_audit.json";

    @Inject
    private RetryModel retryModel;

    private TestResultQueue queue;

    private JenkinsMqmRestClientFactory clientFactory;

    public TestDispatcher() {
        super("MQM Test Dispatcher");
    }

    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        if (queue.peekFirst() == null) {
            return;
        }
        if (retryModel.isQuietPeriod()) {
            logger.info("There are pending test results, but we are in quiet period");
            return;
        }
        MqmRestClient client = null;
        ServerConfiguration configuration = null;
        TestResultQueue.QueueItem item;
        while ((item = queue.peekFirst()) != null) {
            if (client == null) {
                configuration = ConfigurationService.getServerConfiguration();
                if (StringUtils.isEmpty(configuration.location)) {
                    logger.warning("There are pending test results, but MQM server location is not specified, results can't be submitted");
                    return;
                }

                logger.info("There are pending test results, connecting to the MQM server");
                client = clientFactory.create(
                        configuration.location,
                        configuration.domain,
                        configuration.project,
                        configuration.username,
                        configuration.password);
                try {
                    client.tryToConnectProject();
                } catch (DomainProjectNotExistException e) {
                    logger.log(Level.WARNING, "Invalid domain or project. Pending test results can't be submitted", e);
                    retryModel.failure();
                    return;
                } catch (LoginException e) {
                    logger.log(Level.WARNING, "Login failed, pending test results can't be submitted", e);
                    retryModel.failure();
                    return;
                } catch (RequestException e) {
                    logger.log(Level.WARNING, "Problem with communication with MQM server. Pending test results can't be submitted", e);
                    retryModel.failure();
                    return;
                } catch (RequestErrorException e) {
                    logger.log(Level.WARNING, "Connection problem, pending test results can't be submitted", e);
                    retryModel.failure();
                    return;
                }
                retryModel.success();
            }

            AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(item.projectName);
            if (project == null) {
                logger.warning("Project [" + item.projectName + "] no longer exists, pending test results can't be submitted");
                queue.remove();
                continue;
            }

            AbstractBuild build = project.getBuildByNumber(item.buildNumber);
            if (build == null) {
                logger.warning("Build [" + item.projectName + "#" + item.buildNumber + "] no longer exists, pending test results can't be submitted");
                queue.remove();
                continue;
            }

            try {
                if (pushTestResults(client, build)) {
                    logger.info("Successfully pushed test results of build [" + item.projectName + "#" + item.buildNumber + "]");
                    queue.remove();
                    audit(configuration, build, true);
                } else {
                    logger.warning("Failed to push test results of build [" + item.projectName + "#" + item.buildNumber + "]");
                    if (!queue.failed()) {
                        logger.warning("Maximum number of attempts reached, operation will not be re-attempted for this build");
                    }
                    releaseClient(client);
                    client = null;
                    audit(configuration, build, false);
                }
            } catch (FileNotFoundException e) {
                logger.warning("File no longer exists, failed to push test results of build [" + item.projectName + "#" + item.buildNumber + "]");
                queue.remove();
            }
        }
        if (client != null) {
            releaseClient(client);
        }
    }

    private void releaseClient(MqmRestClient client) {
        try {
            client.release();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to release client session", e);
        }
    }

    private boolean pushTestResults(MqmRestClient client, AbstractBuild build) {
        File resultFile = new File(build.getRootDir(), TestListener.TEST_RESULT_FILE);
        try {
            client.postTestResult(resultFile);
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber() + "]", e);
            return false;
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber() + "]", e);
            return false;
        }
        return true;
    }

    private void audit(ServerConfiguration configuration, AbstractBuild build, boolean success) throws IOException, InterruptedException {
        FilePath auditFile = new FilePath(new File(build.getRootDir(), TEST_AUDIT_FILE));
        JSONArray audit;
        if (auditFile.exists()) {
            InputStream is = auditFile.read();
            audit = JSONArray.fromObject(IOUtils.toString(is, "UTF-8"));
            IOUtils.closeQuietly(is);
        } else {
            audit = new JSONArray();
        }
        JSONObject event = new JSONObject();
        event.put("success", success);
        event.put("date", DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date()));
        event.put("location", configuration.location);
        event.put("domain", configuration.domain);
        event.put("project", configuration.project);
        audit.add(event);
        auditFile.write(audit.toString(), "UTF-8");
    }

    @Override
    protected Level getNormalLoggingLevel() {
        return Level.FINE;
    }

    @Override
    public long getRecurrencePeriod() {
        String value = System.getProperty("MQM.TestDispatcher.Period");
        if (!StringUtils.isEmpty(value)) {
            return Long.valueOf(value);
        }
        return TimeUnit2.SECONDS.toMillis(10);
    }

    @Inject
    public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Inject
    public void setTestResultQueue(TestResultQueueImpl queue) {
        this.queue = queue;
    }

    /*
     * To be used in tests only.
     */
    public void _setMqmRestClientFactory(JenkinsMqmRestClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /*
     * To be used in tests only.
     */
    public void _setTestResultQueue(TestResultQueue queue) {
        this.queue = queue;
    }
}
