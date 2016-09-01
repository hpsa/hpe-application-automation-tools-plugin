package ngaclient;

import static ngaclient.JsonUtils.fromStream;
import static ngaclient.JsonUtils.getArray;
import static ngaclient.JsonUtils.newArray;
import static ngaclient.JsonUtils.newObject;
import static ngaclient.JsonUtils.readInt;
import static ngaclient.JsonUtils.readString;
import static ngaclient.JsonUtils.toText;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class is the REST HTTP client for the NGA API. All the communication to
 * NGA is done via this class.
 *
 * @author Romulus Pa&#351;ca
 *
 */
public class NgaRestClient implements NgaClient {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String SLASH = "/";
    private static final String CURRENT_RELEASE = "current_release";
    private static final String URL = "url";
    private static final String CI_SERVER = "ci_server";
    private static final String JOBS = "jobs";
    private static final String NAME = "name";
    private static final String JOB_CI_ID = "jobCiId";
    private static final String ROOT_JOB_CI_ID = "root_job_ci_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CLIENT_ID = "client_id";
    private static final String DATA = "data";
    private static final String SERVER_TYPE = "server_type";
    private static final String INSTANCE_ID = "instance_id";

    // static String TEST_URL =
    // "http://code.nextgenalm.com:8080/api/shared_spaces/1001/workspaces/1002/";
    // static String TEST_URL =
    // "http://code.nextgenalm.com:8080/ui/?admin&p=1001/1002#/settings/sharedspace/swagger";
    static String TEST_URL = "http://code.nextgenalm.com:8080/ui/?p=1001/1002/";
    static String TEST_CLIENT_ID = "ApiTester_j2mwxkg20v4qmioy849konzd8";
    static String TEST_CLIENT_SECRET = "%ce2a952383761dM";

    private static final String HPSSO_HEADER_CSRF = "HPSSO-HEADER-CSRF";
    private static final String HPSSO_COOKIE_CSRF = "HPSSO_COOKIE_CSRF";
    private static final String LWSSO_COOKIE_KEY = "LWSSO_COOKIE_KEY";

    private static final String AUTHENTICATION_SIGN_OUT = "authentication/sign_out";
    private static final String AUTHENTICATION_SIGN_IN = "authentication/sign_in";
    private static final String TEST_REPORT = "test-results";
    private static final String CI_SERVER_REQUEST_ROOT = "ci_servers";
    private static final String PIPELINE_REQUEST_ROOT = "pipelines";
    private static final String BUILD_REQUEST_ROOT = "analytics/ci/builds";

    private final String apiUri;
    private final String serverUri;
    private final String clientId;
    private final String clientSecret;
    private final BasicCookieStore cookieStore;
    private int timeout = 15;
    private CloseableHttpClient httpClient;
    private State state = State.NOT_OPEN;
    private boolean loggedIn = false;
    private String csrfCookie;

    /**
     * Creates an NGAClient using a test url, a test client id and a test API
     * key
     */
    public NgaRestClient() {
        this(TEST_URL, TEST_CLIENT_ID, TEST_CLIENT_SECRET);
    }

    /**
     * Creates an NGAClient using the given url and credentials
     *
     * @param serverUri
     *            - it can be an NGA root API UR or an NGA browser style URI.
     * @param clientId
     *            - the client id
     * @param clientSecret
     *            - the client's secret key
     */
    public NgaRestClient(String serverUri, final String clientId, final String clientSecret) {
        System.out.println("Creating NGaRestClient for " + serverUri);
        URI uri = URI.create(serverUri);
        if (uri.getPath().startsWith("/ui")) {
            serverUri = fromBrowserUrl(serverUri);
            uri = URI.create(serverUri);
        }
        this.apiUri = serverUri.endsWith(SLASH) ? serverUri : serverUri + SLASH;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.cookieStore = new BasicCookieStore();
        this.serverUri = uri.getScheme() + "://" + uri.getAuthority() + SLASH;
    }

    /**
     * Parses a browser style'e URI in order to figure out the root URI for
     * sending API requests
     *
     * @param browserUrl
     */
    private static String fromBrowserUrl(final String browserUrl) {
        try {
            final URI uri = URI.create(browserUrl);
            final Map<String, String> parseParams = parseParams(uri.getQuery());
            final String pIds = parseParams.get("p");
            if (pIds == null) {
                throw new IllegalArgumentException("No parameters for shared space and workspace id");
            }
            final String[] ids = pIds.split(SLASH);
            if (ids.length != 2) {
                throw new IllegalArgumentException(
                        "You must specify exactly to ids. One for sharespace and one for workspace " + pIds);
            }
            final int sharedSpaceId = Integer.parseInt(ids[0]);
            final int workwspaceId = Integer.parseInt(ids[1]);
            final String serverBaseUri = uri.getScheme() + "://" + uri.getAuthority();
            return String.format(serverBaseUri + "/api/shared_spaces/%d/workspaces/%d/", sharedSpaceId, workwspaceId);
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid Id  " + ex.getMessage());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid browser url " + browserUrl + " " + e.getMessage());
        }
    }

    /**
     * Login operation, the equivalent of the following curl command curl -H
     * "Content-Type: application/json" -X POST -d '{"client_id":
     * "ApiTester_j2mwxkg20v4qmioy849konzd8", "client_secret":
     * "%ce2a952383761dM"}'
     * http://code.nextgenalm.com:8080/authentication/sign_in
     *
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @Override
    public boolean login() throws ClientProtocolException, IOException {

        final RequestConfig globalConfig = RequestConfig.custom().setConnectTimeout(this.timeout * 1000)
                .setConnectionRequestTimeout(this.timeout * 1000).setSocketTimeout(this.timeout * 1000).build();

        final SSLContextBuilder builder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf;
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(builder.build(),
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();

            final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(2000);// max connection
            this.httpClient = HttpClientBuilder.create().setDefaultCookieStore(this.cookieStore)
                    .setSSLSocketFactory(sslsf).setConnectionManager(cm).setDefaultRequestConfig(globalConfig).build();
        } catch (final KeyManagementException e) {
            e.printStackTrace();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (final KeyStoreException e) {
            e.printStackTrace();
        }

        // System.setProperty("jsse.enableSNIExtension", "false"); //""
        // httpClient =
        // HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setDefaultRequestConfig(globalConfig)
        // .build();
        final AtomicBoolean success = new AtomicBoolean(false);

        final HttpPost httpPost = new HttpPost(this.serverUri + AUTHENTICATION_SIGN_IN);
        final StringEntity requestEntity = new StringEntity(toText(buildLoginRequest()), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        System.out.println("Executing request " + httpPost.getRequestLine());
        final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    success.set(true);
                    NgaRestClient.this.loggedIn = true;
                    NgaRestClient.this.state = State.OPEN;
                    final HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    final String responseText = EntityUtils.toString(response.getEntity());
                    System.out.println(responseText);
                    return responseText;
                }
            }

        };
        this.httpClient.execute(httpPost, responseHandler);
        this.csrfCookie = null;
        for (final Cookie cookie : this.cookieStore.getCookies()) {
            if (cookie.getName().equals("HPSSO_COOKIE_CSRF")) {
                this.csrfCookie = cookie.getValue();
                break;
            }
        }
        // System.out.println("login " + success);
        return success.get();
    }

    /**
     * Builds a JSON login request
     */
    private ObjectNode buildLoginRequest() {
        final ObjectNode objectNode = newObject();
        objectNode.put(CLIENT_ID, this.clientId);
        objectNode.put(CLIENT_SECRET, this.clientSecret);
        return objectNode;
    }

    /**
     * curl -X GET --header 'Accept: application/json' --header
     * 'HPSSO-HEADER-CSRF: 1fmpgh1i1vsci0omcgqgsvv9bb'
     * 'http://code.nextgenalm.com:8080/api/shared_spaces/1001/workspaces/1002/
     * ci_servers'
     *
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Override
    public ServerInfo[] getServers() throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return new ServerInfo[0];
        }

        final HttpGet httpGet = new HttpGet(this.apiUri + CI_SERVER_REQUEST_ROOT);
        httpGet.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        System.out.println("Executing request " + httpGet.getRequestLine());
        final ResponseHandler<ServerInfo[]> responseHandler = new ResponseHandler<ServerInfo[]>() {
            @Override
            public ServerInfo[] handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                if (status >= 200 && status < 300) {
                    final int numberOfServers = readInt(jsonResult, "total_count");
                    final ServerInfo[] servers = new ServerInfo[numberOfServers];
                    final ArrayNode serversAsJson = getArray(jsonResult, DATA);
                    for (int i = 0; i < numberOfServers; i++) {
                        final JsonNode serverNode = serversAsJson.get(i);
                        servers[i] = ServerInfo.fromJson((ObjectNode) serverNode);
                    }
                    return servers;
                } else {
                    handleErrrorResponse(jsonResult); // always throws
                                                      // exception
                    return null; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpGet, responseHandler);
    }

    /**
     * curl -X POST --header 'Content-Type: application/json' --header 'Accept:
     * application/json' --header 'HPSSO-HEADER-CSRF:
     * 1fmpgh1i1vsci0omcgqgsvv9bb' -d '{ "data": [ { "instance_id": "RommyTest",
     * "name": "Rommy", "url": "codepipeline/romy", "server_type": "AllForTest"
     * } ] }'
     * 'http://code.nextgenalm.com:8080/api/shared_spaces/1001/workspaces/1002/
     * ci_servers'
     */
    @Override
    public ServerInfo createServer(final String instanceId, final String name, final String url,
            final String serverType) throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return null;
        }
        final HttpPost httpPost = new HttpPost(this.apiUri + CI_SERVER_REQUEST_ROOT);
        httpPost.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpPost.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        final JsonNode serverInfo = buildCreateServerRequest(instanceId, name, url, serverType);
        final StringEntity requestEntity = new StringEntity(toText(serverInfo), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        System.out.println("Executing request " + httpPost.getRequestLine());
        System.out.println("Server info is " + toText(serverInfo));
        final ResponseHandler<ServerInfo> responseHandler = new ResponseHandler<ServerInfo>() {
            @Override
            public ServerInfo handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                if (status >= 200 && status < 300) {
                    final int numberOfServers = readInt(jsonResult, "total_count");
                    if (numberOfServers != 1) {
                        // some error happen
                        throw new NgaResponseException("Expected one new server got " + numberOfServers,
                                JsonUtils.toText(jsonResult));
                    }
                    final ArrayNode serversAsJson = getArray(jsonResult, DATA);
                    if (serversAsJson.size() != 1) {
                        throw new NgaResponseException("Expected one new server got " + serversAsJson.size(),
                                JsonUtils.toText(serversAsJson));
                    }
                    return ServerInfo.fromJson((ObjectNode) serversAsJson.get(0));
                } else {
                    handleMultipleErrrorsResponse(jsonResult); // always throws
                    // exception
                    return null; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpPost, responseHandler);
    }

    private JsonNode buildCreateServerRequest(final String instanceId, final String name, final String url,
            final String serverType) {
        final ObjectNode requestNode = newObject();
        final ArrayNode dataNode = newArray();
        final ObjectNode serverNode = newObject();
        serverNode.put(INSTANCE_ID, instanceId);
        serverNode.put(NAME, name);
        serverNode.put(URL, url);
        serverNode.put(SERVER_TYPE, serverType);
        dataNode.add(serverNode);
        requestNode.set(DATA, dataNode);
        return requestNode;
    }

    /**
     * curl -X DELETE --header 'Accept: application/json' --header
     * 'HPSSO-HEADER-CSRF: 1fmpgh1i1vsci0omcgqgsvv9bb'
     * 'http://code.nextgenalm.com:8080/api/shared_spaces/1001/workspaces/1002/
     * ci_servers/1006'
     */
    @Override
    public boolean deleteServer(final int id) throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return false;
        }
        final HttpDelete httpDelete = new HttpDelete(this.apiUri + CI_SERVER_REQUEST_ROOT + SLASH + id);
        httpDelete.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        System.out.println("Executing request " + httpDelete.getRequestLine());
        final ResponseHandler<Boolean> responseHandler = new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return true;
                } else {
                    final HttpEntity entity = response.getEntity();
                    final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                    handleErrrorResponse(jsonResult); // always throws
                    // exception
                    return false; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpDelete, responseHandler);
    }

    @Override
    public PipelineInfo createPipeline(final String name, final EntityInfo server, final JobInfo[] jobIds,
            final String rootJobId) throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return null;
        }
        validateJobIds(jobIds, rootJobId);
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Blank name for pipeline");
        }
        final HttpPost httpPost = new HttpPost(this.apiUri + PIPELINE_REQUEST_ROOT);
        httpPost.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpPost.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        final JsonNode pipelineNode = buildCreatePipelineRequest(name, server, jobIds, rootJobId);
        final StringEntity requestEntity = new StringEntity(toText(pipelineNode), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        System.out.println("Executing request " + httpPost.getRequestLine());
        System.out.println("Pipeline info is " + toText(pipelineNode));
        final ResponseHandler<PipelineInfo> responseHandler = new ResponseHandler<PipelineInfo>() {
            @Override
            public PipelineInfo handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                if (status >= 200 && status < 300) {
                    final int numberOfPipelines = readInt(jsonResult, "total_count");
                    if (numberOfPipelines != 1) {
                        // some error happen
                        throw new NgaResponseException("Expected on new pipeline got " + numberOfPipelines,
                                JsonUtils.toText(jsonResult));
                    }
                    final ArrayNode pipelinesAsJson = getArray(jsonResult, DATA);
                    if (pipelinesAsJson.size() != 1) {
                        throw new NgaResponseException("Expected on new pipeline got " + pipelinesAsJson.size(),
                                JsonUtils.toText(pipelinesAsJson));
                    }
                    return PipelineInfo.fromJson((ObjectNode) pipelinesAsJson.get(0));
                } else {
                    handleMultipleErrrorsResponse(jsonResult); // always throws
                    // exception
                    return null; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpPost, responseHandler);
    }

    private void validateJobIds(final JobInfo[] jobs, final String rootJobId) {
        if (rootJobId != null) {
            if (jobs == null || jobs.length == 0) {
                throw new IllegalArgumentException("Root job exists while job list is empty");
            } else {
                boolean found = false;
                for (final JobInfo jobInfo : jobs) {
                    if (jobInfo.getId().equals(rootJobId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IllegalArgumentException("Root job is not in the job list");
                }
            }
        } else {
            if (jobs != null && jobs.length != 0) {
                throw new IllegalArgumentException("No root job is specified ");
            }
        }
    }

    @Override
    public boolean deletePipeline(final int id) throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return false;
        }
        final HttpDelete httpDelete = new HttpDelete(this.apiUri + PIPELINE_REQUEST_ROOT + SLASH + id);
        httpDelete.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        System.out.println("Executing request " + httpDelete.getRequestLine());
        final ResponseHandler<Boolean> responseHandler = new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return true;
                } else {
                    final HttpEntity entity = response.getEntity();
                    final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                    handleErrrorResponse(jsonResult); // always throws
                    // exception
                    return false; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpDelete, responseHandler);
    }

    private JsonNode buildCreatePipelineRequest(final String name, final EntityInfo server, final JobInfo[] jobs,
            final String rootJobId) {
        final ObjectNode requestNode = newObject();
        final ArrayNode dataNode = newArray();
        final ObjectNode pipelineNode = newObject();
        pipelineNode.put(NAME, name);
        if (rootJobId != null) {
            pipelineNode.put(ROOT_JOB_CI_ID, rootJobId);
        } else {
            pipelineNode.putNull(ROOT_JOB_CI_ID);
        }
        if (jobs == null || jobs.length == 0) {

        } else {
            final ArrayNode jobsNode = newArray();
            for (final JobInfo job : jobs) {
                final ObjectNode jobNode = newObject();
                jobNode.put(JOB_CI_ID, job.getId());
                jobNode.put(NAME, job.getName());
                jobsNode.add(jobNode);
            }
            pipelineNode.set(JOBS, jobsNode);
        }
        pipelineNode.set(CI_SERVER, server.toJsonNode(newObject()));
        dataNode.add(pipelineNode);
        requestNode.set(DATA, dataNode);
        return requestNode;
    }

    @Override
    public PipelineInfo[] getPipelines() throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return new PipelineInfo[0];
        }
        final HttpGet httpGet = new HttpGet(this.apiUri + PIPELINE_REQUEST_ROOT);
        httpGet.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        System.out.println("Executing request " + httpGet.getRequestLine());
        final ResponseHandler<PipelineInfo[]> responseHandler = new ResponseHandler<PipelineInfo[]>() {
            @Override
            public PipelineInfo[] handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                if (status >= 200 && status < 300) {
                    final int numberOfServers = readInt(jsonResult, "total_count");
                    final PipelineInfo[] pipelines = new PipelineInfo[numberOfServers];
                    final ArrayNode serversAsJson = getArray(jsonResult, DATA);
                    for (int i = 0; i < numberOfServers; i++) {
                        final JsonNode serverNode = serversAsJson.get(i);
                        pipelines[i] = PipelineInfo.fromJson((ObjectNode) serverNode);
                    }
                    return pipelines;
                } else {
                    handleErrrorResponse(jsonResult); // always throws
                                                      // exception
                    return null; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpGet, responseHandler);
    }

    @Override
    public PipelineInfo updatePipeline(final int id, final String name, final EntityInfo newRelease)
            throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return null;
        }
        if ((name == null || name.trim().length() == 0) && newRelease == null) {
            throw new IllegalArgumentException("You must specified either a name or a new release");
        }
        final HttpPut httpPut = new HttpPut(this.apiUri + PIPELINE_REQUEST_ROOT + SLASH + id);
        httpPut.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpPut.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        final StringEntity requestEntity = new StringEntity(toText(buildUpdatePipelineRequest(name, newRelease)),
                ContentType.APPLICATION_JSON);
        httpPut.setEntity(requestEntity);

        System.out.println("Executing request " + httpPut.getRequestLine());
        final ResponseHandler<PipelineInfo> responseHandler = new ResponseHandler<PipelineInfo>() {
            @Override
            public PipelineInfo handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                final ObjectNode jsonResult = (ObjectNode) fromStream(entity.getContent());
                if (status >= 200 && status < 300) {
                    return PipelineInfo.fromJson(jsonResult);
                } else {
                    handleMultipleErrrorsResponse(jsonResult); // always throws
                    // exception
                    return null; // will never happen
                }
            }

        };
        return this.httpClient.execute(httpPut, responseHandler);

    }

    private JsonNode buildUpdatePipelineRequest(final String name, final EntityInfo newRelease) {
        final ObjectNode requestNode = newObject();
        if (name != null && name.trim().length() > 0) {
            requestNode.put(NAME, name);
        }
        if (newRelease != null) {
            requestNode.set(CURRENT_RELEASE, newRelease.toJsonNode(newObject()));
        }
        return requestNode;

    }

    @Override
    public void recordBuild(final BuildInfo buildInfo) throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return;
        }
        if (buildInfo == null) {
            throw new IllegalArgumentException("You must specified BuildInfo");
        } else {
            buildInfo.validate();
        }
        final HttpPut httpPut = new HttpPut(this.apiUri + BUILD_REQUEST_ROOT);
        httpPut.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpPut.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        final StringEntity requestEntity = new StringEntity(toText(buildInfo.asJson(newObject())),
                ContentType.APPLICATION_JSON);
        httpPut.setEntity(requestEntity);

        System.out.println("Executing request " + httpPut.getRequestLine());
        System.out.println("Build info is " + toText(buildInfo.asJson(newObject())));
        final ResponseHandler<Boolean> responseHandler = new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return true;
                } else {
                    throw new ClientProtocolException(response.getStatusLine().getReasonPhrase());
                }
            }

        };
        this.httpClient.execute(httpPut, responseHandler);
    }

    @Override
    public void submitTestResults(final InputStream testResult) throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return;
        }
        final HttpPost httpPost = new HttpPost(this.apiUri + TEST_REPORT);
        httpPost.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);
        httpPost.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());

        final InputStreamEntity requestEntity = new InputStreamEntity(testResult);
        httpPost.setEntity(requestEntity);
        System.out.println("Executing request " + httpPost.getRequestLine());
        final ResponseHandler<Boolean> responseHandler = new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                final String rawResponse = EntityUtils.toString(entity);
                System.out.println(rawResponse);
                if (status >= 200 && status < 300) {
                    return true;
                } else {
                    throw new ClientProtocolException(rawResponse);
                }
            }
        };
        this.httpClient.execute(httpPost, responseHandler);
    }

    @Override
    public void logout() throws ClientProtocolException, IOException {
        if (this.state != State.OPEN) {
            return;
        }
        final HttpPost httpPost = new HttpPost(this.serverUri + AUTHENTICATION_SIGN_OUT);
        httpPost.setHeader(HPSSO_HEADER_CSRF, this.csrfCookie);

        System.out.println("Executing request " + httpPost.getRequestLine());
        final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    final HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    System.out.println(EntityUtils.toString(response.getEntity()));
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        this.httpClient.execute(httpPost, responseHandler);
        // String responseBody = httpClient.execute(httpPost, responseHandler);
        this.loggedIn = false;
        this.cookieStore.clear();

    }

    @Override
    public void close() throws IOException {
        if (this.state != State.CLOSED) {
            try {
                if (this.httpClient != null) {
                    this.httpClient.close();
                    this.httpClient = null;
                }
            } finally {
                this.csrfCookie = null;
                this.loggedIn = false;
                this.cookieStore.clear();
                this.state = State.CLOSED;
            }
        }

    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public boolean isLogggedIn() {
        if (this.loggedIn) {
            int foundAndOk = 0;
            final Date now = new Date();
            for (final Cookie cookie : this.cookieStore.getCookies()) {
                final String name = cookie.getName();
                if ((name.equals(HPSSO_COOKIE_CSRF) || name.equals(LWSSO_COOKIE_KEY)) && !cookie.isExpired(now)) {
                    if (++foundAndOk == 2) {
                        break;
                    }
                }

            }
            this.loggedIn = foundAndOk == 2;
            return this.loggedIn;
        } else {
            return false;
        }
    }

    /**
     * Used for testing only
     */
    void clearCookies() {
        if (this.loggedIn) {
            this.cookieStore.clear();
        }
    }

    private void handleErrrorResponse(final ObjectNode jsonResult) {
        throw new NgaRequestException(readString(jsonResult, "error_code"), readString(jsonResult, "description"),
                JsonUtils.toText(jsonResult));

    }

    private void handleMultipleErrrorsResponse(final ObjectNode jsonResult) {
        System.out.println(JsonUtils.toText(jsonResult));
        final ArrayNode jsonErrors = getArray(jsonResult, "errors");
        if (jsonErrors.size() > 0) {
            final ObjectNode firstError = (ObjectNode) jsonErrors.get(0);
            throw new NgaRequestException(readString(firstError, "error_code"), readString(firstError, "description"),
                    JsonUtils.toText(jsonResult));
        } else {
            throw new NgaResponseException("Unknown Error message received", JsonUtils.toText(jsonResult));
        }
    }

    private static Map<String, String> parseParams(final String query) throws UnsupportedEncodingException {
        final Map<String, String> params = new LinkedHashMap<String, String>();
        final String[] pairs = query.split("&");
        for (final String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), DEFAULT_ENCODING) : pair;
            final String value = idx > 0 && pair.length() > idx + 1
                    ? URLDecoder.decode(pair.substring(idx + 1), DEFAULT_ENCODING) : null;
            params.put(key, value);
        }
        return params;
    }

    /**
     * @return the HTTP timeout in seconds
     */
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * @param timeout
     *            - the new HTTP timeout in seconds
     */
    public void setTimeout(final int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid timeout");
        }
        this.timeout = timeout;
    }

}
