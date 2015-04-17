package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.DomainProjectNotExistException;
import com.hp.mqm.client.exception.LoginErrorException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.exception.SessionCreationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public abstract class AbstractMqmRestClient implements BaseMqmRestClient {

    private static final String URI_AUTHENTICATION = "authentication-point/alm-authenticate";
    private static final String URI_CREATE_SESSION = "rest/site-session";
    static final String URI_LOGOUT = "authentication-point/logout";

    private static final String URI_DOMAIN_PROJECT_CHECK = "defects?query=%7Bid%5B0%5D%7D";

    private static final String PROJECT_REST_URI = "rest/domains/{0}/projects/{1}";
    private static final String PROJECT_API_URI = "api/domains/{0}/projects/{1}";

    private static final String URI_PARAM_ENCODING = "UTF-8";

    public static final int DEFAULT_CONNECTION_TIMEOUT = 20000; // in milliseconds
    public static final int DEFAULT_SO_TIMEOUT = 40000; // in milliseconds

    private final DefaultHttpClient httpClient;
    private final String location;
    private final String domain;
    private final String project;
    private final String clientType;
    private final String username;

    private final String password;

    private volatile boolean alreadyLoggedIn = false;

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

        httpClient = new DefaultHttpClient();

        httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionConfig.getDefaultConnectionTimeout() != null ?
                connectionConfig.getDefaultConnectionTimeout() : DEFAULT_CONNECTION_TIMEOUT);
        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, connectionConfig.getDefaultSocketTimeout() != null ?
                connectionConfig.getDefaultSocketTimeout() : DEFAULT_SO_TIMEOUT);

        // proxy setting
        if (connectionConfig.getProxyHost() != null && !connectionConfig.getProxyHost().isEmpty()) {
            HttpHost proxy = new HttpHost(connectionConfig.getProxyHost(), connectionConfig.getProxyPort());
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            if (connectionConfig.getProxyCredentials() != null) {
                AuthScope proxyAuthScope = new AuthScope(connectionConfig.getProxyHost(), connectionConfig.getProxyPort());
                Credentials credentials = proxyCredentialsToCredentials(connectionConfig.getProxyCredentials());
                httpClient.getCredentialsProvider().setCredentials(proxyAuthScope, credentials);
            }
        }
    }

    private Credentials proxyCredentialsToCredentials(ProxyCredentials credentials) {
        if (credentials instanceof UsernamePasswordProxyCredentials) {
            return new UsernamePasswordCredentials(((UsernamePasswordProxyCredentials) credentials).getUsername(),
                    ((UsernamePasswordProxyCredentials) credentials).getPassword());
        } else {
            throw new IllegalStateException("Unsupported proxy credentials type " + credentials.getClass().getName());
        }
    }

    /**
     * Login to MQM with given credentials and create QC session.
     *
     * @throws com.hp.mqm.client.exception.LoginException when authentication failed
     */
    protected synchronized void login() {
        authenticate();
        createSession();
        alreadyLoggedIn = true;
    }

    /**
     * Logout from MQM.
     */
    protected synchronized void logout() {
        HttpUriRequest request = new HttpGet(createBaseUri(URI_LOGOUT));
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
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

    public void release() {
        logout();
    }

    private void authenticate() {
        HttpPost post = new HttpPost(createBaseUri(URI_AUTHENTICATION));
        post.setEntity(new StringEntity(createAuthenticationXml(), ContentType.create("application/xml", "UTF-8")));

        HttpResponse response = null;
        try {
            response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new AuthenticationException("Authentication failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new LoginErrorException("Error occurred during authentication", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private void createSession() {
        HttpPost request = new HttpPost(createBaseUri(URI_CREATE_SESSION));
        request.setEntity(new StringEntity(createSessionXml(), ContentType.create("application/xml", Charset.forName("UTF-8"))));

        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 201) {
                throw new SessionCreationException("Session creation failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new LoginErrorException("Session creation failed", e);
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

    @Override
    public void tryToConnectProject() {
        login();
        checkDomainAndProject();
    }

    // the simplest implementation because we do not know if domain and project will exist in future
    private void checkDomainAndProject() {
        HttpGet request = new HttpGet(createProjectRestUri(URI_DOMAIN_PROJECT_CHECK));
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new DomainProjectNotExistException("Cannot connect to given project.");
            }
        } catch (IOException e) {
            throw new RequestErrorException("Domain and project check failed", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
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
     * with given request and it does login if it is necessary.
     * <p>
     *     Method does not support request with non-repeatable entity (see {@link HttpEntity#isRepeatable()}).
     * </p>
     * @param request which should be executed
     * @return response for given request
     * @throws IllegalArgumentException when request entity is not repeatable
     */
    protected HttpResponse execute(HttpUriRequest request) throws IOException {
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (!entity.isRepeatable()) {
                throw new IllegalArgumentException("MqmRestClient does not support non-repeatable entity (entity.isRepeatable() must be true).");
            }
        }
        doFirstLogin();
        HttpResponse response = httpClient.execute(request);
        if (isLoginNecessary(response)) { // if request fails with 401 do login and execute request again
            HttpClientUtils.closeQuietly(response);
            login();
            response = httpClient.execute(request);
        }
        return response;
    }

    /**
     * Invokes {@link org.apache.http.client.HttpClient#execute(org.apache.http.client.methods.HttpUriRequest, org.apache.http.client.ResponseHandler)}
     * with given request and does login if it is necessary.
     * <p>
     *     Method does not support request with non-repeatable entity (see {@link HttpEntity#isRepeatable()}).
     * </p>
     * @param request which should be executed
     * @return response for given request
     * @throws IllegalArgumentException when request entity is not repeatable
     */
    protected <T> T execute(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) throws IOException {
        doFirstLogin();
        final BooleanReference loginNecessary = new BooleanReference();
        loginNecessary.value = false;
        T result = httpClient.execute(request, new ResponseHandler<T>() {
            @Override
            public T handleResponse(HttpResponse response) throws IOException {
                if (isLoginNecessary(response)) {
                    loginNecessary.value = true;
                    return null;
                }
                return responseHandler.handleResponse(response);
            }
        });
        if (loginNecessary.value) {
            login();
            result = httpClient.execute(request, new ResponseHandler<T>() {
                @Override
                public T handleResponse(HttpResponse response) throws IOException {
                    return responseHandler.handleResponse(response);
                }
            });
        }
        return result;
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

    private static class BooleanReference {
        public boolean value;
    }

}
