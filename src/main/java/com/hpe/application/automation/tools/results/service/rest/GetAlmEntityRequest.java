package com.hpe.application.automation.tools.results.service.rest;

import com.hpe.application.automation.tools.results.service.almentities.AlmEntity;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.GetRequest;

public class GetAlmEntityRequest extends GetRequest {
	
	private AlmEntity almEntity;
	private String queryString ;
	
	public GetAlmEntityRequest(AlmEntity almEntity, Client client) {
		super(client, "");
		this.almEntity = almEntity;
		
	}

	public GetAlmEntityRequest(AlmEntity almEntity, Client client, String queryString) {
		super(client, "");
		this.almEntity = almEntity;
		this.queryString = queryString;
		
	}
	
    protected String getQueryString() {
        
        return queryString;
    }
	
	protected String getSuffix() {
		if(queryString != null && queryString.length() >0 ) {
			return almEntity.getRestPrefix();
			
		} else {
			return String.format("%s/%s", almEntity.getRestPrefix(), almEntity.getId());
		}
	}
}
