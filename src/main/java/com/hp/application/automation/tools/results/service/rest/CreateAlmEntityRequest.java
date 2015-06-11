package com.hp.application.automation.tools.results.service.rest;

import java.util.List;

import com.hp.application.automation.tools.common.Pair;
import com.hp.application.automation.tools.results.service.almentities.AlmEntity;
import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.request.PostRequest;

public class CreateAlmEntityRequest extends PostRequest {
	
	List<Pair<String, String>> attrForCreation;
	AlmEntity almEntity;
	
	public CreateAlmEntityRequest( Client client, AlmEntity almEntity, List<Pair<String, String>> attrForCreation){
		super(client, "");
		this.attrForCreation = attrForCreation;
		this.almEntity = almEntity;
	}
	
	@Override
	protected String getSuffix() {
		// TODO Auto-generated method stub
		return almEntity.getRestPrefix();
	}

    protected List<Pair<String, String>> getDataFields() {
        
        return attrForCreation;
    }
}
