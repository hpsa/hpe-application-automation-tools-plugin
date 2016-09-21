package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Singleton
@Path("/testconnection")
public class OctanceRestResource {
    private static final Logger log = LoggerFactory.getLogger(OctanceRestResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response testConfiguration(String body) throws JsonProcessingException, IOException {
        DTO dto = objectMapper.readValue(body, DTO.class);
        return Response.ok(tryToConnect(dto)).build();
    }


    private String tryToConnect(DTO dto) {
        try {
            String octaneUrl = dto.getOctaneUrl();
            String accessKey = dto.getAccessKey();
            String apiSecret = dto.getApiSecret();
            if (octaneUrl == null || octaneUrl.isEmpty()) {
                return "Octane Instance URL is required.";
            }
            if (accessKey == null || accessKey.isEmpty()) {
                return "Access Key is required.";
            }

            if (apiSecret == null || apiSecret.isEmpty()) {
                return "API Secret is required.";
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class DTO {
        private String octaneUrl;
        private String accessKey;
        private String apiSecret;

        public String getOctaneUrl() {
            return octaneUrl;
        }

        public void setOctaneUrl(String octaneUrl) {
            this.octaneUrl = octaneUrl;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }
    }
}