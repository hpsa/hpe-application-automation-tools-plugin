package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationErrorException;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public abstract class AbstractMqmRestClient {

    private static final String URI_AUTHENTICATION = "authentication-point/alm-authenticate";
    private static final String URI_CREATE_SESSION = "rest/site-session";
    private static final String URI_LOGOUT = "authentication-point/logout";

    private static final String URI_DOMAIN_PROJECT_CHECK = "defects?query=%7Bid%5B0%5D%7D";

    private static final String PROJECT_REST_URI = "rest/domains/{0}/projects/{1}";
    private static final String PROJECT_API_URI = "api/domains/{0}/projects/{1}";

    private static final String URI_PARAM_ENCODING = "UTF-8";

    public static final int DEFAULT_CONNECTION_TIMEOUT = 20000; // in milliseconds
    public static final int DEFAULT_SO_TIMEOUT = 40000; // in milliseconds

    private final CloseableHttpClient httpClient;
    private final String location;
    private final String domain;
    private final String project;
    private final String clientType;
    private final String username;

    private final String password;

    private boolean alreadyLoggedIn = false;

    /**
     * Constructor for AbstractMqmRestClient.
     * @param connectionConfig MQM connection configuration, Fields 'location', 'domain', 'project' and 'clientType' must not be null or empty.
     */
    protected AbstractMqmRestClient(MqmConnectionConfig connectionConfig) {
        checkNotEmpty("Parameter 'location' must not be null or empty.", connectionConfig.getLocation());
        checkNotEmpty("Parameter 'domain' must not be null or empty.", connectionConfig.getDomain());
        checkNotEmpty("Parameter 'project' must not be null or empty.", connectionConfig.getProject());
        checkNotEmpty("Parameter 'clientType' must not be null or empty.", connectionConfig.getClientType());
        this.location = connectionConfig.getLocation();
        this.domain = connectionConfig.getDomain();
        this.project = connectionConfig.getProject();
        this.clientType = connectionConfig.getClientType();

        this.username = connectionConfig.getUsername();
        this.password = connectionConfig.getPassword();

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        // default timeouts
        if (connectionConfig.getDefaultConnectionRequestTimeout() != null) {
            requestConfigBuilder.setConnectionRequestTimeout(connectionConfig.getDefaultConnectionRequestTimeout());
        }
        requestConfigBuilder.setConnectTimeout(connectionConfig.getDefaultConnectionTimeout() != null ?
                connectionConfig.getDefaultConnectionTimeout() : DEFAULT_CONNECTION_TIMEOUT);
        requestConfigBuilder.setSocketTimeout(connectionConfig.getDefaultSocketTimeout() != null ?
                connectionConfig.getDefaultSocketTimeout() : DEFAULT_SO_TIMEOUT);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfigBuilder.build());
        // proxy setting
        if (connectionConfig.getProxyHost() != null && !connectionConfig.getProxyHost().isEmpty()) {
            clientBuilder.setProxy(new HttpHost(connectionConfig.getProxyHost(), connectionConfig.getProxyPort()));

            if (connectionConfig.getProxyCredentials() != null) {
                AuthScope proxyAuthScope = new AuthScope(connectionConfig.getProxyHost(), connectionConfig.getProxyPort());
                Credentials credentials = proxyCredentialsToCredentials(connectionConfig.getProxyCredentials());
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(proxyAuthScope, credentials);
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        httpClient = clientBuilder.build();
    }

    private Credentials proxyCredentialsToCredentials(ProxyCredentials credentials) {
        if (credentials instanceof UsernamePasswordProxyCredentials) {
            return new UsernamePasswordCredentials(((UsernamePasswordProxyCredentials) credentials).getUsername(),
                    ((UsernamePasswordProxyCredentials) credentials).getPassword());
        } else {
            throw new IllegalStateException("Unsupported proxy credentials type " + credentials.getClass().getName());
        }
    }

    // the simplest implementation because we do not know if domain and project will exist in future
    public boolean checkDomainAndProject() {
        RequestBuilder requestBuilder = RequestBuilder.get(createProjectRestUri(URI_DOMAIN_PROJECT_CHECK));

        CloseableHttpResponse response = null;
        try {
            response = execute(requestBuilder.build());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RequestErrorException("Domain and project check failed", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    /**
     * Login to MQM with given credentials and create QC session.
     *
     * @throws AuthenticationException when authentication failed
     */
    protected void login() {
        authenticate();
        createSession();
        alreadyLoggedIn = true;
    }

    /**
     * Logout from MQM.
     */
    protected void logout() {
        RequestBuilder requestBuilder = RequestBuilder.get(createBaseUri(URI_LOGOUT));
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(requestBuilder.build());
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RequestException("Logout failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
            }
            alreadyLoggedIn = false;
        } catch (IOException e) {
            throw new RequestErrorException("Error occurred during logout", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private void authenticate() {
        HttpPost post = new HttpPost(createBaseUri(URI_AUTHENTICATION));
        post.setEntity(new StringEntity(createAuthenticationXml(), ContentType.create("application/xml", "UTF-8")));

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new AuthenticationException("Authentication failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new AuthenticationErrorException("Error occurred during authentication", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private void createSession() {
        RequestBuilder requestBuilder = RequestBuilder.post(createBaseUri(URI_CREATE_SESSION));
        requestBuilder.setEntity(new StringEntity(createSessionXml(), ContentType.create("application/xml", Charset.forName("UTF-8"))));

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(requestBuilder.build());
            if (response.getStatusLine().getStatusCode() != 201) {
                throw new AuthenticationException("Session creation failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new AuthenticationErrorException("Session creation failed", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private String createAuthenticationXml() {
        Document document = new Document();
        Element root = new Element("alm-authentication");
        document.setRootElement(root);
        root.addContent(new Element("user").setText(username != null ? username : ""));
        root.addContent(new Element("password").setText(password != null ? password : ""));
        return new XMLOutputter().outputString(document);
    }

    private String createSessionXml() {
        Document document = new Document();
        Element root = new Element("session-parameters");
        document.setRootElement(root);
        root.addContent(new Element("client-type").setText(clientType));
        root.addContent(new Element("time-out").setText("6")); // TODO What this timeout means? Should it configurable?
        return new XMLOutputter().outputString(document);
    }

    /**
     * Creates absolute URI given by relative path from MQM application context (template). It resolves all placeholders
     * in template according to their order in params. All parameters are URI encoded before they are used for template resolving.
     *
     * @param template URI template of relative path (template must not starts with '/') from MQM application context. Special characters
     *                 which need to be encoded must be already encoded in template.
     *                 Example: test/{0}?id={1}
     * @param params   not encoded parameters of template. Objects are converted to string by its toString() method.
     *                 Example: ["J Unit", 123]
     * @return absolute URI of endpoint with all parameters which are URI encoded. Example: http://mqm.hp.com/qcbin/test/J%20Unit?id=123
     */
    protected URI createBaseUri(String template, Object... params) {
        String result = location + "/" + resolveTemplate(template, params);
        return URI.create(result);
    }

    /**
     * Creates absolute URI given by relative path from project URI leading by 'api'. It resolves all placeholders
     * in template according to their order in params. All parameters are URI encoded before they are used for template resolving.
     *
     * @param template URI template of relative path (template must not starts with '/') from REST URI context. Special characters
     *                 which need to be encoded must be already encoded in template.
     *                 Example: test/{0}?id={1}
     * @param params   not encoded parameters of template. Objects are converted to string by its toString() method.
     *                 Example: ["J Unit", 123]
     * @return absolute URI of endpoint with all parameters which are URI encoded. Example: http://mqm.hp.com/qcbin/domains/DEFAULT/projects/MAIN/rest/test/J%20Unit?id=123
     */
    protected URI createProjectRestUri(String template, Object... params) {
        return createProjectUri(PROJECT_REST_URI, template, params);
    }

    /**
     * Creates absolute URI given by relative path from project URI leading by 'rest'. It resolves all placeholders
     * in template according to their order in params. All parameters are URI encoded before they are used for template resolving.
     *
     * @param template URI template of relative path (template must not starts with '/') from REST URI context. Special characters
     *                 which need to be encoded must be already encoded in template.
     *                 Example: test/{0}?id={1}
     * @param params   not encoded parameters of template. Objects are converted to string by its toString() method.
     *                 Example: ["J Unit", 123]
     * @return absolute URI of endpoint with all parameters which are URI encoded. Example: http://mqm.hp.com/qcbin/domains/DEFAULT/projects/MAIN/rest/test/J%20Unit?id=123
     */
    protected URI createProjectApiUri(String template, Object... params) {
        return createProjectUri(PROJECT_API_URI, template, params);
    }

    protected URI createProjectUri(String projectPartTemplate, String template, Object... params) {
        return URI.create(createBaseUri(projectPartTemplate, domain, project).toString() + "/" + resolveTemplate(template, params));
    }

    /**
     * Resolves all placeholders in template according to their order in params. All parameters are URI encoded before
     * they are used for template resolving.
     * This method works properly only if method {@link #encodeParam(String)} encodes parameters correctly (replace '{' and '}' by some other character(s)).
     * @param template URI template
     * @param params URI parameters
     * @return resolved URI template
     */
    private String resolveTemplate(String template, Object... params) {
        String result = template;
        for (int i = 0; i < params.length; i++) {
            result = result.replaceAll(Pattern.quote("{" + i + "}"), encodeParam(params[i] == null ? "" : params[i].toString()));
        }
        return result;
    }

    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, URI_PARAM_ENCODING).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding used for URI parameter encoding.", e);
        }
    }

    /**
     * Invokes {@link org.apache.http.client.HttpClient#execute(org.apache.http.client.methods.HttpUriRequest)}
     * with given request and does login if it is necessary.
     */
    protected CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        doFirstLogin();
        CloseableHttpResponse response = httpClient.execute(request);
        if (isLoginNecessary(response)) { // if request fails with 401 do login and execute request again
            login();
            response = httpClient.execute(request);
        }
        return response;
    }

    /**
     * Invokes {@link org.apache.http.client.HttpClient#execute(org.apache.http.client.methods.HttpUriRequest, org.apache.http.client.ResponseHandler)}
     * with given request and does login if it is necessary.
     */
    protected <T> T execute(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) throws IOException {
        doFirstLogin();
        return httpClient.execute(request, new ResponseHandler<T>() {
            @Override
            public T handleResponse(HttpResponse response) throws IOException {
                if (isLoginNecessary(response)) {
                    login();
                    return httpClient.execute(request, responseHandler);

                }
                return responseHandler.handleResponse(response);
            }
        });
    }

    private boolean isLoginNecessary(HttpResponse response) {
        return response.getStatusLine().getStatusCode() == 401;
    }

    private void doFirstLogin() {
        if (!alreadyLoggedIn) {
            login();
        }
    }

    private void checkNotEmpty(String msg, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

}
