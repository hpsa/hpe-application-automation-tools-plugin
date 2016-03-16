package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.http.HttpStatus;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by lazara.
 * Created by gadiel.
 */

public class TCConfigurationService {
	private static final Logger logger = Logger.getLogger(TCConfigurationService.class.getName());
	private static TCConfigurationService instance;
	private String m_resourceURL;

	private TCConfigurationService(PluginDescriptor descriptor, SBuildServer server) {
		m_resourceURL = server.getServerRootPath() + descriptor.getPluginResourcesPath("ConfigFile.xml");
	}

	synchronized public static void init(PluginDescriptor descriptor, SBuildServer server) {
		instance = new TCConfigurationService(descriptor, server);
	}

	public static TCConfigurationService getInstance() {
		return instance;
	}

	public String checkConfiguration(NGAConfiguration ngaConfiguration) {
		String resultMessage;

		try {
			NGAResponse result = SDKManager.getService(ConfigurationService.class).validateConfiguration(ngaConfiguration);
			if (result.getStatus() == HttpStatus.SC_OK) {
				resultMessage = "Connection succeeded";
			} else if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
				resultMessage = "Authentication failed";
			} else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
				resultMessage = ngaConfiguration.getApiKey() + " not authorized to shared space " + ngaConfiguration.getSharedSpace();
			} else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
				resultMessage = "Shared space " + ngaConfiguration.getSharedSpace() + " not exists";
			} else {
				resultMessage = "Validation failed for unknown reason; status code: " + result.getStatus();
			}
		} catch (IOException ioe) {
			resultMessage = "Connection failed: " + ioe.getMessage();
		}

		return resultMessage;
	}

	public NGAConfig readConfig() {
		try {
			JAXBContext context = JAXBContext.newInstance(NGAConfig.class);
			Unmarshaller un = context.createUnmarshaller();
			return (NGAConfig) un.unmarshal(new File(m_resourceURL));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveConfig(NGAConfig emp) {
		try {
			JAXBContext context = JAXBContext.newInstance(NGAConfig.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(emp, new File(m_resourceURL));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
