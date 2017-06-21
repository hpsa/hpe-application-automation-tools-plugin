/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests;

import com.hp.mqm.client.LogOutput;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.RequestException;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607"})
public class TestApi {

    private AbstractBuild build;
    private JenkinsMqmRestClientFactory clientFactory;

    public TestApi(AbstractBuild build, JenkinsMqmRestClientFactory clientFactory) {
        this.build = build;
        this.clientFactory = clientFactory;
    }

    public void doAudit(StaplerRequest req, StaplerResponse res) throws IOException, ServletException, InterruptedException {
        // audit log contains possibly sensitive information (location, domain and project): require configure permission
        build.getProject().getACL().checkPermission(Item.CONFIGURE);
        serveFile(res, TestDispatcher.TEST_AUDIT_FILE, Flavor.JSON);
    }

    public void doXml(StaplerRequest req, StaplerResponse res) throws IOException, ServletException, InterruptedException {
        build.getACL().checkPermission(Item.READ);
        serveFile(res, TestListener.TEST_RESULT_FILE, Flavor.XML);
    }

    public void doLog(StaplerRequest req, final StaplerResponse res) throws IOException, ServletException, InterruptedException {
        build.getACL().checkPermission(Item.READ);
        FilePath auditFile = new FilePath(new File(build.getRootDir(), TestDispatcher.TEST_AUDIT_FILE));
        if (!auditFile.exists()) {
            res.sendError(404, "Audit file is not present, log information is not available");
            return;
        }
        JSONArray audit = JSONArray.fromObject(auditFile.readToString());
        JSONObject lastAudit = audit.getJSONObject(audit.size() - 1);
        if (!lastAudit.getBoolean("pushed")) {
            res.sendError(404, "Last audited push didn't succeed, log information is not available");
            return;
        }
        long id = lastAudit.getLong("id");
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        String location = lastAudit.getString("location");
        if (!location.equals(configuration.location)) {
            res.sendError(404, "Server configuration has changed, log information is not available");
            return;
        }
        MqmRestClient restClient = clientFactory.obtain(configuration.location, configuration.sharedSpace, configuration.username, configuration.password);
        res.setStatus(200);
        try {
            restClient.getTestResultLog(id, new JenkinsLogOutput(res));
        } catch (RequestException e) {
            if ("testbox.not_found".equals(e.getErrorCode())) {
                res.sendError(404, "Log information is not available. It either expired or shared space was re-created on the server.");
                return;
            }
            throw e;
        }
    }

    private void serveFile(StaplerResponse res, String relativePath, Flavor flavor) throws IOException, InterruptedException {
        FilePath file = new FilePath(new File(build.getRootDir(), relativePath));
        if (!file.exists()) {
            res.sendError(404, "Information not available");
            return;
        }
        res.setStatus(200);
        res.setContentType(flavor.contentType);
        InputStream is = file.read();
        IOUtils.copy(is, res.getOutputStream());
        IOUtils.closeQuietly(is);
    }

    private static class JenkinsLogOutput implements LogOutput {

        private StaplerResponse res;

        JenkinsLogOutput(StaplerResponse res) {
            this.res = res;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return res.getOutputStream();
        }

        @Override
        public void setContentType(String contentType) {
            res.setContentType(contentType);
        }
    }
}
