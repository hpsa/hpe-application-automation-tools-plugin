package com.hpe.application.automation.tools.results.service.rest;

import java.util.List;
import java.util.Map;

import com.hpe.application.automation.tools.results.service.almentities.AlmEntity;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.GeneralPutBulkRequest;

public class UpdateAlmEntityRequest extends GeneralPutBulkRequest {
	
	List<Map<String, String>> attrForUpdate;
	AlmEntity almEntity;
	
	public UpdateAlmEntityRequest(Client client, AlmEntity almEntity, List<Map<String, String>> attrForUpdate){
		super(client);
		this.attrForUpdate = attrForUpdate;
		this.almEntity = almEntity;
	}
	
	@Override
	protected String getSuffix() {
		// TODO Auto-generated method stub
		return String.format("%s/%s", almEntity.getRestPrefix(), almEntity.getId());
	}

    protected List<Map<String, String>> getFields() {
        
        return attrForUpdate;
    }
}
