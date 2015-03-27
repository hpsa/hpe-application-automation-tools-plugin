package com.hp.mqm.client;

import org.junit.Assert;
import org.junit.Test;

public class MqmRestClientImplTest {

    @Test
    public void testCreateBaseUri() {
        MqmRestClientImpl client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "ddd", "ppp", "albert", "abcde", "test", "proxyHost", 12345));
        Assert.assertEquals("http://abc.com:123/qcbin/some/base/endpoint", client.createBaseUri("some/base/endpoint").toString());
        // test query parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/some?hola=sp&hello=en", client.createBaseUri("some?hola={0}&hello={1}", "sp", "en").toString());
        // test query and path parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/some/endpoint?hola=sp&hello=en", client.createBaseUri("{0}/{1}?hola={2}&hello={3}", "some", "endpoint", "sp", "en").toString());
        // null and integer used as parameters
        Assert.assertEquals("http://abc.com:123/qcbin/123/abc?hola=&hello=e%26n%3F%23%25", client.createBaseUri("{0}/{1}?hola={2}&hello={3}", 123, "abc", null, "e&n?#%").toString());
        // parameter encoding
        Assert.assertEquals("http://abc.com:123/qcbin/so%2Fme/end%40%20point%25?hola=s%20p&hello=e%26n%3F%23%25", client.createBaseUri("{0}/{1}?hola={2}&hello={3}", "so/me", "end@ point%", "s p", "e&n?#%").toString());
    }

    @Test
    public void testCreateRestUri() {
        MqmRestClientImpl client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "ddd", "ppp", "albert", "abcde", "test", "proxyHost", 12345));
        Assert.assertEquals("http://abc.com:123/qcbin/api/domains/ddd/projects/ppp/some/base/endpoint", client.createProjectRestUri("some/base/endpoint").toString());
        // test query parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/api/domains/ddd/projects/ppp/some?hola=sp&hello=en", client.createProjectRestUri("some?hola={0}&hello={1}", "sp", "en").toString());
        // test query and path parameters resolving
        Assert.assertEquals("http://abc.com:123/qcbin/api/domains/ddd/projects/ppp/some/endpoint?hola=sp&hello=en", client.createProjectRestUri("{0}/{1}?hola={2}&hello={3}", "some", "endpoint", "sp", "en").toString());
        // null and integer used as parameters
        Assert.assertEquals("http://abc.com:123/qcbin/api/domains/ddd/projects/ppp/123/abc?hola=&hello=e%26n%3F%23%25", client.createProjectRestUri("{0}/{1}?hola={2}&hello={3}", 123, "abc", null, "e&n?#%").toString());
        // parameter encoding
        client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "d d/d", "p%pp", "albert", "abcde", "test", "proxyHost", 12345));
        Assert.assertEquals("http://abc.com:123/qcbin/api/domains/d%20d%2Fd/projects/p%25pp/so%2Fme/end%40%20point%25?hola=s%20p&hello=e%26n%3F%23%25", client.createProjectRestUri("{0}/{1}?hola={2}&hello={3}", "so/me", "end@ point%", "s p", "e&n?#%").toString());

    }

    @Test
    public void testCreateRestUri_placeholderAsParamValue() {
        MqmRestClientImpl client = new MqmRestClientImpl(new MqmConnectionConfig("http://abc.com:123/qcbin", "ddd", "ppp", "albert", "abcde", "test", "proxyHost", 12345));
        // expected value is 'http://abc.com:123/qcbin/api/domains/ddd/projects/ppp/{1}/{?hola=}&hello={3}'
        Assert.assertEquals("http://abc.com:123/qcbin/api/domains/ddd/projects/ppp/%7B1%7D/%7B?hola=%7D&hello=%7B3%7D", client.createProjectRestUri("{0}/{1}?hola={2}&hello={3}", "{1}", "{", "}", "{3}").toString());
    }
}
