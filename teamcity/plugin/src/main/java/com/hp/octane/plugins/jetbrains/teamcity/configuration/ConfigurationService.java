// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.SessionCreationException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.octane.plugins.jetbrains.teamcity.client.MqmRestClientFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationService {

    private final static Logger logger = Logger.getLogger(ConfigurationService.class.getName());

    private static final String PARAM_SHARED_SPACE = "p"; // NON-NLS

    public static final String CLIENT_TYPE = "HPE_JENKINS_PLUGIN";//"HPE_TEAMCITY_PLUGIN";
//    private JenkinsMqmRestClientFactory clientFactory;

//    public static ServerConfiguration getServerConfiguration()
//    {
//        OctanePlugin octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
//        return octanePlugin.getServerConfiguration();
//    }

    public static MqmProject parseUiLocation(String uiLocation) throws IOException, Exception {
        String[] returnString = new String[2];
        try {
            URL url = new URL(uiLocation);
            String location =null;
            int contextPos = uiLocation.indexOf("/ui");
            if (contextPos < 0) {
                //throw FormValidation.errorWithMarkup(markup("red", Messages.ApplicationContextNotFound()));
                throw  new IOException("Application context not found in URL");
            } else {
                location = uiLocation.substring(0, contextPos);
            }
            List<NameValuePair> params = URLEncodedUtils.parse(url.toURI(), "UTF-8");
            for (NameValuePair param: params) {
                if (param.getName().equals(PARAM_SHARED_SPACE)) {
                    String[] sharedSpaceAndWorkspace = param.getValue().split("/");
                    // we are relaxed and allow parameter without workspace in order not to force user to makeup
                    // workspace value when configuring manually or via config API and not via copy & paste
                    if (sharedSpaceAndWorkspace.length < 1 || StringUtils.isEmpty(sharedSpaceAndWorkspace[0])) {
                        //throw FormValidation.errorWithMarkup(markup("red", Messages.UnexpectedSharedSpace()));
                        throw new IOException("Unexpected shared space parameter value");
                    }

                    return new MqmProject(location, sharedSpaceAndWorkspace[0]);
                }
            }
            //throw FormValidation.errorWithMarkup(markup("red", Messages.MissingSharedSpace()));
            throw new IOException("Missing shared space parameter");
        } catch (MalformedURLException e) {
            // throw FormValidation.errorWithMarkup(markup("red", Messages.ConfigurationUrInvalid()));
            throw new IOException("Invalid URL");
        } catch (URISyntaxException e) {
            // throw FormValidation.errorWithMarkup(markup("red", Messages.ConfigurationUrInvalid()));
            throw new IOException("Invalid URL");
        }
    }

    public static String checkConfiguration(String location, String sharedSpace, String username, String password, String clientType) {

        String returnString = "";
        MqmRestClient client = MqmRestClientFactory.create(
                clientType, location, sharedSpace, username, password);

        try {
            client.tryToConnectSharedSpace();
        } catch (AuthenticationException e) {
            returnString = "Authentication failed.";
            logger.log(Level.WARNING, returnString, e);
        } catch (SessionCreationException e) {
            returnString = "Session creation failed.";
            logger.log(Level.WARNING, returnString, e);
        } catch (SharedSpaceNotExistException e) {
            returnString = "Shared space validation failed.";
            logger.log(Level.WARNING, returnString, e);
        } catch (RequestErrorException e) {
            returnString = "Connection check failed due to communication problem.";
            logger.log(Level.WARNING, returnString, e);
        }catch (Throwable t){
            logger.log(Level.WARNING,"",t);
        }

        return returnString;
    }

}
