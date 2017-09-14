package com.hpe.application.automation.tools.results.service.almentities;

import java.util.List;
import java.util.Map;

public interface AlmEntity {
	
	public void setFieldValue(String fieldName, String fieldValue);
	public Object getFieldValue(String fieldName);
	public void addRelatedEntity(String relationName, AlmEntity entity);
	public Map<String, List<AlmEntity>> getRelatedEntities() ;

	public String getName();
	
	public String getId();
	
	public void setId(String id);
	
	public String getRestPrefix();
	
}
