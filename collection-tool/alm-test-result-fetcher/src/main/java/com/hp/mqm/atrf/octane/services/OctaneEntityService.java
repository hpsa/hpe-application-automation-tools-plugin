package com.hp.mqm.atrf.octane.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.mqm.atrf.core.rest.HTTPUtils;
import com.hp.mqm.atrf.core.rest.Response;
import com.hp.mqm.atrf.core.rest.RestConnector;
import com.hp.mqm.atrf.octane.core.OctaneEntity;
import com.hp.mqm.atrf.octane.core.OctaneEntityCollection;
import com.hp.mqm.atrf.octane.core.OctaneEntityDescriptor;
import com.hp.mqm.atrf.octane.core.OctaneTestResultOutput;
import com.hp.mqm.atrf.octane.entities.*;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by berkovir on 21/11/2016.
 */
public class OctaneEntityService {

    private RestConnector restConnector;
    private long sharedSpaceId;
    private long workspaceId;

    Map<String, OctaneEntityDescriptor> typesMap = new HashMap<>();

    public OctaneEntityService(RestConnector restConnector) {
        this.restConnector = restConnector;


        registerTypes();
    }

    private void registerTypes() {
        typesMap.put(Test.TYPE, new TestDescriptor());
        typesMap.put(ListNode.TYPE, new ListNodeDescriptor());
        typesMap.put(WorkspaceUser.TYPE, new WorkspaceUserDescriptor());
        typesMap.put(TestVersion.TYPE, new TestVersionDescriptor());
        typesMap.put(Phase.TYPE, new PhaseDescriptor());
        typesMap.put(Release.TYPE, new ReleaseDescriptor());
        typesMap.put(Sprint.TYPE, new SprintDescriptor());
    }


    public boolean login(String user, String password) {
        boolean ret = false;

        restConnector.clearAll();

        OctaneAuthenticationPojo authData = new OctaneAuthenticationPojo();
        authData.setUser(user);
        authData.setPassword(password);


        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(authData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fail in generating json for login data : " + e.getMessage());
        }

        //Get LWSSO COOKIE
        Map<String, String> headers = new HashMap<>();
        headers.put(HTTPUtils.HEADER_CONTENT_TYPE, HTTPUtils.HEADER_APPLICATION_JSON);
        Response authResponse = restConnector.httpPost(OctaneRestConstants.AUTHENTICATION_URL, jsonString, headers);
        if (authResponse.getStatusCode() == HttpStatus.SC_OK) {
            ret = true;
        }

        return ret;
    }


    public void setWorkspaceId(long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public long getSharedSpaceId() {
        return sharedSpaceId;
    }

    public void setSharedSpaceId(long sharedSpaceId) {
        this.sharedSpaceId = sharedSpaceId;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public OctaneEntityCollection getEntities(String collectionName, OctaneQueryBuilder queryBuilder) {
        String entityCollectionUrl = String.format(OctaneRestConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, getSharedSpaceId(), getWorkspaceId(), collectionName);
        String queryString = queryBuilder.build();

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTPUtils.HEADER_ACCEPT, HTTPUtils.HEADER_APPLICATION_JSON);
        headers.put(OctaneRestConstants.CLIENTTYPE_HEADER, OctaneRestConstants.CLIENTTYPE_INTERNAL);

        String entitiesCollectionStr = restConnector.httpGet(entityCollectionUrl,  Arrays.asList(queryString), headers).getResponseData();
        JSONObject jsonObj = new JSONObject(entitiesCollectionStr);
        OctaneEntityCollection col = parseCollection(jsonObj);
        return col;
    }

    public OctaneTestResultOutput postTestResults(String data) {
        String entityCollectionUrl = String.format(OctaneRestConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, getSharedSpaceId(), getWorkspaceId(), "test-results");

        Map<String, String> headers = new HashMap<>();
        headers.put(HTTPUtils.HEADER_ACCEPT, HTTPUtils.HEADER_APPLICATION_JSON);
        headers.put(HTTPUtils.HEADER_CONTENT_TYPE, HTTPUtils.HEADER_APPLICATION_XML);

        String responseStr = restConnector.httpPost(entityCollectionUrl, data, headers).getResponseData();
        OctaneTestResultOutput result = parseTestResultOutput(responseStr);
        return result;
    }

    public OctaneTestResultOutput getTestResultStatus(OctaneTestResultOutput output) {
        String entityCollectionUrl = String.format(OctaneRestConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, getSharedSpaceId(), getWorkspaceId(), "test-results") + "/" + output.getId();
        Map<String, String> headers = new HashMap<>();
        headers.put(HTTPUtils.HEADER_ACCEPT, HTTPUtils.HEADER_APPLICATION_JSON);


        String responseStr = restConnector.httpGet(entityCollectionUrl, null, headers).getResponseData();
        OctaneTestResultOutput result = parseTestResultOutput(responseStr);
        return result;
    }

    private OctaneTestResultOutput parseTestResultOutput(String responseStr) {
        JSONObject jsonObj = new JSONObject(responseStr);
        OctaneTestResultOutput result = new OctaneTestResultOutput();
        result.put(OctaneTestResultOutput.FIELD_ID, jsonObj.get(OctaneTestResultOutput.FIELD_ID));
        result.put(OctaneTestResultOutput.FIELD_STATUS, jsonObj.get(OctaneTestResultOutput.FIELD_STATUS));

        return result;
    }

    private OctaneEntityCollection parseCollection(JSONObject jsonObj) {
        OctaneEntityCollection coll = new OctaneEntityCollection();

        int total = jsonObj.getInt("total_count");
        coll.setTotalCount(total);

        if (jsonObj.has("exceeds_total_count")) {
            boolean exceedsTotalCount = jsonObj.getBoolean("exceeds_total_count");
            coll.setExceedsTotalCount(exceedsTotalCount);
        }

        JSONArray entitiesJArr = jsonObj.getJSONArray("data");
        for (int i = 0; i < entitiesJArr.length(); i++) {

            JSONObject entObj = entitiesJArr.getJSONObject(i);
            OctaneEntity entity = parseEntity(entObj);

            coll.getData().add(entity);
        }

        return coll;
    }

    private OctaneEntity parseEntity(JSONObject entObj) {

        String type = entObj.getString("type");

        OctaneEntity entity = createEntity(type);
        for (String key : entObj.keySet()) {
            Object value = entObj.get(key);
            if (value instanceof JSONObject) {
                JSONObject jObj = (JSONObject) value;
                if (jObj.has("type")) {
                    OctaneEntity valueEntity = parseEntity(jObj);
                    value = valueEntity;
                } else if (jObj.has("total_count")) {
                    OctaneEntityCollection coll = parseCollection(jObj);
                    value = coll;
                } else {
                    value = jObj.toString();
                }
            } else if (JSONObject.NULL.equals(value)) {
                value = null;
            }
            entity.put(key, value);
        }
        return entity;
    }

    private OctaneEntity createEntity(String type) {
        OctaneEntityDescriptor descriptor = typesMap.get(type);
        if (descriptor == null) {
            //return new OctaneEntity(type);
            throw new RuntimeException("Unregistered type " + type);
        }
        OctaneEntity entity = null;
        try {
            entity = descriptor.getEntityClass().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity of type " + type, e);
        }

        return entity;
    }

}
