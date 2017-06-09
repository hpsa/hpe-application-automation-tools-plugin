package com.hpe.application.automation.tools.sse.autenvironment;

/**
 * Created by barush on 02/11/2014.
 */
public class AUTEnvironmnentParameter {
    
    public final static String ALM_PARAMETER_ID_FIELD = "id";
    public final static String ALM_PARAMETER_PARENT_ID_FIELD = "parent-id";
    public final static String ALM_PARAMETER_NAME_FIELD = "name";
    public final static String ALM_PARAMETER_VALUE_FIELD = "value";
    
    private String id;
    private String parentId;
    private String name;
    private String value;
    private String fullPath;
    
    public AUTEnvironmnentParameter(String id, String parentId, String name) {
        
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getFullPath() {
        return fullPath;
    }
    
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    
    public String getId() {
        return id;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public String getName() {
        return name;
    }
}
