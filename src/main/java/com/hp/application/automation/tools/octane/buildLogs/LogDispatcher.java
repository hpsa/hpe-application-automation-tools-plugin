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

package com.hp.application.automation.tools.octane.buildLogs;

import com.google.inject.Inject;
import com.hp.application.automation.tools.octane.ResultQueue;
import com.hp.application.automation.tools.octane.client.RetryModel;
import com.hp.application.automation.tools.octane.configuration.BdiConfiguration;
import com.hp.application.automation.tools.octane.configuration.ConfigurationService;
import com.hp.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import com.hp.mqm.client.exception.RequestErrorException;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;
import org.apache.http.HttpEntity;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Created by benmeior on 11/20/2016.
 */
@Extension
public class LogDispatcher extends AbstractSafeLoggingAsyncPeriodWork {
    private static Logger logger = LogManager.getLogger(LogDispatcher.class);

    private static final String BDI_PRODUCT = "octane";
    private static final String CONTENT_ENCODING_GZIP = "gzip";
    private static final String CLIENT_CERTIFICATE_HEADER = "X-CERT";
    private static final String SECURE_PROTOCOL = "https";
    private static final String CONSOLE_LOG_DATA_TYPE = "consolelog";
    private static final String DATA_IN_QUERY_TYPE = "data-in";
    private static final String UNCOMPRESSED_CONTENT_LENGTH_HEADER = "Uncompressed-Content-Length";
    private static final int MAX_TOTAL_CONNECTIONS = 20;
    private static final int MIN_SIZE_TO_ZIP = 1024;

    @Inject
    private RetryModel retryModel;

    @Inject
    private BdiConfigurationFetcher bdiConfigurationFetcher;

    private ResultQueue logsQueue;

    private final CloseableHttpClient httpClient;
    private ProxyConfiguration proxyConfiguration;
    private HttpClientContext clientContext;

    private String encodedPem;

    public LogDispatcher() {
        super("BDI log dispatcher");

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        if (!isPemFilePropertyInit()) {
            return;
        }
        if (logsQueue.peekFirst() == null) {
            return;
        }
        if (retryModel.isQuietPeriod()) {
            logger.info("There are pending logs, but we are in quiet period");
            return;
        }
        manageLogsQueue();
    }

    private void manageLogsQueue() {
        BdiConfiguration bdiConfiguration = bdiConfigurationFetcher.obtain();
        if (bdiConfiguration == null || !bdiConfiguration.isFullyConfigured()) {
            logger.error("Could not send logs. BDI is not configured");
            return;
        }

        ResultQueue.QueueItem item;
        while ((item = logsQueue.peekFirst()) != null) {
            Run build = getBuildFromQueueItem(item);
            if (build == null) {
                logsQueue.remove();
                continue;
            }
            String encodedPem = getEncodedPem();
            HttpResponse httpResponse = null;
            try {
                HttpClientContext clientContext = getHttpClientContext();

                String url = String.format("%s://%s:%s/rest-service/api/%s/%s?product=%s&tenantid=%d&workspace=%s&dataid=%s",
                        SECURE_PROTOCOL, bdiConfiguration.getHost(), bdiConfiguration.getPort(), CONSOLE_LOG_DATA_TYPE, DATA_IN_QUERY_TYPE,
                        BDI_PRODUCT, Long.valueOf(bdiConfiguration.getTenantId()), item.getWorkspace(), buildDataId(build));
                HttpPost request = new HttpPost(URI.create(url));

                request.setHeader(CLIENT_CERTIFICATE_HEADER, encodedPem);

                File logFile = build.getLogFile();
                if (logFile.length() > MIN_SIZE_TO_ZIP) {
                    request.setHeader(HTTP.CONTENT_ENCODING, CONTENT_ENCODING_GZIP);
                    request.setHeader(UNCOMPRESSED_CONTENT_LENGTH_HEADER, String.valueOf(logFile.length()));
                    request.setEntity(createGZipEntity(logFile));
                } else {
                    request.setEntity(new FileEntity(logFile));
                }

                httpResponse = httpClient.execute(request, clientContext);
                HttpEntity entity = httpResponse.getEntity();
                ContentType entityContentType = ContentType.getOrDefault(entity);
                Charset charset = entityContentType.getCharset() != null ? entityContentType.getCharset() : StandardCharsets.UTF_8;
                String responseString = EntityUtils.toString(entity, charset);

                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RequestErrorException(String.format("Response status: %d, Response message: %s",
                            httpResponse.getStatusLine().getStatusCode(), responseString));
                }

                logger.info(String.format("Successfully sent log of build [%s#%s]", item.getProjectName(), item.getBuildNumber()));

                logsQueue.remove();
            } catch (Exception e) {
                logger.error(String.format("Could not send log of build [%s#%s] to bdi.", item.getProjectName(), item.getBuildNumber()), e);
                if (!logsQueue.failed()) {
                    logger.warn("Maximum number of attempts reached, operation will not be re-attempted for this build");
                }
            } finally {
                if (httpResponse != null) {
                    EntityUtils.consumeQuietly(httpResponse.getEntity());
                    HttpClientUtils.closeQuietly(httpResponse);
                }
            }
        }
    }

    private HttpClientContext getHttpClientContext() {
        if (this.clientContext != null && this.proxyConfiguration == Jenkins.getInstance().proxy) {
            return this.clientContext;
        } else {
            this.clientContext = new HttpClientContext();
            RequestConfig.Builder config = RequestConfig.custom()
                    .setConnectTimeout(20 * 1000)
                    .setSocketTimeout(2 * 60 * 1000);

            proxyConfiguration = Jenkins.getInstance().proxy;
            if (proxyConfiguration != null) {
                HttpHost proxyHost = new HttpHost(proxyConfiguration.name, proxyConfiguration.port);
                config.setProxy(proxyHost);
                if (proxyConfiguration.getUserName() != null && !proxyConfiguration.getUserName().isEmpty()
                        && proxyConfiguration.getPassword() != null && !proxyConfiguration.getPassword().isEmpty()) {
                    AuthScope proxyAuthScope = new AuthScope(proxyConfiguration.name, proxyConfiguration.port);
                    Credentials credentials = new UsernamePasswordCredentials(proxyConfiguration.getUserName(), proxyConfiguration.getPassword());

                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(proxyAuthScope, credentials);
                }
            }
            this.clientContext.setRequestConfig(config.build());
            return this.clientContext;
        }
    }

    private Run getBuildFromQueueItem(ResultQueue.QueueItem item) {
        Job project = (Job) Jenkins.getInstance().getItemByFullName(item.getProjectName());
        if (project == null) {
            logger.warn("Project [" + item.getProjectName() + "] no longer exists, pending logs can't be submitted");
            return null;
        }

        Run build = project.getBuildByNumber(item.getBuildNumber());
        if (build == null) {
            logger.warn("Build [" + item.getProjectName() + "#" + item.getBuildNumber() + "] no longer exists, pending logs can't be submitted");
            return null;
        }
        return build;
    }

    private String buildDataId(Run build) {
        String ciServerId = ConfigurationService.getModel().getIdentity();
        String ciBuildId = String.valueOf(build.getNumber());
        String jobName = build.getParent().getName();

        return String.format("%s-%s-%s", ciServerId, ciBuildId, jobName.replaceAll(" ", ""));
    }

    @Override
    public long getRecurrencePeriod() {
        String value = System.getProperty("BDI.LogDispatcher.Period"); // let's us config the recurrence period. default is 10 seconds.
        if (!StringUtils.isEmpty(value)) {
            return Long.valueOf(value);
        }
        return TimeUnit2.SECONDS.toMillis(10);
    }

    void enqueueLog(String projectName, int buildNumber, String workspace) {
        logsQueue.add(projectName, buildNumber, workspace);
    }

    @Inject
    public void setLogResultQueue(LogAbstractResultQueue queue) {
        this.logsQueue = queue;
    }

    private String getEncodedPem() {
        if (this.encodedPem != null) {
            return this.encodedPem;
        } else {
            String path = System.getProperty("pem_file");

            try (Reader reader = new FileReader(path)) {
                String pem = readToString(reader);
                this.encodedPem = DatatypeConverter.printBase64Binary(pem.getBytes());
            } catch (IOException ex) {
                logger.error("cannot read pem file from path: " + path);
            }
            return this.encodedPem;
        }
    }

    private String readToString(Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            return builder.toString();
        }
    }

    private ByteArrayEntity createGZipEntity(File file) {
        try (FileInputStream inputStream = new FileInputStream(file);
             ByteArrayOutputStream arr = new ByteArrayOutputStream()) {
            try (GZIPOutputStream zipper = new GZIPOutputStream(arr)) {
                byte[] buffer = new byte[1024];

                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    zipper.write(buffer, 0, len);
                }
            }

            return new ByteArrayEntity(arr.toByteArray(), ContentType.APPLICATION_XML);
        } catch (IOException ex) {
            throw new RequestErrorException("Failed to create GZip entity.", ex);
        }
    }

    private boolean isPemFilePropertyInit() {
        return System.getProperty("pem_file") != null && !System.getProperty("pem_file").isEmpty();
    }
}
