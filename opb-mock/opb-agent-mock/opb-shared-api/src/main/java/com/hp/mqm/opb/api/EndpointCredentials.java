package com.hp.mqm.opb.api;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents an endpoint credentials.
 *
 * @author avrahame
 */
public interface EndpointCredentials extends Serializable {

    /**
     * The endpoint credentials id.
     *
     * @return The id as string.
     */
    public String getId();

    /**
     * The endpoint credentials name.
     *
     * @return The name as string.
     */
    public String getName();

        /**
         * The endpoint credentials user name.
         *
         * @return The user as string.
         */
    public String getUser();

    /**
     * Get the endpoint credentials password.
     *
     * @return The password as byte array.
     */
    public byte[] getPassword();

    /**
     * Returns additional endpoint credentials parameters.
     *
     * @return Endpoint credentials additional parameters.
     */
    public Map<String, Object> getParameters();
}
