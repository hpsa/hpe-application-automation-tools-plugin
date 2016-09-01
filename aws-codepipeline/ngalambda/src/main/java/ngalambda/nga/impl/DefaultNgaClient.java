package ngalambda.nga.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import ngaclient.BuildCause;
import ngaclient.BuildInfo;
import ngaclient.BuildInfo.BuildResult;
import ngaclient.BuildInfo.BuildStatus;
import ngaclient.EntityInfo;
import ngaclient.JenkinsToNga;
import ngaclient.JobInfo;
import ngaclient.NgaClient;
import ngaclient.NgaRequestException;
import ngaclient.NgaRestClient;
import ngaclient.PipelineInfo;
import ngaclient.ServerInfo;
import ngalambda.aws.NgaCodePipelineClient;

public class DefaultNgaClient implements ngalambda.nga.NgaClient {

    private final NgaClient client;

    private static final String CLIENT_ID = "CodePipeline_p6rmx8e008wqrfdqp7krpxqeg";
    private static final String CLIENT_SECRET = "?3f85a78dbff877cM";
    public int ciServerId = -1;

    @SuppressWarnings("resource")
    public static void main(final String[] args) {
        new DefaultNgaClient("http://code.nextgenalm.com:8080/ui/?p=1001/1002#almafa", CLIENT_ID, CLIENT_SECRET);
        new DefaultNgaClient("http://code.nextgenalm.com:8080/ui/?admin&p=1001/1002#kortefa", CLIENT_ID, CLIENT_SECRET);
        new DefaultNgaClient("http://code.nextgenalm.com:8080/api/shared_spaces/1001/workspaces/1002", CLIENT_ID,
                CLIENT_SECRET);

    }

    public DefaultNgaClient(final String url, final String clientId, final String clientSecret) {
        this.client = new NgaRestClient(url, clientId, clientSecret);
    }

    @Override
    public int getCiServerId() throws IOException {
        int ciId = -1;
        if (this.ciServerId != -1) {
            return this.ciServerId;
        }
        if (!this.client.isLogggedIn()) {
            if (!this.client.login()) {
                return -1;
            }
        }

        final ServerInfo[] servers = this.client.getServers();

        for (final ServerInfo server : servers) {
            if (server.getName().equals(CODEPIPELINE_CI_SERVER)) {
                ciId = server.getId();
                break;
            }
        }
        if (ciId == -1) {
            final ServerInfo result = this.client.createServer(CODEPIPELINE_CI_SERVER_ID, CODEPIPELINE_CI_SERVER,
                    CODEPIPELINE_URL, "ci_server");
            ciId = result.getId();
        }
        this.ciServerId = ciId;
        return ciId;
    }

    @Override
    public int syncPipeline(final String name, final List<JobInfo> stages) throws IOException {
        int pipelineId = -1;
        if (!this.client.isLogggedIn()) {
            if (!this.client.login()) {
                return -1;
            }
        }

        if (pipelineId == -1) {
            try {
                // try to create the pipeline
                final EntityInfo ci = new EntityInfo(this.ciServerId, "ci_server");
                final PipelineInfo info = this.client.createPipeline(name, ci,
                        stages.toArray(new JobInfo[stages.size()]), stages.get(0).getId());
                pipelineId = info.getId();
            } catch (final NgaRequestException e) {
                // if it already exists, query the pipelines and find the
                // existing one with the given name
                if ("platform.duplicate_entity_error".equals(e.getErrorCode())) {
                    final PipelineInfo[] pipelines = this.client.getPipelines();
                    for (final PipelineInfo pipeline : pipelines) {
                        if (pipeline.getName().equals(name) && pipeline.getCiServer().getId() == this.ciServerId) {
                            pipelineId = pipeline.getId();
                            break;
                        }
                    }
                }
            }
        }
        return pipelineId;
    }

    @Override
    public NgaClient getClient() {
        return this.client;
    }

    @Override
    public void close() throws Exception {
        if (this.client.isLogggedIn()) {
            this.client.logout();
        }
        this.client.close();
    }

    @Override
    public void updateBuildStatus(final String job, final String build, final BuildStatus status,
            final Instant startTime, final BuildResult result, final long duration, final String causedByJob,
            final String causedByBuild) throws IOException {
        if (!this.client.isLogggedIn()) {
            this.client.login();
        }

        final BuildInfo info = new BuildInfo();
        info.setServerCiId(CODEPIPELINE_CI_SERVER_ID);
        info.setJobInfo(new JobInfo(job));
        info.setBuildCiId(build);
        info.setDuration(Duration.ofMillis(duration));
        info.setBuildName(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(startTime)));
        info.setStatus(status);
        info.setStartTime(startTime);
        info.setResult(result);
        if (causedByJob != null && causedByBuild != null) {
            final BuildCause cause = new BuildCause(causedByJob, causedByBuild);
            info.setCauses(new BuildCause[] { cause });
        }
        this.client.recordBuild(info);
    }

    @Override
    public void submitTestResults(final InputStream input, final String buildId, final String jobId,
            final String jobName, final Instant startTime)
                    throws IOException, SAXException, ParserConfigurationException {
        final BuildInfo info = new BuildInfo();
        info.setServerCiId(ngalambda.nga.NgaClient.CODEPIPELINE_CI_SERVER_ID);
        info.setBuildCiId(buildId);
        info.setStartTime(startTime);
        info.setJobInfo(new JobInfo(jobId, jobName));
        final File tmpResults = File.createTempFile("results-", ".tmp",
                new File(NgaCodePipelineClient.NGA_CONFIG_PATH));
        try (ZipInputStream zip = new ZipInputStream(input)) {
            JenkinsToNga.transform(info, zip, new FileOutputStream(tmpResults));
            IOUtils.copy(new FileInputStream(tmpResults), System.out);
            if (!this.client.isLogggedIn()) {
                this.client.login();
            }
            this.client.submitTestResults(new FileInputStream(tmpResults));
        }
        tmpResults.delete();
    }

}
