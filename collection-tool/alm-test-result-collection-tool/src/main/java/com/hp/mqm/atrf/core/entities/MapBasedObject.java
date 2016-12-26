package com.hp.mqm.atrf.core.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by berkovir on 28/05/2015.
 */
public class MapBasedObject {
    private Map<String, Object> fields = new HashMap<String, Object>();

    public void put(String fieldName, Object fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public Object get(String fieldName) {
        return fields.get(fieldName);
    }

    public String getString(String fieldName) {
        return (String)fields.get(fieldName);
    }


    public void remove(String fieldName){
        fields.remove(fieldName);
    }

    public boolean isFieldSet(String fieldName) {
        return fields.containsKey(fieldName);
    }

    public boolean isFieldSetAndNotEmpty(String fieldName) {
        Object value = fields.get(fieldName);
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            return !((String) value).isEmpty();
        }

        if (value instanceof Collection) {
            return !((Collection) value).isEmpty();
        }

        return true;
    }

}

