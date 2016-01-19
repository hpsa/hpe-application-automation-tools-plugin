package com.hp.octane.plugins.jetbrains.teamcity.utils;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by gadiel on 18/01/2016.
 */
public class ConfigManager {

    private  PluginDescriptor m_descriptor;
    private  SBuildServer m_server;
    private  String m_resourceURL;
    private static ConfigManager m_ConfigManager;

    private ConfigManager(PluginDescriptor descriptor, SBuildServer server)
    {
        m_descriptor=descriptor;
        m_server = server;
        m_resourceURL = m_server.getServerRootPath() + m_descriptor.getPluginResourcesPath("ConfigFile.xml");

    }

    public static ConfigManager getInstance(PluginDescriptor descriptor, SBuildServer server)
    {
        if(m_ConfigManager==null)
            m_ConfigManager = new ConfigManager(descriptor, server);
        return m_ConfigManager;
    }



    public  Config jaxbXMLToObject() {
        try {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            Unmarshaller un = context.createUnmarshaller();
            Config emp = (Config) un.unmarshal(new File(m_resourceURL));
            return emp;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }


    public  void jaxbObjectToXML(Config emp) {

        try {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(emp, new File(m_resourceURL));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public String printConfig()
    {
        return  jaxbXMLToObject().toString();
    }

}
