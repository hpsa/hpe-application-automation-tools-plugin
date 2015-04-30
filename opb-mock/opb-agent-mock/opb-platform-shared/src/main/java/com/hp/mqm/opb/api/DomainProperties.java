package com.hp.mqm.opb.api;

import java.io.Serializable;
import java.util.Properties;

/**
 * Hold domain information which is defined in domain.xml
 */
public interface DomainProperties extends Serializable {
    /**
     * Get domain name (aka domain type).    
     * @return domain name
     */
	String getName();
    
	/**
	 * Get domain version
	 * @return versoin
	 */
    String getVersion();
    
    /**
     * Get domain additional properties
     * @return domain properties
     */
    Properties getProperties();
}
