package com.hp.octane.plugins.jenkins.configuration;

import hudson.Extension;
import jenkins.model.Jenkins;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public final class PredefinedConfigurationUnmarshaller {

    private final static Logger logger = Logger.getLogger(PredefinedConfigurationUnmarshaller.class.getName());

    private static PredefinedConfigurationUnmarshaller extensionInstance;
    private static Unmarshaller jaxbUnmarshaller;

    public static synchronized PredefinedConfigurationUnmarshaller getExtensionInstance() {
        List<PredefinedConfigurationUnmarshaller> extensions;
        if (extensionInstance == null) {
            extensions = Jenkins.getInstance().getExtensionList(PredefinedConfigurationUnmarshaller.class);
            if (extensions.isEmpty()) {
                logger.log(Level.WARNING, "PredefinedConfigurationUnmarshaller was not initialized properly");
                return null;
            } else {
                extensionInstance = extensions.get(0);
            }
        }

        if (jaxbUnmarshaller == null) {
            try {
                jaxbUnmarshaller = JAXBContext.newInstance(PredefinedConfiguration.class).createUnmarshaller();
            } catch (JAXBException e) {
                logger.log(Level.WARNING, "Unable to create JAXB unmarshaller for predefined server configuration", e);
                return null;
            }
        }
        return extensionInstance;
    }

    public PredefinedConfiguration unmarshall(File configurationFile) {
        if (!configurationFile.canRead()) {
            logger.log(Level.WARNING, "Unable to read predefined server configuration file");
            return null;
        }
        try {
            return (PredefinedConfiguration) jaxbUnmarshaller.unmarshal(configurationFile);
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Unable to unmarshall predefined server configuration", e);
            return null;
        }
    }
}
