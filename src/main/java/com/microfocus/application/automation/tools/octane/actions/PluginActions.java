/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.actions;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.Extension;
import hudson.model.RootAction;
import net.sf.json.JSONObject;
import org.apache.http.entity.ContentType;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class PluginActions implements RootAction {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String API = "/nga/api/v1";
    private static final String STATUS_REQUEST = API + "/status";
    private static final String REENQUEUE_EVENT_REQUEST = API + "/reenqueue";
    private static final String CLEAR_JOB_LIST_CACHE = API + "/clear-job-list-cache";
    private static final String CLEAR_OCTANE_ROOTS_CACHE = API + "/clear-octane-roots-cache";
    private static final String OCTANE_ROOTS_CACHE = API + "/octane-roots-cache";

    private static final String INSTANCE_ID_PARAM = "instanceId";

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "nga";
    }


    public void doDynamic(StaplerRequest req, StaplerResponse res) throws IOException {

        res.setHeader(CONTENT_TYPE, ContentType.TEXT_PLAIN.getMimeType());
        res.setStatus(200);
        if (req.getRequestURI().toLowerCase().contains(STATUS_REQUEST)) {
            JSONObject result = getStatusResult(req.getParameterMap());
            res.setHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            res.getWriter().write(result.toString());
        } else if (req.getRequestURI().toLowerCase().contains(REENQUEUE_EVENT_REQUEST)) {
            reEnqueueEvent(req.getParameterMap());
            res.getWriter().write("resent");
        } else if (req.getRequestURI().toLowerCase().contains(CLEAR_JOB_LIST_CACHE)) {
            resetJobListCache();
            res.getWriter().write("done");
        } else if (req.getRequestURI().toLowerCase().contains(CLEAR_OCTANE_ROOTS_CACHE)) {
            resetOctaneRootsCache();
            res.getWriter().write("done");
        } else if (req.getRequestURI().toLowerCase().contains(OCTANE_ROOTS_CACHE)) {
            JSONObject result = readOctaneRootsCache();
            res.getWriter().write(result.toString());
        } else {
            res.setStatus(404);
            res.getWriter().write("");
        }
    }

    private JSONObject getStatusResult(Map<String, String[]> parameterMap) {
        JSONObject sdkJson = new JSONObject();
        sdkJson.put("sdkVersion", OctaneSDK.SDK_VERSION);
        JSONObject pluginJson = new JSONObject();
        pluginJson.put("version", ConfigurationService.getPluginVersion());
        JSONObject serverInfoJson = new JSONObject();
        CIServerInfo serverInfo = CIJenkinsServicesImpl.getJenkinsServerInfo();
        serverInfoJson.put("type", serverInfo.getType());
        serverInfoJson.put("version", serverInfo.getVersion());
        serverInfoJson.put("url", serverInfo.getUrl());
        serverInfoJson.put("currentTime", format.format(new Date()));

        JSONObject result = new JSONObject();
        result.put("sdk", sdkJson);
        result.put("plugin", pluginJson);
        result.put("server", serverInfoJson);

        if (parameterMap.containsKey("metrics")) {
            JSONObject allMetricsJson = new JSONObject();
            OctaneSDK.getClients().forEach(

                    client -> {
                        JSONObject confJson = new JSONObject();
                        addMetrics(client.getMetrics(), "client", confJson);
                        addMetrics(client.getBridgeService().getMetrics(), "taskPollingService", confJson);
                        addMetrics(client.getEventsService().getMetrics(), "eventsService", confJson);
                        addMetrics(client.getTestsService().getMetrics(), "testsService", confJson);
                        addMetrics(client.getLogsService().getMetrics(), "buildLogsService", confJson);
                        addMetrics(client.getVulnerabilitiesService().getMetrics(), "vulnerabilitiesService", confJson);
                        addMetrics(client.getSonarService().getMetrics(), "sonarService", confJson);
                        addMetrics(client.getCoverageService().getMetrics(), "coverageService", confJson);
                        addMetrics(client.getSCMDataService().getMetrics(), "scmDataService", confJson);
                        addMetrics(client.getTasksProcessor().getMetrics(), "tasksProcessor", confJson);
                        addMetrics(client.getConfigurationService().getMetrics(), "configurationService", confJson);
                        addMetrics(client.getRestService().obtainOctaneRestClient().getMetrics(), "restClient", confJson);


                        allMetricsJson.put(client.getConfigurationService().getConfiguration().geLocationForLog(), confJson);
                    }
            );
            result.put("metrics", allMetricsJson);
        }

        return result;
    }

    private void addMetrics(Map<String, Object> metrics, String metricsGroup, JSONObject confJson) {
        JSONObject metricsJson = new JSONObject();
        metrics.entrySet().forEach(e -> {
            String value = e.getValue() instanceof Date ? format.format(e.getValue()) : e.getValue().toString();
            metricsJson.put(e.getKey(), value);
        });
        confJson.put(metricsGroup, metricsJson);
    }

    private void resetJobListCache() {
        OctaneSDK.getClients().stream().forEach(oc -> {
            oc.getTasksProcessor().resetJobListCache();
        });
    }

    private void resetOctaneRootsCache() {
        OctaneSDK.getClients().stream().forEach(oc -> {
            oc.getConfigurationService().resetOctaneRootsCache();
        });
    }

    private JSONObject readOctaneRootsCache() {
        JSONObject result = new JSONObject();
        OctaneSDK.getClients().forEach(
                client -> {
                    com.hp.octane.integrations.services.configuration.ConfigurationService cs = client.getConfigurationService();
                    result.put(cs.getConfiguration().geLocationForLog(), cs.getOctaneRootsCacheCollection());
                }
        );

        return result;
    }

    private void reEnqueueEvent(Map<String, String[]> parameterMap) {
        if (!parameterMap.containsKey(INSTANCE_ID_PARAM)) {
            throw new IllegalArgumentException("instanceId parameter is missing");
        }
        if (!parameterMap.containsKey("eventType")) {
            throw new IllegalArgumentException("eventType parameter is missing");
        }
        if (!parameterMap.containsKey("jobId")) {
            throw new IllegalArgumentException("jobId parameter is missing");
        }
        if (!parameterMap.containsKey("buildId")) {
            throw new IllegalArgumentException("buildId parameter is missing");
        }

        String instanceId = parameterMap.get(INSTANCE_ID_PARAM)[0];
        String eventType = parameterMap.get("eventType")[0];
        String jobId = parameterMap.get("jobId")[0];
        String buildId = parameterMap.get("buildId")[0];
        String rootId = null;
        if (parameterMap.containsKey("rootId")) {
            rootId = parameterMap.get("rootId")[0];
        }

        OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
        if ("tests".equals(eventType.toLowerCase())) {
            octaneClient.getTestsService().enqueuePushTestsResult(jobId, buildId, rootId);
        } else if ("commits".equals(eventType.toLowerCase())) {
            octaneClient.getSCMDataService().enqueueSCMData(jobId, buildId, null, null);
        }
    }
}
