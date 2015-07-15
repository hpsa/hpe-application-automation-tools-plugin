package com.hp.mqm.clt;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SettingsTest {

    @Test
    public void testSettings() throws IOException {
        Settings settings = new Settings();
        settings.load("test.properties");
        assertEquals("http://localhost:8080/qcbin", settings.getServer());
        assertEquals("default", settings.getDomain());
        assertEquals("MAIN", settings.getProject());
        assertNull(settings.getWorkspace());
        assertEquals("admin", settings.getUser());
        assertEquals("password.txt", settings.getPasswordFile());
    }
}
