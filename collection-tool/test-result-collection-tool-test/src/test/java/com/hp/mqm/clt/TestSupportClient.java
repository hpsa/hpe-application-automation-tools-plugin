// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt;

import com.hp.mqm.clt.model.PagedList;
import com.hp.mqm.clt.model.Release;
import com.hp.mqm.clt.model.TestResultStatus;
import com.hp.mqm.clt.model.TestRun;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestSupportClient extends RestClient {

    private static final String URI_RELEASES = "releases";
    private static final String URI_TEST_RUN = "runs";
    private static final String URI_TEST_RESULT_STATUS = "test-results/{0}";

    private static final String FILTERING_FRAGMENT = "query={query}";
    private static final String PAGING_FRAGMENT = "offset={offset}&limit={limit}";
    private static final String ORDER_BY_FRAGMENT = "order_by={order}";

    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    protected TestSupportClient(Settings settings) {
        super(settings);
    }

    public Release createRelease(String name) throws IOException {
        JSONObject releaseObject = ResourceUtils.readJson("release.json");
        releaseObject.put("name", name);

        JSONObject resultObject = postEntity(URI_RELEASES, releaseObject);
        return new Release(resultObject.getLong("id"), resultObject.getString("name"));
    }

    public PagedList<TestRun> queryTestRuns(String name, int offset, int limit) {
        List<String> conditions = new LinkedList<String>();
        if (!StringUtils.isEmpty(name)) {
            conditions.add(condition("name", "*" + name + "*"));
        }
        return getEntities(getEntityURI(URI_TEST_RUN, conditions, offset, limit, null), offset, new TestRunEntityFactory());
    }

    private JSONObject postEntity(String uri, JSONObject entityObject) throws IOException {
        URI requestURI = createWorkspaceApiUri(uri);
        HttpPost request = new HttpPost(requestURI);
        JSONArray data = new JSONArray();
        data.add(entityObject);
        JSONObject body = new JSONObject();
        body.put("data", data);
        request.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));
        CloseableHttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                String payload = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                throw new IOException("Posting failed with status code " + response.getStatusLine().getStatusCode() + ", reason " + response.getStatusLine().getReasonPhrase() + " and payload: " + payload);
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            response.getEntity().writeTo(result);
            JSONObject jsonObject = JSONObject.fromObject(new String(result.toByteArray(), "UTF-8"));
            return jsonObject.getJSONArray("data").getJSONObject(0);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    protected <E> PagedList<E> getEntities(URI uri, int offset, EntityFactory<E> factory) {
        HttpGet request = new HttpGet(uri);
        CloseableHttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Entity retrieval failed");
            }
            String entitiesJson = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject entities =  JSONObject.fromObject(entitiesJson);

            LinkedList<E> items = new LinkedList<E>();
            for (JSONObject entityObject : getJSONObjectCollection(entities, "data")) {
                items.add(factory.create(entityObject.toString()));
            }
            return new PagedList<E>(items, offset, entities.getInt("total_count"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot retrieve entities from MQM.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    public TestResultStatus getTestResultStatus(long id) {
        HttpGet request = new HttpGet(createWorkspaceApiUri(URI_TEST_RESULT_STATUS, id));
        CloseableHttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Result status retrieval failed");
            }
            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = JSONObject.fromObject(json);
            Date until = null;
            if (jsonObject.has("until")) {
                try {
                    until = parseDatetime(jsonObject.getString("until"));
                } catch (ParseException e) {
                    throw new RuntimeException("Cannot obtain status", e);
                }
            }
            return new TestResultStatus(jsonObject.getString("status"), until);
        } catch (IOException e) {
            throw new RuntimeException("Cannot obtain status.", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    protected URI getEntityURI(String collection, List<String> conditions, int offset, int limit, String orderBy) {
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

        return createWorkspaceApiUriMap(template.toString(), params);

    }

    private static class TestRunEntityFactory implements EntityFactory<TestRun> {

        @Override
        public TestRun create(String json) {
            JSONObject entityObject = JSONObject.fromObject(json);
            return new TestRun(
                    entityObject.getInt("id"),
                    entityObject.getString("name"));
        }
    }

    private String condition(String name, String value) {
        return name + "='" + escapeQueryValue(value) + "'";
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

    static Collection<JSONObject> getJSONObjectCollection(JSONObject object, String key) {
        JSONArray array = object.getJSONArray(key);
        return (Collection<JSONObject>) array.subList(0, array.size());
    }

    private Date parseDatetime(String datetime) throws ParseException {
        return new SimpleDateFormat(DATETIME_FORMAT).parse(datetime);
    }

    interface EntityFactory<E> {

        E create(String json);

    }
}