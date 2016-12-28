package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.sal.api.component.ComponentLocator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Path("/testconnection")
public class OctaneRestResource {
    private static final Logger log = LoggerFactory.getLogger(OctaneRestResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testConfiguration(String body) throws IOException {
        OctaneConnectionDTO dto = objectMapper.readValue(body, OctaneConnectionDTO.class);
        return Response.ok(tryToConnect(dto)).build();
    }

    private String tryToConnect(OctaneConnectionDTO dto) {
        try {
            String octaneUrl = dto.getOctaneUrl();
            String accessKey = dto.getAccessKey();
            String apiSecret = dto.getApiSecret();
            String userName = dto.getUserName();
            if (octaneUrl == null || octaneUrl.isEmpty()) {
                return "Octane Instance URL is required";
            }
            if (accessKey == null || accessKey.isEmpty()) {
                return "Access Key is required";
            }

            if (apiSecret == null || apiSecret.isEmpty()) {
                return "API Secret is required";
            }

            if(userName == null || userName.isEmpty()){
                return "Bamboo user name is required";
            }

            if(!isUserAuthorized(userName)){
                return "Bamboo user misconfigured or doesn't have enough permissions\n";
            }

            OctaneConfiguration config = OctaneSDK.getInstance().getConfigurationService().buildConfiguration(octaneUrl, accessKey, apiSecret);
            OctaneResponse result = OctaneSDK.getInstance().getConfigurationService().validateConfiguration(config);
            if (result.getStatus() == HttpStatus.SC_OK) {
                return "Success";
            } else if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                return "You are unauthorized";
            } else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
                return "Connection Forbidden";

            } else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
                return "URL not found";
            }
            return "Error validating octane config";

        } catch (Exception e) {
            log.error("Exception at tryToConnect", e);
            return "Error validating octane config";
        }
    }

    private boolean isUserAuthorized(String userName) {

        BambooUserManager bambooUserManager = ComponentLocator.getComponent(BambooUserManager.class);
        BambooUser bambooUser = bambooUserManager.loadUserByUsername(userName);
        if(bambooUser!=null) {
            return true;
        }
        return false;
    }

}