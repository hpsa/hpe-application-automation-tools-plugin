package com.hpe.application.automation.tools.results.service.rest;

import com.hpe.application.automation.tools.common.Pair;
import com.hpe.application.automation.tools.rest.RESTConstants;
import com.hpe.application.automation.tools.results.service.almentities.AlmEntity;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.request.PostRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAlmEntityRequest extends PostRequest {
	
	List<Pair<String, String>> attrForCreation;
	AlmEntity almEntity;
	private static final String IGNORE_REQUIRED_FIELDS_VALIDATION = "X-QC-Ignore-Customizable-Required-Fields-Validation";
	public CreateAlmEntityRequest(Client client, AlmEntity almEntity, List<Pair<String, String>> attrForCreation){
		super(client, "");
		this.attrForCreation = attrForCreation;
		this.almEntity = almEntity;
	}
	
    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);
        ret.put(IGNORE_REQUIRED_FIELDS_VALIDATION, "Y");
        return ret;
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
