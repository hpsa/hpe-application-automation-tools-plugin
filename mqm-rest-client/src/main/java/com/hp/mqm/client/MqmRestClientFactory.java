package com.hp.mqm.client;

/**
 * Factory for MqmRestClient.
 */
public class MqmRestClientFactory {

    /**
     * Creates MqmRestClient.
     * @param connectionConfig MQM connection configuration
     * @return Initialized MqmRestClient
     */
    public static MqmRestClient create(MqmConnectionConfig connectionConfig) {
        return new MqmRestClientImpl(connectionConfig);
    }
}
