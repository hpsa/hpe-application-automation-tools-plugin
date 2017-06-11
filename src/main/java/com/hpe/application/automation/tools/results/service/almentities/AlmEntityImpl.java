package com.hpe.application.automation.tools.results.service.almentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class  AlmEntityImpl implements AlmEntity {

	private final Map<String, String> fields = new HashMap<String, String>();
    private final Map<String, List<AlmEntity>> relatedEntities =
            new HashMap<String, List<AlmEntity>>();
	
            
    public String getName() {
    	return (String) getFieldValue(AlmCommonProperties.NAME);
    }
    
    public String getId() {
    	return  getFieldValue(AlmCommonProperties.ID);
    	
    }
    
    public void setId(String id) {
    	setFieldValue(AlmCommonProperties.ID, id);
    }
    
	@Override
	public void setFieldValue(String fieldName, String fieldValue) {
		fields.put(fieldName, fieldValue);

	}
	
	@Override
	public String getFieldValue(String fieldName) {
		return fields.get(fieldName);
	}

	@Override
	public void addRelatedEntity(String relationName, AlmEntity entity) {
        List<AlmEntity> entities = relatedEntities.get(relationName);
        if (entities == null){
            relatedEntities.put(relationName, new ArrayList<AlmEntity>());
            entities = relatedEntities.get(relationName);
        }

        entities.add(entity);
	}

	@Override
    public Map<String, List<AlmEntity>> getRelatedEntities() {
        return relatedEntities;
    }	
	
	public String toString(){
		String s = this.getRestPrefix() +":";
		
		for(Map.Entry<String, String> field : fields.entrySet()) {
			s += field.getKey();
			s += "=";
			s += field.getValue();
		}
		return s;
	}
}
