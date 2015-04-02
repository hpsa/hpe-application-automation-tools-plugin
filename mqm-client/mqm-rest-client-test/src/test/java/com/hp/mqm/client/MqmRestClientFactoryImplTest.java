package com.hp.mqm.client;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MqmRestClientFactoryImplTest {

    @Test
    public void testCreate() {
        MqmRestClient client = MqmRestClientFactory.create(MqmRestClientImplTest.connectionConfig);
        // test if client is initialized correctly
        client.tryToConnectProject();
        client.release();
    }
}
