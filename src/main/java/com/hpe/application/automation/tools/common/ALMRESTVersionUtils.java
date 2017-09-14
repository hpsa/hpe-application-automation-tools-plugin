package com.hpe.application.automation.tools.common;

import com.hpe.application.automation.tools.model.ALMVersion;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

/**
 * @author Effi Bar-She'an
 */
public class ALMRESTVersionUtils {

    public static ALMVersion toModel(byte[] xml) {

        ALMVersion ret = null;
        try {
            JAXBContext context = JAXBContext.newInstance(ALMVersion.class);
            Unmarshaller unMarshaller = context.createUnmarshaller();
            ret = (ALMVersion) unMarshaller.unmarshal(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new SSEException("Failed to convert XML to ALMVersion", e);
        }

        return ret;
    }
}