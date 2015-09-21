package com.hp.mqm.clt;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SettingsTest {

    @Test
    public void testSettings() throws IOException, URISyntaxException {
        Settings settings = new Settings();
        settings.load(getClass().getResource("test.properties").getPath());
        assertEquals("http://localhost:8080/qcbin", settings.getServer());
        assertEquals(Integer.valueOf(1001), settings.getSharedspace());
        assertEquals(Integer.valueOf(1002), settings.getWorkspace());
        assertEquals("admin", settings.getUser());
        assertEquals("password.txt", settings.getPasswordFile());
    }

    @Test
    public void testSettings_empty() throws IOException, URISyntaxException {
        Settings settings = new Settings();
        settings.load(null);
        assertNull(settings.getServer());
        assertNull(settings.getSharedspace());
        assertNull(settings.getWorkspace());
        assertNull(settings.getUser());
        assertNull(settings.getPasswordFile());

        settings.setDefaultConfigFilenameProvider(new TestDefaultConfigFilenameProvider());
        settings.load(null);
        assertEquals("http://localhost:8282/qcbin", settings.getServer());
        assertEquals(Integer.valueOf(1011), settings.getSharedspace());
        assertEquals(Integer.valueOf(1012), settings.getWorkspace());
        assertEquals("user", settings.getUser());
        assertNull(settings.getPasswordFile());
    }

    private class TestDefaultConfigFilenameProvider implements Settings.DefaultConfigFilenameProvider {

        @Override
        public String getDefaultConfigFilename() {
            return "testDefault.properties";
        }
    }
}
