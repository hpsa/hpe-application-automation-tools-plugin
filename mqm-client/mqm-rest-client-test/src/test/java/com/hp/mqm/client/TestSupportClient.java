// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client;

import com.hp.mqm.client.model.Release;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestSupportClient extends AbstractMqmRestClient {

    private static final String URI_RELEASES = "releases-mqm";

    protected TestSupportClient(MqmConnectionConfig connectionConfig) {
        super(connectionConfig);
    }

    public Release createRelease(String name) throws IOException {
        JSONObject releaseObject = ResourceUtils.readJson("release.json");
        releaseObject.put("name", name);

        HttpPost request = new HttpPost(createProjectApiUri(URI_RELEASES));
        request.setEntity(new StringEntity(releaseObject.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                String payload = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                throw new IOException("Release posting failed with status code " + response.getStatusLine().getStatusCode() + ", reason " + response.getStatusLine().getReasonPhrase() + " and payload: " + payload);
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            response.getEntity().writeTo(result);
            JSONObject resultObject = JSONObject.fromObject(new String(result.toByteArray(), "UTF-8"));
            return new Release(resultObject.getInt("id"), resultObject.getString("name"));
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }
}