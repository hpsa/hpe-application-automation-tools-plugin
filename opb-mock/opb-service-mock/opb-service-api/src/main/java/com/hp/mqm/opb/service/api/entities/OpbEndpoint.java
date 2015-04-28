// (C) Copyright 2003-2013 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.opb.service.api.entities;

import java.util.Map;

/**
 * This interface represents end-point information in OPB
 */
public interface OpbEndpoint {

    public static final String DISABLE_STATUS = "disable";


    int getId();

    /**
     * Get end-point description.
     * 
     * @return description
     */
    String getDescription();

    /**
     * Set end-point description.
     * 
     * @param description
     */
    void setDescription(String description);

    /**
     * Get end-point type.
     * 
     * @return end-point type
     */
    String getType();

    /**
     * Set end-point type.
     * 
     * @param type
     */
    void setType(String type);

    /**
     * Get end-point name.
     * 
     * @return end-point name
     */
    String getName();

    /**
     * Set end-point name.
     * 
     * @param name
     *            end-point name
     */
    void setName(String name);

    /**
     * Get end-point status.
     * 
     * @return the status
     */
    String getStatus();

    /**
     * Set end-point status.
     * 
     * @param status
     *            end-point status
     */
    void setStatus(String status);

    /**
     * Get agent id.
     * 
     * @return the agent id
     */
    Integer getAgentId();

    /**
     * Set agent id.
     * 
     * @param agentId
     */
    void setAgentId(Integer agentId);

    /**
     * Get credentials id.
     * 
     * @return credentials id
     */
    String getCredentialsId();

    /**
     * Set credentials id.
     * 
     * @param credentialsId
     */
    void setCredentialsId(String credentialsId);

    /**
     * Get credentials description.
     * 
     * @return credentials description
     */
    String getCredentialsDescription();

    /**
     * Set credentials description
     * 
     * @param credDesc
     */
    void setCredentialsDescription(String credDesc);

    /**
     * Get boolean default value.
     * 
     * @return the boolean value
     */
    Boolean getIsDefault();

    /**
     * Set boolean default.
     * 
     * @param isDefault
     */
    void setIsDefault(Boolean isDefault);

    /**
     * Get uniqueness identity
     * 
     * @return uniqueness identity
     */
    String getUniquenessIdent();

    /**
     * Set uniqueness identity.
     * 
     * @param uniqIdent
     */
    void setUniquenessIdent(String uniqIdent);

    /**
     * Get additional parameters
     * 
     * @return parameters
     */
    Map<String, String> getParameters();

    /**
     * Set additional parameters.
     * 
     * @param params
     */
    void setParameters(Map<String, String> params);

    Integer getRevision();

    void setRevision(Integer revision);

}
