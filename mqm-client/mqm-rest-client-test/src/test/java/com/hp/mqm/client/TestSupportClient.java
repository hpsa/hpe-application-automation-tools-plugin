// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client;

import com.hp.mqm.client.model.Release;
import com.hp.mqm.client.model.Taxonomy;
import com.hp.mqm.client.model.TaxonomyType;
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
    private static final String URI_TAXONOMY_TYPES = "taxonomy-types";
    private static final String URI_TAXONOMIES = "taxonomies";

    protected TestSupportClient(MqmConnectionConfig connectionConfig) {
        super(connectionConfig);
    }

    public Release createRelease(String name) throws IOException {
        JSONObject releaseObject = ResourceUtils.readJson("release.json");
        releaseObject.put("name", name);

        JSONObject resultObject = postEntity(URI_RELEASES, releaseObject);
        return new Release(resultObject.getInt("id"), resultObject.getString("name"));
    }

    public TaxonomyType createTaxonomyType(String name) throws IOException {
        JSONObject taxonomyTypeObject = ResourceUtils.readJson("taxonomyType.json");
        taxonomyTypeObject.put("name", name);

        JSONObject resultObject = postEntity(URI_TAXONOMY_TYPES, taxonomyTypeObject);
        return new TaxonomyType(resultObject.getInt("id"), resultObject.getString("name"));
    }

    public Taxonomy createTaxonomy(int typeId, String name) throws IOException {
        JSONObject taxonomyObject = ResourceUtils.readJson("taxonomy.json");
        taxonomyObject.put("taxonomy-type-id", typeId);
        taxonomyObject.put("name", name);

        JSONObject resultObject = postEntity(URI_TAXONOMIES, taxonomyObject);
        return new Taxonomy(resultObject.getInt("id"), resultObject.getInt("taxonomy-type-id"), resultObject.getString("name"), null);
    }

    private JSONObject postEntity(String uri, JSONObject entityObject) throws IOException {
        HttpPost request = new HttpPost(createProjectApiUri(uri));
        request.setEntity(new StringEntity(entityObject.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = null;
        try {
            response = execute(request);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                String payload = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                throw new IOException("Posting failed with status code " + response.getStatusLine().getStatusCode() + ", reason " + response.getStatusLine().getReasonPhrase() + " and payload: " + payload);
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            response.getEntity().writeTo(result);
            return JSONObject.fromObject(new String(result.toByteArray(), "UTF-8"));
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }
}