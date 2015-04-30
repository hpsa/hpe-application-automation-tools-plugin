package com.hp.mqm.opb.api;

import java.io.Serializable;
import java.util.Properties;

/**
 * Represents On-Premise end-point in the system.
 * 
 * @author avrahame
 */
public interface Endpoint extends Serializable {
    /**
     * The param is used to get endpoint URL.
     */
    public static final String URL_PARAM = "URL";

    /**
     * Gets the end-point credentialsId.
     * 
     * @return The end-point credentialsId as string.
     */
    public String getCredentialsId();
    
    /**
     * Gets the end-point name.
     * 
     * @return The end-point name as string.
     */
    public String getName();
    
    /**
     * Gets additional endpoint properties.
     * 
     * @return Endpoint additional properties.
     */
    public Properties getProperties();

    /**
     * Gets the end-point id
     *
     * @return Endpoint id as int
     */
    public int getId();
}
