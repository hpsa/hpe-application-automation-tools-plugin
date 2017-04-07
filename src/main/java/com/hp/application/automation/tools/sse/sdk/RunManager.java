package com.hp.application.automation.tools.sse.sdk;

import com.hp.application.automation.tools.common.SSEException;
import com.hp.application.automation.tools.rest.RestClient;
import com.hp.application.automation.tools.sse.common.StringUtils;
import com.hp.application.automation.tools.sse.result.PublisherFactory;
import com.hp.application.automation.tools.sse.result.model.junit.Testsuites;
import com.hp.application.automation.tools.sse.sdk.authenticator.AuthenticationTool;
import com.hp.application.automation.tools.sse.sdk.handler.PollHandler;
import com.hp.application.automation.tools.sse.sdk.handler.PollHandlerFactory;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandler;
import com.hp.application.automation.tools.sse.sdk.handler.RunHandlerFactory;

/**
 *
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 *
 */
public class RunManager {

    private RunHandler _runHandler;
    private PollHandler _pollHandler;
    private Logger _logger;
    private boolean _running = false;
    private boolean _polling = false;

    /**
     * Execute
     * @param client
     * @param args
     * @param logger
     * @return
     * @throws InterruptedException
     */
    public Testsuites execute(RestClient client, Args args, Logger logger)
            throws InterruptedException {

        Testsuites ret = null;
        _logger = logger;
        _running = true;
        if (AuthenticationTool.authenticate(client, args.getUsername(), args.getPassword(), args.getUrl(), logger)) {
            initialize(args, client);
            if (start(args)) {
                _polling = true;
                if (poll()) {
                    ret =
                            new PublisherFactory().create(
                                    client,
                                    args.getRunType(),
                                    args.getEntityId(),
                                    _runHandler.getRunId()).publish(
                                    _runHandler.getNameSuffix(),
                                    args.getUrl(),
                                    args.getDomain(),
                                    args.getProject(),
                                    logger);
                }
                _polling = false;
            }
        }

        return ret;
    }

    /**
     * Initialize
     * @param args
     * @param client
     */
    private void initialize(Args args, RestClient client) {
        String entityId = args.getEntityId();
        _runHandler = new RunHandlerFactory().create(client, args.getRunType(), entityId);
        _pollHandler = new PollHandlerFactory().create(client, args.getRunType(), entityId);
    }

    /**
     * Poll
     * @return
     * @throws InterruptedException
     */
    private boolean poll() throws InterruptedException {

        return _pollHandler.poll(_logger);
    }

    /**
     * Stop
     */
    public void stop() {

        _logger.log("Stopping run...");
        if (_runHandler != null) {
            _runHandler.stop();
            _running = false;
        }
        if (_pollHandler != null) {
            _polling = false;
        }
    }

    /**
     * Start
     * @param args
     * @return
     */
    private boolean start(Args args) {

        boolean ret = false;
        Response response =
                _runHandler.start(
                        args.getDuration(),
                        args.getPostRunAction(),
                        args.getEnvironmentConfigurationId(),
                        args.getCdaDetails());
        if (isOk(response, args)) {
            RunResponse runResponse = getRunResponse(response);
            setRunId(runResponse);
            if (runResponse.isSucceeded()) {
                ret = true;
            }
        }
        logReportUrl(ret, args);

        return ret;
    }

    /**
     * Set Run id
     * @param runResponse
     */
    private void setRunId(RunResponse runResponse) {

        String runId = runResponse.getRunId();
        if (StringUtils.isNullOrEmpty(runId)) {
            _logger.log("No run ID");
            throw new SSEException("No run ID");
        } else {
            _runHandler.setRunId(runId);
            _pollHandler.setRunId(runId);
        }
    }

    /**
     * Log report url
     * @param isSucceeded
     * @param args
     */
    private void logReportUrl(boolean isSucceeded, Args args) {

        if (isSucceeded) {
            _logger.log(String.format(
                    "%s run report for run id %s is at: %s",
                    args.getRunType(),
                    _runHandler.getRunId(),
                    _runHandler.getReportUrl(args)));
        } else {
            _logger.log(String.format(
                    "Failed to start %s ID:%s, run id: %s "
                            + "\nNote: You can run only functional test sets and build verification suites using this plugin. "
                            + "Check to make sure that the configured ID is valid "
                            + "(and that it is not a performance test ID).",
                    args.getRunType(),
                    args.getEntityId(),
                    _runHandler.getRunId()));
        }
    }

    /**
     * Get run response
     * @param response
     * @return
     */
    private RunResponse getRunResponse(Response response) {

        return _runHandler.getRunResponse(response);
    }

    /**
     * Is ok
     * @param response
     * @param args
     * @return
     */
    private boolean isOk(Response response, Args args) {

        boolean ret = false;
        if (response.isOk()) {
            _logger.log(String.format(
                    "Executing %s ID: %s in %s/%s %sDescription: %s",
                    args.getRunType(),
                    args.getEntityId(),
                    args.getDomain(),
                    args.getProject(),
                    StringUtils.NEW_LINE,
                    args.getDescription()));
            ret = true;
        } else {
            Throwable cause = response.getFailure();
            if (cause != null) {
                _logger.log(String.format(
                        "Failed to start %s ID: %s, ALM Server URL: %s (Exception: %s)",
                        args.getRunType(),
                        args.getEntityId(),
                        args.getUrl(),
                        cause.getMessage()));
            } else {
                _logger.log(String.format(
                        "Failed to execute %s ID: %s, ALM Server URL: %s (Response: %s)",
                        args.getRunType(),
                        args.getEntityId(),
                        args.getUrl(),
                        response.getStatusCode()));
            }
        }

        return ret;
    }

    public boolean getRunning() {

        return _running;
    }

    public boolean getPolling() {

        return _polling;
    }

}