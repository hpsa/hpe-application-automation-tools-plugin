package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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

    private static final String BASE_REST_URI = "rest/domains/{0}/projects/{1}";

    private final CloseableHttpClient httpClient;

    private final String location;
    private final String domain;
    private final String project;
    private final String username;
    private final String password;
    private final String proxyHost;
    private final Integer proxyPort;

    private boolean alreadyLoggedIn = false;

    private String encoding = "UTF-8";

    public AbstractMqmRestClient(MqmConnectionConfig connectionConfig) {
        this.location = connectionConfig.getLocation();
        this.domain = connectionConfig.getDomain();
        this.project = connectionConfig.getProject();
        this.username = connectionConfig.getUsername();
        this.password = connectionConfig.getPassword();
        this.proxyHost = connectionConfig.getProxyHost();
        this.proxyPort = connectionConfig.getProxyPort();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        if (proxyHost != null && !proxyHost.isEmpty()) {
            clientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        }
        httpClient = clientBuilder.build();
//        List<String> authPrefs = new ArrayList<String>(2);
//        authPrefs.add(AuthPolicy.DIGEST);
//        authPrefs.add(AuthPolicy.BASIC);
//        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
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
            throw new RequestErrorException("Error occurs during logout", e); // TODO specific exception
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
            throw new AuthenticationException("Error occurs during authentication", e); // TODO specific exception
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
                String errorMessage = "Session creation failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase();
                throw new IllegalStateException(errorMessage); // TODO specific exception
            }
        } catch (IOException e) {
            throw new IllegalStateException("Session creation failed", e); // TODO specific exception
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
        root.addContent(new Element("client-type").setText("octane-jenkins-plugin"));
        root.addContent(new Element("time-out").setText("6"));
        return new XMLOutputter().outputString(document);
    }

    /**
     * Creates absolute URI given by relative path from MQM application context (template). It resolves all placeholders
     * in template according to their order in params. All parameters are URI encoded before they are used for template resolving.
     *
     * @param template URI template of relative path (template should not starts with '/') from MQM application context.
     *                 Example: test/{0}?id={1}
     * @param params   not encoded parameters of template. Example: ["J Unit", "123"]
     * @return absolute URI of endpoint with all parameters which are URI encoded. Example: http://mqm.hp.com/qcbin/test/J%20Unit?id=123
     */
    protected URI createBaseUri(String template, String... params) {
        String result = location + "/" + resolveTemplate(template, params);
        return URI.create(result);
    }

    /**
     * Creates absolute URI given by relative path from REST URI context (template). It resolves all placeholders
     * in template according to their order in params. All parameters are URI encoded before they are used for template resolving.
     *
     * @param template URI template of relative path (template should not starts with '/') from REST URI context.
     *                 Example: test/{0}?id={1}
     * @param params   not encoded parameters of template. Example: ["J Unit", "123"]
     * @return absolute URI of endpoint with all parameters which are URI encoded. Example: http://mqm.hp.com/qcbin/domains/DEFAULT/projects/MAIN/rest/test/J%20Unit?id=123
     */
    protected URI createRestUri(String template, String... params) {
        return URI.create(createBaseUri(BASE_REST_URI, domain, project).toString() + "/" + resolveTemplate(template, params));
    }

    private String resolveTemplate(String template, String... params) {
        String result = template;
        for (int i = 0; i < params.length; i++) {
            result = result.replaceAll(Pattern.quote("{" + i + "}"), encodeParam(params[i]));
        }
        return result;
    }

    private String encodeParam(String param) {
        if (param == null) {
            return null;
        }
        if (encoding == null) {
            return param;
        } else {
            try {
                return URLEncoder.encode(param, encoding).replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Unsupported encoding used for URI parameter encoding.", e);
            }
        }
    }

    /**
     * Invokes {@link org.apache.http.client.HttpClient#execute(org.apache.http.client.methods.HttpUriRequest)}
     * with given request and does login if it is necessary.
     */
    protected CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        doFirstLogin();
        CloseableHttpResponse response = httpClient.execute(request);
        if (isLoginNecessary(response)) {
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
        return response.getStatusLine().getStatusCode() != 401;
    }

    private void doFirstLogin() {
        if (!alreadyLoggedIn) {
            login();
        }
    }

}
