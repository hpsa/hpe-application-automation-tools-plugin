// (C) Copyright 2003-2013 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.opb.service.api.entities;

/**
 * This class represents Opb end-point type.
 * 
 */
public class OpbEndpointType {

    private String name;
    private String displayName;
    private String version;

    public OpbEndpointType() {
    }

    /**
     * Constructor
     * 
     * @param name
     *            type name
     * @param displayName
     *            display name
     * @param version
     */
    public OpbEndpointType(String name, String displayName, String version) {
        this.name = name;
        this.displayName = displayName;
        this.version = version;
    }

    /**
     * Get type name.
     * 
     * @return type name
     */
    public String getName() {
        return name;
    }

    /**
     * Set type name.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get display name.
     * 
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set display name.
     * 
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get version
     * 
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set version
     * 
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
