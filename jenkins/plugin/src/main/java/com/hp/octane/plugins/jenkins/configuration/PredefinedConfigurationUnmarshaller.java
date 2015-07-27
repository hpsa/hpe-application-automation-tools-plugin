package com.hp.octane.plugins.jenkins.configuration;

import hudson.Extension;
import jenkins.model.Jenkins;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

@Extension
public final class PredefinedConfigurationUnmarshaller {

    private static PredefinedConfigurationUnmarshaller extensionInstance;
    private static Unmarshaller jaxbUnmarshaller;

    public static synchronized PredefinedConfigurationUnmarshaller getExtensionInstance() {
        List<PredefinedConfigurationUnmarshaller> extensions;
        if (extensionInstance == null) {
            extensions = Jenkins.getInstance().getExtensionList(PredefinedConfigurationUnmarshaller.class);
            if (extensions.isEmpty()) {
                throw new RuntimeException("PredefinedConfigurationUnmarshaller was not initialized properly");
            } else {
                extensionInstance = extensions.get(0);
            }
        }

        if (jaxbUnmarshaller == null) {
            try {
                jaxbUnmarshaller = JAXBContext.newInstance(PredefinedConfiguration.class).createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException("Unbale to create JAXB unmarshaller for predefined server configuration");
            }
        }
        return extensionInstance;
    }

    public PredefinedConfiguration unmarshall(File configurationFile) {
        if (!configurationFile.canRead()) {
            throw new RuntimeException("Unable to read predefined server configuration file");
        }
        try {
            return (PredefinedConfiguration) jaxbUnmarshaller.unmarshal(configurationFile);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to unmarshall predefined server configuration");
        }
    }
}
