package com.hp.mqm.clt;

import com.hp.mqm.clt.tests.TestResultPushStatus;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RestClient {

    // CODE REVIEW, Johnny, 19Oct2015 - verify the authentication mechanism with Vojta once the authentication API is
    // consumed by Jenkins MQM REST client
    private static final String URI_AUTHENTICATION = "authentication/sign_in";
    private static final String HEADER_NAME_AUTHORIZATION = "Authorization";
    private static final String HEADER_VALUE_BASIC_AUTH = "Basic ";
    private static final String HEADER_CLIENT_TYPE = "HPE_CLIENT_TYPE"; // TODO rename ????
    static final String URI_LOGOUT = "authentication/sign_out";

    private static final String SHARED_SPACE_API_URI = "api/shared_spaces/{0}";
    private static final String WORKSPACE_API_URI = SHARED_SPACE_API_URI + "/workspaces/{1}";

    private static final String URI_TEST_RESULT_PUSH = "test-results?skip-errors={0}";
    private static final String URI_TEST_RESULT_STATUS = "test-results/{0}";

    private static final String URI_PARAM_ENCODING = "UTF-8";

    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static final int DEFAULT_CONNECTION_TIMEOUT = 20000; // in milliseconds
    public static final int DEFAULT_SO_TIMEOUT = 40000; // in milliseconds

    private static final String CLIENT_TYPE = "HPE_COLLECTION_TOOL";

    private CloseableHttpClient httpClient;
    private Settings settings;

    private volatile boolean isLoggedIn = false;

    public RestClient(Settings settings) {
        this.settings = settings;

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setSocketTimeout(DEFAULT_SO_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);

        // proxy setting
        if (StringUtils.isNotEmpty(settings.getProxyHost())) {
            HttpHost proxy = new HttpHost(settings.getProxyHost(), settings.getProxyPort());
            requestConfigBuilder.setProxy(proxy);
            if (settings.getProxyUser() != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(settings.getProxyHost(), settings.getProxyPort()),
                        new UsernamePasswordCredentials(settings.getProxyUser(), settings.getProxyPassword()));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        httpClient = httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build()).build();
    }

    public long postTestResult(HttpEntity entity) throws IOException, ValidationException {
        HttpPost request = new HttpPost(createWorkspaceApiUri(URI_TEST_RESULT_PUSH, settings.isSkipErrors()));
        request.setEntity(entity);
        CloseableHttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
                String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                JSONObject jsonObject = new JSONObject(json);
                String description = "";
                if (jsonObject.has("description")) {
                    description = ": " + jsonObject.getString("description");
                }
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST && settings.isInternal()) {
                    // Error was probably caused by XSD validation failure
                    throw new ValidationException("Test result XML was refused by server" + description);
                }
                throw new RuntimeException("Test result post failed with status code (" +
                        response.getStatusLine().getStatusCode() + ")" + description);
            }
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getLong("id");
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    public TestResultPushStatus getTestResultStatus(long id) {
        HttpGet request = new HttpGet(createWorkspaceApiUri(URI_TEST_RESULT_STATUS, id));
        CloseableHttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Result status retrieval failed");
            }
            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            Date until = null;
            if (jsonObject.has("until")) {
                try {
                    until = parseDatetime(jsonObject.getString("until"));
                } catch (ParseException e) {
                    throw new RuntimeException("Cannot obtain status", e);
                }
            }
            return new TestResultPushStatus(jsonObject.getString("status"), until);
        } catch (IOException e) {
            throw new RuntimeException("Cannot obtain status.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    protected CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        addClientTypeHeader(request);
        CloseableHttpResponse response = httpClient.execute(request);
        if (isLoginNecessary(response)) { // if request fails with 401 do login and execute request again
            HttpClientUtils.closeQuietly(response);
            login();
            response = httpClient.execute(request);
        }
        return response;
    }

    private boolean isLoginNecessary(HttpResponse response) {
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED;
    }

    protected synchronized void login() throws IOException {
        authenticate();
        isLoggedIn = true;
    }

    private void authenticate() throws IOException {
        HttpPost post = new HttpPost(createBaseUri(URI_AUTHENTICATION));
        String authorizationString =
                (settings.getUser() != null ? settings.getUser() : "") + ":" + (settings.getPassword() != null ? settings.getPassword() : "");
        post.setHeader(HEADER_NAME_AUTHORIZATION, HEADER_VALUE_BASIC_AUTH + Base64.encodeBase64String(authorizationString.getBytes(StandardCharsets.UTF_8)));
        addClientTypeHeader(post);

        HttpResponse response = null;
        try {
            response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Authentication failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
            }
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void release() throws IOException {
        logout();
    }

    protected synchronized void logout() throws IOException {
        if (isLoggedIn) {
            HttpPost post = new HttpPost(createBaseUri(URI_LOGOUT));
            addClientTypeHeader(post);
            HttpResponse response = null;
            try {
                response = httpClient.execute(post);
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK
                        && response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) { // required until defect #2919 is fixed
                    throw new RuntimeException("Logout failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
                }
                isLoggedIn = false;
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        }
    }

    protected URI createWorkspaceApiUri(String template, Object... params) {
        return URI.create(createBaseUri(WORKSPACE_API_URI, settings.getSharedspace(), settings.getWorkspace()).toString() + "/" + resolveTemplate(template, asMap(params)));
    }

    protected URI createWorkspaceApiUriMap(String template, Map<String, ?> params) {
        return URI.create(createBaseUri(WORKSPACE_API_URI, settings.getSharedspace(), settings.getWorkspace()).toString() + "/" + resolveTemplate(template, params));
    }

    private URI createBaseUri(String template, Object... params) {
        String result = settings.getServer() + "/" + resolveTemplate(template, asMap(params));
        return URI.create(result);
    }

    private String resolveTemplate(String template, Map<String, ?> params) {
        String result = template;
        for (String param : params.keySet()) {
            Object value = params.get(param);
            result = result.replaceAll(Pattern.quote("{" + param + "}"), encodeParam(value == null ? "" : value.toString()));
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

    private void addClientTypeHeader(HttpUriRequest request) {
        if (request.getFirstHeader(HEADER_CLIENT_TYPE) == null) {
            request.addHeader(HEADER_CLIENT_TYPE, CLIENT_TYPE);
        }
    }

    private Date parseDatetime(String datetime) throws ParseException {
        return new SimpleDateFormat(DATETIME_FORMAT).parse(datetime);
    }

    private Map<String, Object> asMap(Object... params) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < params.length; i++) {
            map.put(String.valueOf(i), params[i]);
        }
        return map;
    }
}
