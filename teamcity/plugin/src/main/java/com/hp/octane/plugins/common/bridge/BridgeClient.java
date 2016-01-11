package com.hp.octane.plugins.common.bridge;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.MqmRestClientImpl;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.TemporarilyUnavailableException;
import com.hp.octane.plugins.common.bridge.tasks.CITaskService;
import com.hp.octane.plugins.common.bridge.tasks.CITaskServiceFactory;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.DummyPluginConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.client.TeamCityMqmRestClientFactory;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;


public class BridgeClient {
    private static final Logger logger = Logger.getLogger(BridgeClient.class.getName());
    private static final String serverInstanceId = DummyPluginConfiguration.identity;//UUID.randomUUID().toString();//
    private static final String ciLocation = "http://localhost:8888/httpAuth";//

    private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
    private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
    volatile private boolean shuttingDown = false;

    private ServerConfiguration mqmConfig;
    private TeamCityMqmRestClientFactory restClientFactory;
    private CITaskService ciTaskService = CITaskServiceFactory.create(TeamCityMqmRestClientFactory.CLIENT_TYPE);

    public BridgeClient(ServerConfiguration mqmConfig, TeamCityMqmRestClientFactory restClientFactory) {
        this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
        this.restClientFactory = restClientFactory;
        connect();
        logger.info("BRIDGE: client initialized for '" + this.mqmConfig.location + "' (SP: " + this.mqmConfig.sharedSpace + ")");
    }

    public void update(ServerConfiguration newConfig) {
        mqmConfig = new ServerConfiguration(newConfig.location, newConfig.sharedSpace, newConfig.username, newConfig.password, newConfig.impersonatedUser);
        logger.info("BRIDGE: updated for '" + mqmConfig.location + "' (SP: " + mqmConfig.sharedSpace + ")");
        connect();
    }

    private void connect() {
        if (!shuttingDown) {
            connectivityExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    String tasksJSON;
                    try {
                        logger.info("BRIDGE: connecting to '" + mqmConfig.location +
                                "' (SP: " + mqmConfig.sharedSpace +
                                "; instance ID: " + serverInstanceId +
                                "; self URL: " + ciLocation);//new PluginActions.ServerInfo().getUrl());
                        MqmRestClient restClient = restClientFactory.create(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);

                        tasksJSON = restClient.getAbridgedTasks(serverInstanceId, ciLocation/*new PluginActions.ServerInfo().getUrl()*/);
                        logger.info("BRIDGE: back from '" + mqmConfig.location + "' (SP: " + mqmConfig.sharedSpace + ") with " + (tasksJSON == null || tasksJSON.isEmpty() ? "no tasks" : "some tasks"));
                        connect();
                        if (tasksJSON != null && !tasksJSON.isEmpty()) {
                            dispatchTasks(tasksJSON);
                        }
                    } catch (AuthenticationException ae) {
                        logger.severe("BRIDGE: connection to MQM Server temporary failed: authentication error");
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException ie) {
                            logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
                        }
                        connect();
                    } catch (TemporarilyUnavailableException tue) {
                        logger.severe("BRIDGE: connection to MQM Server temporary failed: resource not available");
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException ie) {
                            logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
                        }
                        connect();
                    } catch (Exception e) {
                        logger.severe("BRIDGE: connection to MQM Server temporary failed: " + e.getMessage());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
                        }
                        connect();
                    }
                }
            });
        } else if (shuttingDown) {
            logger.info("BRIDGE: bridge client stopped");
        }
    }

    void dispose() {
        //  TODO: disconnect current connection once async connectivity is possible
        shuttingDown = true;
    }

    private void dispatchTasks(String tasksJSON) {
        try {
            JSONArray tasks = JSONArray.fromObject(tasksJSON);
            logger.info("BRIDGE: going to process " + tasks.size() + " tasks");
            for (int i = 0; i < tasks.size(); i++) {
                taskProcessingExecutors.execute(new TaskProcessor(
                        tasks.getJSONObject(i),
                        restClientFactory,
                        mqmConfig,
                        ciTaskService
                ));
            }
        } catch (Exception e) {
            logger.severe("BRIDGE: failed to process tasks: " + e.getMessage());
        }
    }


    public String getLocation() {
        return mqmConfig.location;
    }


    public String getSharedSpace() {
        return mqmConfig.sharedSpace;
    }


    public String getUsername() {
        return mqmConfig.username;
    }

    private static final class AbridgedConnectivityExecutorsFactory implements ThreadFactory {

        @Override
        public Thread newThread(@Nonnull Runnable runnable) {
            Thread result = new Thread(runnable);
            result.setName("AbridgedConnectivityThread-" + result.getId());
            result.setDaemon(true);
            return result;
        }
    }

    private static final class AbridgedTasksExecutorsFactory implements ThreadFactory {

        @Override
        public Thread newThread(@Nonnull Runnable runnable) {
            Thread result = new Thread(runnable);
            result.setName("AbridgedTasksExecutorsFactory-" + result.getId());
            result.setDaemon(true);
            return result;
        }
    }
}
