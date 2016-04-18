package com.hp.mqm.client;

import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.ExceptionStackTraceParser;
import com.hp.mqm.client.exception.LoginErrorException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.exception.ServerException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.org.apache.http.*;
import com.hp.mqm.org.apache.http.client.CookieStore;
import com.hp.mqm.org.apache.http.client.CredentialsProvider;
import com.hp.mqm.org.apache.http.client.config.RequestConfig;
import com.hp.mqm.org.apache.http.client.protocol.HttpClientContext;
import com.hp.mqm.org.apache.http.cookie.Cookie;
import com.hp.mqm.org.apache.http.entity.ContentType;
import com.hp.mqm.org.apache.http.entity.StringEntity;
import com.hp.mqm.org.apache.http.impl.client.BasicCookieStore;
import com.hp.mqm.org.apache.http.impl.client.BasicCredentialsProvider;
import com.hp.mqm.org.apache.http.impl.client.CloseableHttpClient;
import com.hp.mqm.org.apache.http.impl.client.HttpClients;
import com.hp.mqm.org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import com.hp.mqm.org.apache.http.protocol.BasicHttpContext;
import com.hp.mqm.org.apache.http.protocol.HttpContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import com.hp.mqm.org.apache.http.auth.AuthScope;
import com.hp.mqm.org.apache.http.auth.Credentials;
import com.hp.mqm.org.apache.http.auth.UsernamePasswordCredentials;
import com.hp.mqm.org.apache.http.client.methods.HttpGet;
import com.hp.mqm.org.apache.http.client.methods.HttpPost;
import com.hp.mqm.org.apache.http.client.methods.HttpUriRequest;
import com.hp.mqm.org.apache.http.client.utils.HttpClientUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class AbstractMqmRestClient implements BaseMqmRestClient {
	private static final Logger logger = Logger.getLogger(AbstractMqmRestClient.class.getName());
	private static final String URI_AUTHENTICATION = "authentication/sign_in";
	private static final String HEADER_CLIENT_TYPE = "HPECLIENTTYPE";
	private static final String LWSSO_COOKIE_NAME = "LWSSO_COOKIE_KEY";
	private static final String HPSSO_COOKIE_NAME = "HPSSO_COOKIE_CSRF";
	private static final String HPSSO_HEADER_NAME = "HPSSO_HEADER_CSRF";

	private Cookie LWSSO_TOKEN = null;
	private Cookie CSRF_TOKEN = null;

	private static final String PROJECT_API_URI = "api/shared_spaces/{0}";
	private static final String SHARED_SPACE_INTERNAL_API_URI = "internal-api/shared_spaces/{0}";
	private static final String SHARED_SPACE_API_URI = "api/shared_spaces/{0}";
	private static final String WORKSPACE_API_URI = SHARED_SPACE_API_URI + "/workspaces/{1}";
	private static final String WORKSPACE_INTERNAL_API_URI = SHARED_SPACE_INTERNAL_API_URI + "/workspaces/{1}";
	private static final String FILTERING_FRAGMENT = "query={query}";
	private static final String PAGING_FRAGMENT = "offset={offset}&limit={limit}";
	private static final String ORDER_BY_FRAGMENT = "order_by={order}";

	private static final String URI_PARAM_ENCODING = "UTF-8";

	public static final int DEFAULT_CONNECTION_TIMEOUT = 20000; // in milliseconds
	public static final int DEFAULT_SO_TIMEOUT = 40000; // in milliseconds

	private CloseableHttpClient httpClient;
	private CookieStore cookieStore;
	private final String clientType;
	private final String location;
	private final String sharedSpace;
	private final String username;
	private final String password;

	/**
	 * Constructor for AbstractMqmRestClient.
	 *
	 * @param connectionConfig MQM connection configuration, Fields 'location', 'domain', 'project' and 'clientType' must not be null or empty.
	 */
	protected AbstractMqmRestClient(MqmConnectionConfig connectionConfig) {
		checkNotEmpty("Parameter 'location' must not be null or empty.", connectionConfig.getLocation());
		checkNotEmpty("Parameter 'sharedSpace' must not be null or empty.", connectionConfig.getSharedSpace());
		checkNotEmpty("Parameter 'clientType' must not be null or empty.", connectionConfig.getClientType());
		clientType = connectionConfig.getClientType();
		location = connectionConfig.getLocation();
		sharedSpace = connectionConfig.getSharedSpace();
		username = connectionConfig.getUsername();
		password = connectionConfig.getPassword();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(20);
		cm.setDefaultMaxPerRoute(20);
		cookieStore = new BasicCookieStore();

		if (connectionConfig.getProxyHost() != null && !connectionConfig.getProxyHost().isEmpty()) {
			HttpHost proxy = new HttpHost(connectionConfig.getProxyHost(), connectionConfig.getProxyPort());

			RequestConfig requestConfig = RequestConfig.custom()
					.setProxy(proxy)
					.setConnectTimeout(connectionConfig.getDefaultConnectionTimeout() != null ? connectionConfig.getDefaultConnectionTimeout() : DEFAULT_CONNECTION_TIMEOUT)
					.setSocketTimeout(connectionConfig.getDefaultSocketTimeout() != null ? connectionConfig.getDefaultSocketTimeout() : DEFAULT_SO_TIMEOUT).build();

			if (connectionConfig.getProxyCredentials() != null) {
				AuthScope proxyAuthScope = new AuthScope(connectionConfig.getProxyHost(), connectionConfig.getProxyPort());
				Credentials credentials = proxyCredentialsToCredentials(connectionConfig.getProxyCredentials());

				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(proxyAuthScope, credentials);

				httpClient = HttpClients.custom()
						.setConnectionManager(cm)
						.setDefaultCookieStore(cookieStore)
						.setDefaultCredentialsProvider(credsProvider)
						.setDefaultRequestConfig(requestConfig)
						.build();
			} else {
				httpClient = HttpClients.custom()
						.setConnectionManager(cm)
						.setDefaultCookieStore(cookieStore)
						.setDefaultRequestConfig(requestConfig)
						.build();
			}
		} else {
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(connectionConfig.getDefaultConnectionTimeout() != null ? connectionConfig.getDefaultConnectionTimeout() : DEFAULT_CONNECTION_TIMEOUT)
					.setSocketTimeout(connectionConfig.getDefaultSocketTimeout() != null ? connectionConfig.getDefaultSocketTimeout() : DEFAULT_SO_TIMEOUT)
					.build();
			httpClient = HttpClients.custom()
					.setConnectionManager(cm)
					.setDefaultCookieStore(cookieStore)
					.setDefaultRequestConfig(requestConfig)
					.build();

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
	}

	private void authenticate() {
		HttpPost post = new HttpPost(createBaseUri(URI_AUTHENTICATION));
		StringEntity loginApiJson = new StringEntity(
				"{\"user\":\"" + (username != null ? username : "") + "\"," +
						"\"password\":\"" + (password != null ? password : "") + "\"}"
				, ContentType.APPLICATION_JSON);
		post.setHeader(HEADER_CLIENT_TYPE, clientType);
		post.setEntity(loginApiJson);

		HttpResponse response = null;
		try {
			cookieStore.clear();
			response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				for (Cookie cookie : cookieStore.getCookies()) {
					if (cookie.getName().equals(LWSSO_COOKIE_NAME)) {
						LWSSO_TOKEN = cookie;
					}
					if (cookie.getName().equals(HPSSO_COOKIE_NAME)) {
						CSRF_TOKEN = cookie;
					}
				}
			} else {
				throw new AuthenticationException("Authentication failed: code=" + response.getStatusLine().getStatusCode() + "; reason=" + response.getStatusLine().getReasonPhrase());
			}
		} catch (IOException e) {
			throw new LoginErrorException("Error occurred during authentication", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	@Override
	public void tryToConnectSharedSpace() {
		login();
		checkSharedSpace();
	}

	private void checkSharedSpace() {
		HttpGet request = new HttpGet(createSharedSpaceApiUri("workspaces"));
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new SharedSpaceNotExistException("Cannot connect to given shared space.");
			}
		} catch (IOException e) {
			throw new RequestErrorException("Shared space check failed", e);
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
		String result = location + "/" + resolveTemplate(template, asMap(params));
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
	protected URI createProjectApiUri(String template, Object... params) {
		return createProjectUri(PROJECT_API_URI, template, asMap(params));
	}

	protected URI createSharedSpaceApiUri(String template, Object... params) {
		return createSharedSpaceApiUriMap(template, asMap(params));
	}

	protected URI createSharedSpaceApiUriMap(String template, Map<String, ?> params) {
		return URI.create(createBaseUri(SHARED_SPACE_API_URI, sharedSpace).toString() + "/" + resolveTemplate(template, params));
	}

	protected URI createSharedSpaceInternalApiUri(String template, Object... params) {
		return createSharedSpaceInternalApiUriMap(template, asMap(params));
	}

	protected URI createSharedSpaceInternalApiUriMap(String template, Map<String, ?> params) {
		return URI.create(createBaseUri(SHARED_SPACE_INTERNAL_API_URI, sharedSpace).toString() + "/" + resolveTemplate(template, params));
	}

	/**
	 * Creates absolute URI given by relative path from project URI leading by 'api'. It resolves all placeholders
	 * in template. All parameters are URI encoded before they are used for template resolving.
	 *
	 * @param template URI template of relative path (template must not starts with '/') from REST URI context. Special characters
	 *                 which need to be encoded must be already encoded in template.
	 * @param params   not encoded parameters of template. Objects are converted to string by its toString() method.
	 * @return absolute URI of endpoint with all parameters which are URI encoded
	 */
	protected URI createProjectApiUriMap(String template, Map<String, ?> params) {
		return createProjectUri(PROJECT_API_URI, template, params);
	}

	// don't remove (used in test-support)
	protected URI createWorkspaceInternalApiUriMap(String template, long workspaceId, Object... params) {
		return URI.create(createBaseUri(WORKSPACE_INTERNAL_API_URI, sharedSpace, workspaceId).toString() + "/" + resolveTemplate(template, asMap(params)));
	}

	protected URI createWorkspaceApiUri(String template, long workspaceId, Object... params) {
		return createWorkspaceApiUriMap(template, workspaceId, asMap(params));
	}

	protected URI createWorkspaceApiUriMap(String template, long workspaceId, Map<String, ?> params) {
		return URI.create(createBaseUri(WORKSPACE_API_URI, sharedSpace, workspaceId).toString() + "/" + resolveTemplate(template, params));
	}

	protected URI createProjectUri(String projectPartTemplate, String template, Map<String, ?> params) {
		return URI.create(createBaseUri(projectPartTemplate, sharedSpace).toString() + "/" + resolveTemplate(template, params));
	}

	/**
	 * Resolves all placeholders in template according to their order in params. All parameters are URI encoded before
	 * they are used for template resolving.
	 * This method works properly only if method {@link #encodeParam(String)} encodes parameters correctly (replace '{' and '}' by some other character(s)).
	 *
	 * @param template URI template
	 * @param params   URI parameters
	 * @return resolved URI template
	 */
	private String resolveTemplate(String template, Map<String, ?> params) {
		String result = template;
		for (String param : params.keySet()) {
			Object value = params.get(param);
			result = result.replaceAll(Pattern.quote("{" + param + "}"), encodeParam(value == null ? "" : value.toString()));
		}
		return result;
	}

	private Map<String, Object> asMap(Object... params) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < params.length; i++) {
			map.put(String.valueOf(i), params[i]);
		}
		return map;
	}

	private String encodeParam(String param) {
		try {
			return URLEncoder.encode(param, URI_PARAM_ENCODING).replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupported encoding used for URI parameter encoding.", e);
		}
	}

	/**
	 * Invokes {@link com.hp.mqm.org.apache.http.client.HttpClient#execute(com.hp.mqm.org.apache.http.client.methods.HttpUriRequest)}
	 * with given request and it does login if it is necessary.
	 * <p>
	 * Method does not support request with non-repeatable entity (see {@link HttpEntity#isRepeatable()}).
	 * </p>
	 *
	 * @param request which should be executed
	 * @return response for given request
	 * @throws IllegalArgumentException when request entity is not repeatable
	 */
	protected HttpResponse execute(HttpUriRequest request) throws IOException {
		HttpResponse response;

		if (LWSSO_TOKEN == null) {
			login();
		}
		HttpContext localContext = new BasicHttpContext();
		CookieStore localCookies = new BasicCookieStore();
		localCookies.addCookie(LWSSO_TOKEN);
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, localCookies);

		addRequestHeaders(request);
		response = httpClient.execute(request, localContext);
		if (response.getStatusLine().getStatusCode() == 401) {
			HttpClientUtils.closeQuietly(response);
			login();
			localCookies.clear();
			localCookies.addCookie(LWSSO_TOKEN);
			localContext.setAttribute(HttpClientContext.COOKIE_STORE, localCookies);
			addRequestHeaders(request);
			response = httpClient.execute(request, localContext);
		}
		return response;
	}

	protected <E> PagedList<E> getEntities(URI uri, int offset, EntityFactory<E> factory) {
		HttpGet request = new HttpGet(uri);
		HttpResponse response = null;
		try {
			response = execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw createRequestException("Entity retrieval failed", response);
			}
			String entitiesJson = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JSONObject entities = JSONObject.fromObject(entitiesJson);

			LinkedList<E> items = new LinkedList<E>();
			for (JSONObject entityObject : getJSONObjectCollection(entities, "data")) {
				items.add(factory.create(entityObject.toString()));
			}
			return new PagedList<E>(items, offset, entities.getInt("total_count"));
		} catch (IOException e) {
			throw new RequestErrorException("Cannot retrieve entities from MQM.", e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	protected URI getEntityURI(String collection, List<String> conditions, Long workspaceId, int offset, int limit, String orderBy) {
		Map<String, Object> params = pagingParams(offset, limit);
		StringBuilder template = new StringBuilder(collection + "?" + PAGING_FRAGMENT);

		if (!conditions.isEmpty()) {
			StringBuilder expr = new StringBuilder();
			for (String condition : conditions) {
				if (expr.length() > 0) {
					expr.append(";");
				}
				expr.append(condition);
			}
			params.put("query", "\"" + expr.toString() + "\"");
			template.append("&" + FILTERING_FRAGMENT);
		}

		if (!StringUtils.isEmpty(orderBy)) {
			params.put("order", orderBy);
			template.append("&" + ORDER_BY_FRAGMENT);
		}

		if (workspaceId != null) {
			return createWorkspaceApiUriMap(template.toString(), workspaceId, params);
		} else {
			return createSharedSpaceApiUriMap(template.toString(), params);
		}
	}

	protected RequestException createRequestException(String message, HttpResponse response) {
		String description = null;
		String stackTrace = null;
		String errorCode = null;
		try {
			String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JSONObject jsonObject = JSONObject.fromObject(json);
			if (jsonObject.has("error_code") && jsonObject.has("description")) {
				// exception response
				errorCode = jsonObject.getString("error_code");
				description = jsonObject.getString("description");
				// stack trace may not be present in production
				stackTrace = jsonObject.optString("stack_trace");
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to determine failure message: ", e);
		} catch (JSONException e) {
			logger.log(Level.SEVERE, "Unable to determine failure message: ", e);
		}

		ServerException cause = null;
		if (!StringUtils.isEmpty(stackTrace)) {
			try {
				Throwable parsedException = ExceptionStackTraceParser.parseException(stackTrace);
				cause = new ServerException("Exception thrown on server, see cause", parsedException);
			} catch (RuntimeException e) {
				// the parser is best-effort code, don't fail if anything goes wrong
				logger.log(Level.SEVERE, "Unable to parse server stacktrace: ", e);
			}
		}
		int statusCode = response.getStatusLine().getStatusCode();
		String reason = response.getStatusLine().getReasonPhrase();
		if (!StringUtils.isEmpty(errorCode)) {
			return new RequestException(message + "; error code: " + errorCode + "; description: " + description,
					description, errorCode, statusCode, reason, cause);
		} else {
			return new RequestException(message + "; status code " + statusCode + "; reason " + reason,
					description, errorCode, statusCode, reason, cause);
		}
	}

	protected String conditionRef(String name, long id) {
		return name + "={id=" + id + "}";
	}

	protected String conditionRef(String name, String refName, String value) {
		return name + "={" + condition(refName, value) + "}";
	}

	protected String condition(String name, String value) {
		return name + "='" + escapeQueryValue(value) + "'";
	}

	protected String condition(String name, int value) {
		return name + "=" + value;
	}

	private static String escapeQueryValue(String value) {
		return value.replaceAll("(\\\\)", "$1$1").replaceAll("([\"'])", "\\\\$1");
	}

	private Map<String, Object> pagingParams(int offset, int limit) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("limit", limit);
		return params;
	}

	private void addRequestHeaders(HttpUriRequest request) {
		addRequestHeaders(request, false);
	}

	private void addRequestHeaders(HttpUriRequest request, boolean isLogin) {
		request.setHeader(HEADER_CLIENT_TYPE, clientType);
		if (!isLogin) {
			request.setHeader(HPSSO_HEADER_NAME, CSRF_TOKEN.getValue());
		}
	}

	private void checkNotEmpty(String msg, String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}

	static Collection<JSONObject> getJSONObjectCollection(JSONObject object, String key) {
		JSONArray array = object.getJSONArray(key);
		return (Collection<JSONObject>) array.subList(0, array.size());
	}

	interface EntityFactory<E> {
		E create(String json);
	}
}
