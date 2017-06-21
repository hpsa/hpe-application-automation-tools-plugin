/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.configuration;

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
