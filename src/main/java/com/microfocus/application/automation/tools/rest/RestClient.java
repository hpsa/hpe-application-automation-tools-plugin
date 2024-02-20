/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.rest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.microfocus.application.automation.tools.common.SSEException;
import com.microfocus.application.automation.tools.sse.sdk.Client;
import com.microfocus.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.microfocus.application.automation.tools.sse.sdk.Response;
import hudson.ProxyConfiguration;

import com.microfocus.application.automation.tools.sse.sdk.HttpRequestDecorator;

import com.microfocus.adm.performancecenter.plugins.common.rest.RESTConstants;

/***
 *
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 *
 */
public class RestClient implements Client {

    private final String _serverUrl;
    protected Map<String, String> _cookies = new HashMap<String, String>();
    private final String _restPrefix;
    private final String _webuiPrefix;
    private final String _username;
    private final String XSRF_TOKEN_VALUE;

    private CookieManager cookieManager;

    /**
     * Configure SSL context for the client.
     */
    static {
        // First create a trust manager that won't care.
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Don't do anything.
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Don't do anything.
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // Don't do anything.
                return null;
            }

        };
        // Now put the trust manager into an SSLContext.
        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[] { trustManager }, null);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new SSEException(e);
        }
        
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        
        //Ignore hostname verify
        HttpsURLConnection.setDefaultHostnameVerifier(
            new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession sslSession) {
                    return true;
                }
            }
        );
    }

    /**
     * Constructor for setting rest client properties.
     */
    public RestClient(String url, String domain, String project, String username) {

        if (!url.endsWith("/")) {
            url = String.format("%s/", url);
        }
        _serverUrl = url;
        _username = username;
        _restPrefix =
                getPrefixUrl(
                        "rest",
                        String.format("domains/%s", domain),
                        String.format("projects/%s", project));
        _webuiPrefix = getPrefixUrl("webui/alm", domain, project);

        XSRF_TOKEN_VALUE = UUID.randomUUID().toString();
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            //
        }
        HttpCookie cookie = new HttpCookie("XSRF-TOKEN", XSRF_TOKEN_VALUE);
        cookie.setPath("/qcbin");
        cookieManager.getCookieStore().add(uri, cookie);
    }

    public String getXsrfTokenValue() {
        return XSRF_TOKEN_VALUE;
    }

    /**
     * Build
     * @param suffix
     * @return
     */
    @Override
    public String build(String suffix) {
        return String.format("%1$s%2$s", _serverUrl, suffix);
    }

    /**
     * Build rest request
     */
    @Override
    public String buildRestRequest(String suffix) {
        return String.format("%1$s/%2$s", _restPrefix, suffix);
    }

    /**
     * Build web ui request
     */
    @Override
    public String buildWebUIRequest(String suffix) {
        return String.format("%1$s/%2$s", _webuiPrefix, suffix);
    }

    /**
     * Http get request
     */
    @Override
    public Response httpGet(
            String url,
            String queryString,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret = null;
        try {
            ret = doHttp(RESTConstants.GET, url, queryString, null, headers, resourceAccessLevel);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    /**
     * Http post request
     */
    @Override
    public Response httpPost(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret = null;
        try {
            ret = doHttp(RESTConstants.POST, url, null, data, headers, resourceAccessLevel);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    /**
     * Http put request
     */
    @Override
    public Response httpPut(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret = null;
        try {
            ret = doHttp(RESTConstants.PUT, url, null, data, headers, resourceAccessLevel);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    /**
     * Get server url
     */
    @Override
    public String getServerUrl() {
        return _serverUrl;
    }

    /**
     * Get prefix url
     * @param protocol
     * @param domain
     * @param project
     * @return
     */
    private String getPrefixUrl(String protocol, String domain, String project) {
        return String.format("%s%s/%s/%s", _serverUrl, protocol, domain, project);
    }

    /**
     * Do http request
     */
    private Response doHttp(
            String type,
            String url,
            String queryString,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret;
        if ((queryString != null) && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection)ProxyConfiguration.open(new URL(url));
            connection.setRequestMethod(type);

            Map<String, String> decoratedHeaders = new HashMap<String, String>();
            if (headers != null) {
                decoratedHeaders.putAll(headers);
            }

            HttpRequestDecorator.decorateHeaderWithUserInfo(
                    decoratedHeaders,
                    getUsername(),
                    resourceAccessLevel);

            prepareHttpRequest(connection, decoratedHeaders, data);
            connection.connect();
            ret = retrieveHtmlResponse(connection);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    /**
     * Prepare http request
     */
    private void prepareHttpRequest(
            HttpURLConnection connnection,
            Map<String, String> headers,
            byte[] bytes) {
        setConnectionHeaders(connnection, headers);
        setConnectionData(connnection, bytes);
    }

    /**
     * Set connection data
     */
    private void setConnectionData(HttpURLConnection connnection, byte[] bytes) {

        if (bytes != null && bytes.length > 0) {
            connnection.setDoOutput(true);
            try {
                OutputStream out = connnection.getOutputStream();
                out.write(bytes);
                out.flush();
                out.close();
            } catch (Exception cause) {
                throw new SSEException(cause);
            }
        }
    }

    /**
     * Set connection headers
     */
    private void setConnectionHeaders(HttpURLConnection connnection, Map<String, String> headers) {

        if (headers != null) {
            Iterator<Entry<String, String>> headersIterator = headers.entrySet().iterator();
            while (headersIterator.hasNext()) {
                Entry<String, String> header = headersIterator.next();
                connnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    /**
     * Retrieve Html Response
     * @param connection
     *            that is already connected to its url with an http request, and that should contain
     *            a response for us to retrieve
     * @return a response from the server to the previously submitted http request
     */
    private Response retrieveHtmlResponse(HttpURLConnection connection) {

        Response ret = new Response();

        try {
            ret.setStatusCode(connection.getResponseCode());
            ret.setHeaders(connection.getHeaderFields());
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        InputStream inputStream;
        // select the source of the input bytes, first try 'regular' input
        try {
            inputStream = connection.getInputStream();
        }
        // if the connection to the server somehow failed, for example 404 or 500,
        // con.getInputStream() will throw an exception, which we'll keep.
        // we'll also store the body of the exception page, in the response data. */
        catch (Exception e) {
            inputStream = connection.getErrorStream();
            ret.setFailure(e);
        }

        // this takes data from the previously set stream (error or input)
        // and stores it in a byte[] inside the response
        ByteArrayOutputStream container = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int read;
        try {
            while ((read = inputStream.read(buf, 0, 1024)) > 0) {
                container.write(buf, 0, read);
            }
            ret.setData(container.toByteArray());
        } catch (Exception ex) {
            throw new SSEException(ex);
        }

        return ret;
    }

    @Override
    public String getUsername() {
        return _username;
    }

}
