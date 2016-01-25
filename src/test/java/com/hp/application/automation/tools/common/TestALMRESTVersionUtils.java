package com.hp.application.automation.tools.common;

import com.hp.application.automation.tools.model.ALMVersion;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * @author Effi Bar-She'an
 */
public class TestALMRESTVersionUtils {

    @Test
    public void testToModel() throws UnsupportedEncodingException {

        final String MAJOR = "12";
        final String MINOR = "5";
        final String _dataFormat = "<SiteVersions>\n" +
                "<SiteVersion>12.50.39 (Patch 1)</SiteVersion>\n" +
                "<MajorVersion>%s</MajorVersion>\n" +
                "<MinorVersion>%s</MinorVersion>\n" +
                "<MinorMinorVersion>0</MinorMinorVersion>\n" +
                "<PatchLevel>1</PatchLevel>\n" +
                "<FormatVersion>1</FormatVersion>\n" +
                "</SiteVersions>";
        ALMVersion version = ALMRESTVersionUtils.toModel(String.format(_dataFormat, MAJOR, MINOR).getBytes("UTF-8"));
        Assert.assertEquals(MAJOR, version.getMajorVersion());
        Assert.assertEquals(MINOR, version.getMinorVersion());
    }
}
