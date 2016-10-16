package com.hp.octane.plugins.jenkins.configuration;

import hudson.Extension;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

@Extension
public final class PredefinedConfigurationUnmarshaller {
	private final static Logger logger = LogManager.getLogger(PredefinedConfigurationUnmarshaller.class);

	private static PredefinedConfigurationUnmarshaller extensionInstance;
	private static Unmarshaller jaxbUnmarshaller;

	public static synchronized PredefinedConfigurationUnmarshaller getExtensionInstance() {
		List<PredefinedConfigurationUnmarshaller> extensions;
		if (extensionInstance == null) {
			extensions = Jenkins.getInstance().getExtensionList(PredefinedConfigurationUnmarshaller.class);
			if (extensions.isEmpty()) {
				logger.warn("PredefinedConfigurationUnmarshaller was not initialized properly");
				return null;
			} else {
				extensionInstance = extensions.get(0);
			}
		}

		if (jaxbUnmarshaller == null) {
			try {
				jaxbUnmarshaller = JAXBContext.newInstance(PredefinedConfiguration.class).createUnmarshaller();
			} catch (JAXBException e) {
				logger.warn("Unable to create JAXB unmarshaller for predefined server configuration", e);
				return null;
			}
		}
		return extensionInstance;
	}

	public PredefinedConfiguration unmarshall(File configurationFile) {
		if (!configurationFile.canRead()) {
			logger.warn("Unable to read predefined server configuration file");
			return null;
		}
		try {
			return (PredefinedConfiguration) jaxbUnmarshaller.unmarshal(configurationFile);
		} catch (JAXBException e) {
			logger.warn("Unable to unmarshall predefined server configuration", e);
			return null;
		}
	}
}
