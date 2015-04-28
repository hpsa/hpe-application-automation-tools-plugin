package com.hp.mqm.opb.service.api.entities;



/**
 * This interface represents agent in Opb.
 */
public interface OpbAgent  {

    Integer getId() ;
    /**
     * Get agent GUID.
     * 
     * @return agent GUID
     */
    String getGuid();

    /**
     * Set agent GUID.
     * 
     * @param guid
     */
    void setGuid(String guid);

    /**
     * Get agent name.
     * 
     * @return agent name
     */
    String getName();

    /**
     * Set agent name.
     * 
     * @param agentName
     */
    void setName(String agentName);

    /**
     *
     * @return agent version
     */
    String getVersion();

    /**
     * set agent version
     * @param version agent version
     */
    void setVersion(String version);

    /**
     *
     * @return 'Y' if agent version is compatible with AgM version
     */
    String getIsVersionSupported();

    /**
     * set agent version is compatible with AgM version
     * @param isVersionSupported 'Y' or 'N'
     */
    void setIsVersionSupported(String isVersionSupported);

    /**
     * Get agent description.
     * 
     * @return description
     */
    String getDescription();

    /**
     * Set agent description.
     * 
     * @param description
     */
    void setDescription(String description);

    /**
     * Get last seen time in milliseconds
     * 
     * @return
     */
    Double getLastSeen();

    /**
     * Set last seen time
     * 
     * @param lastSeen
     */
    void setLastSeen(Double lastSeen);

    /**
     * Get agent status.
     * 
     * @return status
     */
    String getStatus();

    /**
     * set agent status.
     * 
     * @param status
     */
    void setStatus(String status);
}
