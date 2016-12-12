package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.api.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.api.connectivity.OctaneResponse;
import com.hp.octane.plugins.jetbrains.teamcity.OctaneTeamCityPlugin;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

/**
 * Created by lazara.
 * Created by gadiel.
 */

public class TCConfigurationService {
	private static final Logger logger = LogManager.getLogger(TCConfigurationService.class);
	private static final String CONFIG_FILE = "octane-config.xml";

	@Autowired
	private SBuildServer buildServer;
	@Autowired
	private OctaneTeamCityPlugin octaneTeamCityPlugin;

	public String checkConfiguration(OctaneConfiguration octaneConfiguration) {
		String resultMessage;

		try {
			OctaneResponse result = OctaneSDK.getInstance().getConfigurationService().validateConfiguration(octaneConfiguration);
			if (result.getStatus() == HttpStatus.SC_OK) {
				resultMessage = "Connection succeeded";
			} else if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
				resultMessage = "Authentication failed";
			} else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
				resultMessage = octaneConfiguration.getApiKey() + " not authorized to shared space " + octaneConfiguration.getSharedSpace();
			} else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
				resultMessage = "Shared space " + octaneConfiguration.getSharedSpace() + " not exists";
			} else {
				resultMessage = "Validation failed for unknown reason; status code: " + result.getStatus();
			}
		} catch (IOException ioe) {
			resultMessage = "Connection failed: " + ioe.getMessage();
		}

		return resultMessage;
	}

	public OctaneConfigStructure readConfig() {
		OctaneConfigStructure result = null;
		try {
			JAXBContext context = JAXBContext.newInstance(OctaneConfigStructure.class);
			Unmarshaller un = context.createUnmarshaller();
			result = (OctaneConfigStructure) un.unmarshal(getConfigurationResource());
		} catch (JAXBException jaxbe) {
			logger.error("failed to read Octane configuration", jaxbe);
		}
		return result;
	}

	public void saveConfig(OctaneConfigStructure config) {
		try {
			JAXBContext context = JAXBContext.newInstance(OctaneConfigStructure.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(config, getConfigurationResource());
			OctaneSDK.getInstance().getConfigurationService().notifyChange();
		} catch (JAXBException jaxbe) {
			logger.error("failed to save Octane configuration", jaxbe);
		}
	}

	private File getConfigurationResource() {
		return new File(buildServer.getServerRootPath() + octaneTeamCityPlugin.getDescriptor().getPluginResourcesPath(CONFIG_FILE));
	}
}
