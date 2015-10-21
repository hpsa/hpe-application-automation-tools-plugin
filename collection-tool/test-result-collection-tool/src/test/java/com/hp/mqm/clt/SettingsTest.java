package com.hp.mqm.clt;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class SettingsTest {

    @Test
    public void testSettings() throws IOException, URISyntaxException {
        Settings settings = new Settings();
        settings.load(getClass().getResource("test.properties").getPath());
        Assert.assertEquals("http://localhost:8080/qcbin", settings.getServer());
        Assert.assertEquals(Integer.valueOf(1001), settings.getSharedspace());
        Assert.assertEquals(Integer.valueOf(1002), settings.getWorkspace());
        Assert.assertEquals("admin", settings.getUser());
        Assert.assertEquals("test.proxy.hpe.com", settings.getProxyHost());
        Assert.assertEquals(Integer.valueOf(8282), settings.getProxyPort());
        Assert.assertEquals("proxyadmin", settings.getProxyUser());
    }

    @Test
    public void testSettings_empty() throws IOException, URISyntaxException {
        Settings settings = new Settings();
        settings.load(null);
        Assert.assertNull(settings.getServer());
        Assert.assertNull(settings.getSharedspace());
        Assert.assertNull(settings.getWorkspace());
        Assert.assertNull(settings.getUser());
        Assert.assertNull(settings.getProxyHost());
        Assert.assertNull(settings.getProxyPort());
        Assert.assertNull(settings.getProxyUser());

        settings.setDefaultConfigFilenameProvider(new TestDefaultConfigFilenameProvider());
        settings.load(null);
        Assert.assertEquals("http://localhost:8282/qcbin", settings.getServer());
        Assert.assertEquals(Integer.valueOf(1011), settings.getSharedspace());
        Assert.assertEquals(Integer.valueOf(1012), settings.getWorkspace());
        Assert.assertEquals("user", settings.getUser());
        Assert.assertEquals("some.proxy.hpe.com", settings.getProxyHost());
        Assert.assertNull(settings.getProxyPort());
        Assert.assertEquals("proxyuser", settings.getProxyUser());
    }

    @Test
    public void testSettings_invalidIntegerValue() throws IOException, URISyntaxException {
        Settings settings = new Settings();
        try {
            settings.load(getClass().getResource("test.propertiesx").getPath());
            Assert.fail();
        } catch (NumberFormatException e) {
            Assert.assertEquals("For input string: \"invalid-integer\"", e.getMessage());
        }
    }

    private class TestDefaultConfigFilenameProvider implements Settings.DefaultConfigFilenameProvider {

        @Override
        public String getDefaultConfigFilename() {
            return getClass().getResource("testDefault.properties").getPath();
        }
    }
}
