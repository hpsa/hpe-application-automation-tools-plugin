package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.SessionCreationException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by gadiel on 21/01/2016.
 */
public class TestConnectionActionController extends AbstractActionController {
    private static final Logger logger = Logger.getLogger(TestConnectionActionController.class.getName());
    private static final String CLIENT_TYPE = "HPE_TEAMCITY_PLUGIN";

    private SBuildServer m_server;
    PluginDescriptor m_descriptor;

    Config m_config;

    public TestConnectionActionController(SBuildServer server, ProjectManager projectManager,
                                          BuildTypeResponsibilityFacade responsibilityFacade, ProjectSettingsManager projectSettingsManager, PluginDescriptor descriptor) {
        m_server = server;
        m_descriptor = descriptor;

    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
       String username = httpServletRequest.getParameter("username1");
        String password = httpServletRequest.getParameter("password1");
        String url_str = httpServletRequest.getParameter("server");

        // separating the sharedSpace from the uiLocation
        String sharedSpace;
        String uiLocation;
        int start = url_str.indexOf("p=");
        int end = (url_str.substring(start)).indexOf("/");
        if(end!=-1) {
            sharedSpace = url_str.substring(start + 2, start + end);
            uiLocation = url_str.substring(0,start+end);
        }
        else
        {
            sharedSpace = url_str.substring(start + 2);
            uiLocation=url_str;
        }
        int index=0;
        for(int i=0; i<url_str.length(); i++)
            if ((url_str.charAt(i))==':')
                index = i;


        String Location = url_str.substring(0,index+5);

        String returnString ="OK";
        PrintWriter writer;
       // MqmConnectionConfig clientConfig = new MqmConnectionConfig(Location, sharedSpace, username, password, CLIENT_TYPE);
        MqmRestClient client = com.hp.octane.plugins.jetbrains.teamcity.client.MqmRestClientFactory.create(CLIENT_TYPE,Location, sharedSpace, username, password);

        try {
            client.tryToConnectSharedSpace();
        }
        catch (AuthenticationException e) {
            logger.log(Level.WARNING, "Authentication failed.", e);
            returnString ="Authentication failed.";
        } catch (SessionCreationException e) {
            logger.log(Level.WARNING, "Session creation failed.", e);
            returnString ="Session creation failed.";
        } catch (SharedSpaceNotExistException e) {
            logger.log(Level.WARNING, "Shared space validation failed.", e);
            returnString ="Shared space validation failed.";
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Connection check failed due to communication problem.", e);
            returnString ="Connection check failed due to communication problem.";
        }

        try
        {
            writer = httpServletResponse.getWriter();
            writer.write(returnString);
        }

        catch(IOException e){}


        return null;
    }

    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }


}
