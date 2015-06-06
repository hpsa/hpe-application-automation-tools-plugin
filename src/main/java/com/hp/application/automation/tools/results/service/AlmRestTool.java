package com.hp.application.automation.tools.results.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.application.automation.tools.common.Pair;
import com.hp.application.automation.tools.rest.RestClient;
import com.hp.application.automation.tools.results.service.almentities.AlmEntity;
import com.hp.application.automation.tools.results.service.rest.CreateAlmEntityRequest;
import com.hp.application.automation.tools.results.service.rest.GetAlmEntityRequest;
import com.hp.application.automation.tools.results.service.rest.UpdateAlmEntityRequest;
import com.hp.application.automation.tools.sse.common.XPathUtils;
import com.hp.application.automation.tools.sse.sdk.Logger;
import com.hp.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.hp.application.automation.tools.sse.sdk.Response;
import com.hp.application.automation.tools.sse.sdk.RestAuthenticator;

public class AlmRestTool {
	
	private Logger _logger ;
	
	private RestClient restClient;
	private AlmRestInfo almLoginInfo;
	
	
	public AlmRestTool (AlmRestInfo almLoginInfo, Logger logger) {
		this.restClient = new RestClient(
        							almLoginInfo.getServerUrl(),
        							almLoginInfo.getDomain(),
        							almLoginInfo.getProject(),
        							almLoginInfo.getUserName());;
		this.almLoginInfo = almLoginInfo;
		this._logger = logger;
	}
	
	public RestClient getRestClient() {
		return this.restClient;
	}
	
	
    private void appendQCSessionCookies(RestClient client) {
        
        // issue a post request so that cookies relevant to the QC Session will be added to the RestClient
        Response response =
                client.httpPost(
                        client.build("rest/site-session"),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);
        if (!response.isOk()) {
        	_logger.log("Failed to add QC Session Cookies.");
        }
    }
    
    
	
	public boolean login() {

		boolean ret = true;
        try {
            ret =
                    new RestAuthenticator().login(
                    		restClient,
                    		almLoginInfo.getUserName(),
                    		almLoginInfo.getPassword(),
                    		_logger);
            appendQCSessionCookies(restClient);
        } catch (Throwable cause) {
            ret = false;
            _logger.log(String.format(
                    "Failed login to ALM Server URL: %s. Exception: %s",
                    almLoginInfo.getServerUrl(),
                    cause.getMessage()));
        }        
        return ret;
	}
	
	public List<Pair<String, String>> getPairListForAlmEntityFields(AlmEntity almEntity, List<String> fieldNames){
		List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
		for(String fieldName : fieldNames) {
			pairs.add(new Pair<String, String>(fieldName, String.valueOf(almEntity.getFieldValue(fieldName))));	
		}
		return pairs;
	}
	
	public List<Pair<String, String>> getPairListForAlmEntityFields(AlmEntity almEntity, String[] fieldNames){
		List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
		for(String fieldName : fieldNames) {
			pairs.add(new Pair<String, String>(fieldName, String.valueOf(almEntity.getFieldValue(fieldName))));	
		}
		return pairs;
	}	
	
	public List<Map<String, String>> getMapListForAlmEntityFields(AlmEntity almEntity, String[] fieldNames){
		List<Map<String, String>> fieldsMapList = new ArrayList<Map<String, String>>();
		
		Map<String, String> fieldsMap = new HashMap<String, String> ();
		
		for(String fieldName : fieldNames) {
			fieldsMap.put(fieldName, String.valueOf(almEntity.getFieldValue(fieldName)));	
		}
		fieldsMapList.add(fieldsMap);
		return fieldsMapList;
	}
	
	public void populateAlmEntityFieldValue(Map<String, String> mapFieldValue, AlmEntity almEntity) {
		for(Map.Entry<String, String> entry : mapFieldValue.entrySet()){
			almEntity.setFieldValue(entry.getKey(), entry.getValue());
		}
	}
	
	public <E extends AlmEntity>  List<E> getAlmEntityList(List<Map<String, String>> entities, Class<E> c) {
		
		List<E> entityList = new ArrayList<E> ();
		for(Map<String, String> fieldValueMap:  entities) {
			try {
				E entity = c.newInstance();
				populateAlmEntityFieldValue(fieldValueMap, entity);
				entityList.add(entity);
			} catch (Exception e) {
				
			}
		}
		
		return entityList;
		
	}
	
	public static String getEncodedString(String s) {
		String quotedStr = "\"" + s +"\"";
		try {
			quotedStr = URLEncoder.encode(quotedStr, "UTF-8");
		} catch (Exception e) {
			
		}
		return quotedStr;
	}
	
	public <E extends AlmEntity > E getEntityUnderParentFolder( Class<E> entityClass, int parentId, String entityName ){

		String getEntityUnderParentFolderQuery = String.format("fields=id,name&query={parent-id[%s];name[%s]}", String.valueOf(parentId), getEncodedString(entityName));
		try {
			AlmEntity entity = entityClass.newInstance();
			GetAlmEntityRequest getRequest = new GetAlmEntityRequest(entity, getRestClient(), getEntityUnderParentFolderQuery);
			Response response = getRequest.perform();
			if(response.isOk() ) {
				List<Map<String, String>> entities2 = XPathUtils.toEntities(response.toString());
				List<E> entities = getAlmEntityList(entities2, entityClass);
	
				if(entities.size()>0){
					return entities.get(0);
				} else {
					//_logger.log("No Entity Found:" +getEntityUnderParentFolderQuery );
					return null;
				}
			} else {
				_logger.log("Failed to get Entity:" +getEntityUnderParentFolderQuery );
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}	
	
	public <E extends AlmEntity > List<E> getAlmEntity( E entity, String queryString){

		List<E> ret = new ArrayList<E>();
		
		try {
			GetAlmEntityRequest getRequest = new GetAlmEntityRequest(entity, getRestClient(), queryString);
			Response response = getRequest.perform();
			if(response.isOk() && !response.toString().equals("")) {
				List<Map<String, String>> entities2 = XPathUtils.toEntities(response.toString());
				List entities = getAlmEntityList(entities2, entity.getClass());
				return entities;
			} else {
				return ret;
			}

		} catch (Exception e) {
			e.printStackTrace();
			_logger.log("Failed to get Entity:" + entity.toString() +" with query string:" +queryString);
			return ret;
		}

	}	
	
	public <E extends AlmEntity> E createAlmEntity (E entity, String[] fieldsForCreation) {
		
		CreateAlmEntityRequest createRequest = new CreateAlmEntityRequest(getRestClient(), entity, getPairListForAlmEntityFields(entity, fieldsForCreation) );
		Response response = createRequest.perform();	
		if(response.isOk() && !response.toString().equals("")){
			List<Map<String, String>> entities2 = XPathUtils.toEntities(response.toString());
			List entities = getAlmEntityList(entities2, entity.getClass());
	
			if(entities.size()>0){
				
				return (E)entities.get(0);
			} else {
				_logger.log("Failed to create Entity:" + entity.toString());
				return null;
			}
		} else {
			_logger.log("Failed to create Entity:" + entity.toString());
			return null;
		}
		
	}
	
	public <E extends AlmEntity> void updateAlmEntity (E entity, String[] fieldsForUpdate) {
		
		UpdateAlmEntityRequest updateRequest = new UpdateAlmEntityRequest(getRestClient(), entity, getMapListForAlmEntityFields(entity, fieldsForUpdate)) ;
		Response response = updateRequest.execute();
		
		if(!response.isOk()) {
			_logger.log("Failed to update entity:" + entity.toString());
		}

	}	
	
}
