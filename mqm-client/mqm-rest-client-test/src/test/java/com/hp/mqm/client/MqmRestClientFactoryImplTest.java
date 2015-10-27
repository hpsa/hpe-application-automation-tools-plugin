package com.hp.mqm.client;

import org.junit.Ignore;
import org.junit.Test;

public class MqmRestClientFactoryImplTest {

    @Test
    @Ignore // ignored until defect #2919 is fixed
    public void testCreate() {
        MqmRestClient client = MqmRestClientFactory.create(MqmRestClientImplTest.connectionConfig);
        // test if client is initialized correctly
        client.tryToConnectSharedSpace();
        client.release();
    }
}
