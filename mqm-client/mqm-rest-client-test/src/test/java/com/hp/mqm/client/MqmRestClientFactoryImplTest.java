package com.hp.mqm.client;

import org.junit.Test;

public class MqmRestClientFactoryImplTest {

    @Test
    public void testCreate() {
        MqmRestClient client = MqmRestClientFactory.create(MqmRestClientImplTest.connectionConfig);
        // test if client is initialized correctly
        client.checkDomainAndProject();
        client.release();
    }
}
