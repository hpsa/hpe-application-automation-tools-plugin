package com.hp.application.automation.tools.sse.common;

import junit.framework.Assert;

public class TestRestXmlUtils {
    
    public void testFieldXml() throws Exception {
        
        final String FIELD = "duration";
        final String VALUE = "60";
        Assert.assertEquals(
                String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", FIELD, VALUE),
                RestXmlUtils.fieldXml(FIELD, VALUE));
    }
}
