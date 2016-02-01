package com.hp.mqm.client;

import org.junit.Test;

public class MqmRestClientFactoryImplTest {

    @Test
    public void testCreate() {
        MqmRestClient client = new MqmRestClientImpl(MqmRestClientImplTest.connectionConfig);
        // test if client is initialized correctly
        client.tryToConnectSharedSpace();
    }
}
