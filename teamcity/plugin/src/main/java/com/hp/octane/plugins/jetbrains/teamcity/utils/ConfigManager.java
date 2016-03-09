package com.hp.octane.plugins.jetbrains.teamcity.utils;

import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfig;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by gadiel on 18/01/2016.
 */
public class ConfigManager {
	private String m_resourceURL;
	private static ConfigManager m_ConfigManager;

	private ConfigManager(PluginDescriptor descriptor, SBuildServer server) {
		m_resourceURL = server.getServerRootPath() + descriptor.getPluginResourcesPath("ConfigFile.xml");
	}

	synchronized public static ConfigManager getInstance(PluginDescriptor descriptor, SBuildServer server) {
		if (m_ConfigManager == null) {
			m_ConfigManager = new ConfigManager(descriptor, server);
		}
		return m_ConfigManager;
	}

	public NGAConfig jaxbXMLToObject() {
		try {
			JAXBContext context = JAXBContext.newInstance(NGAConfig.class);
			Unmarshaller un = context.createUnmarshaller();
			return (NGAConfig) un.unmarshal(new File(m_resourceURL));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void jaxbObjectToXML(NGAConfig emp) {
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
