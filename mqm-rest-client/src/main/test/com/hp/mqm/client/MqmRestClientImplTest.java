package com.hp.mqm.client;

import org.junit.Assert;
import org.junit.Test;

public class MqmRestClientImplTest {

    @Test
    public void testCreateBaseUri() {
        MqmRestClientImpl client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "ddd", "ppp", "albert", "abcde", "proxyHost", 12345));
        Assert.assertEquals("http://abc.com:123/qcbin/some/base/endpoint", client.createBaseUri("some/base/endpoint").toString());
        // test query parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/some?hola=sp&hello=en", client.createBaseUri("some?hola={0}&hello={1}", "sp", "en").toString());
        // test query and path parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/some/endpoint?hola=sp&hello=en", client.createBaseUri("{0}/{1}?hola={2}&hello={3}", "some", "endpoint", "sp", "en").toString());
        // parameter encoding
        Assert.assertEquals("http://abc.com:123/qcbin/so%2Fme/end%40%20point%25?hola=s%20p&hello=e%26n%3F%23%25", client.createBaseUri("{0}/{1}?hola={2}&hello={3}", "so/me", "end@ point%", "s p", "e&n?#%").toString());
    }

    @Test
    public void testCreateRestUri() {
        MqmRestClientImpl client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "ddd", "ppp", "albert", "abcde", "proxyHost", 12345));
        Assert.assertEquals("http://abc.com:123/qcbin/rest/domains/ddd/projects/ppp/some/base/endpoint", client.createRestUri("some/base/endpoint").toString());
        // test query parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/rest/domains/ddd/projects/ppp/some?hola=sp&hello=en", client.createRestUri("some?hola={0}&hello={1}", "sp", "en").toString());
        // test query and path parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/rest/domains/ddd/projects/ppp/some/endpoint?hola=sp&hello=en", client.createRestUri("{0}/{1}?hola={2}&hello={3}", "some", "endpoint", "sp", "en").toString());
        // parameter encoding
        client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "d d/d", "p%pp", "albert", "abcde", "proxyHost", 12345));
        Assert.assertEquals("http://abc.com:123/qcbin/rest/domains/d%20d%2Fd/projects/p%25pp/so%2Fme/end%40%20point%25?hola=s%20p&hello=e%26n%3F%23%25", client.createRestUri("{0}/{1}?hola={2}&hello={3}", "so/me", "end@ point%", "s p", "e&n?#%").toString());
    }
}
