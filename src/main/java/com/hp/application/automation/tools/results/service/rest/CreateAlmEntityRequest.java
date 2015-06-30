package com.hp.application.automation.tools.results.service.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.application.automation.tools.common.Pair;
import com.hp.application.automation.tools.rest.HttpHeaders;
import com.hp.application.automation.tools.results.service.almentities.AlmEntity;
import com.hp.application.automation.tools.sse.common.RestXmlUtils;
import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.request.PostRequest;

public class CreateAlmEntityRequest extends PostRequest {
	
	List<Pair<String, String>> attrForCreation;
	AlmEntity almEntity;
	private static final String IGNORE_REQUIRED_FIELDS_VALIDATION = "X-QC-Ignore-Customizable-Required-Fields-Validation";
	public CreateAlmEntityRequest( Client client, AlmEntity almEntity, List<Pair<String, String>> attrForCreation){
		super(client, "");
		this.attrForCreation = attrForCreation;
		this.almEntity = almEntity;
	}
	
    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(HttpHeaders.CONTENT_TYPE, RestXmlUtils.APP_XML);
        ret.put(HttpHeaders.ACCEPT, RestXmlUtils.APP_XML);
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
