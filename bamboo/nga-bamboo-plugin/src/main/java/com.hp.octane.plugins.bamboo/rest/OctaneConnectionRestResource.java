package com.hp.octane.plugins.bamboo.rest;


import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;

import static com.opensymphony.xwork2.Action.SUCCESS;

@Provider
@Path("/connection")
public class OctaneConnectionRestResource implements InitializingBean {

    private final PluginSettingsFactory settingsFactory;
    private static final Logger log = LoggerFactory.getLogger(OctaneConnectionRestResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String octaneUrl="";
    private String accessKey="";
    private String apiSecret="";
    private String userName="";
    private String uuid="";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response connectToOctane(String body) throws IOException {
        OctaneConnectionDTO dto = objectMapper.readValue(body, OctaneConnectionDTO.class);
        OctaneSDK.getInstance().getPluginServices().getServerInfo();
        String result = doSave(dto);
        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectionDetails(String body) throws IOException {
        String json = DTOFactory.getInstance().dtoToJson(OctaneSDK.getInstance().getPluginServices().getServerInfo());
        return Response.ok(json).build();
    }

    public OctaneConnectionRestResource(PluginSettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;
        readData();
    }

    public String doSave(OctaneConnectionDTO dto) {
        octaneUrl = dto.getOctaneUrl();
        accessKey = dto.getAccessKey();
        apiSecret = dto.getApiSecret();
        userName = dto.getUserName();
        log.info("save configuration");
        PluginSettings settings = settingsFactory.createGlobalSettings();
        settings.put(OctaneConfigurationKeys.OCTANE_URL, octaneUrl);
        settings.put(OctaneConfigurationKeys.ACCESS_KEY, accessKey);
        settings.put(OctaneConfigurationKeys.API_SECRET, apiSecret);
        settings.put(OctaneConfigurationKeys.IMPERSONATION_USER, userName);
        OctaneSDK.getInstance().getConfigurationService().notifyChange();
        return SUCCESS;
    }

    public void afterPropertiesSet() throws Exception {
        try {
            OctaneSDK.init(new BambooPluginServices(settingsFactory), true);
        } catch (IllegalStateException ise) {
            log.warn("failed to init SDK, more than a single init?", ise);
        }
    }

    private void readData() {
        PluginSettings settings = settingsFactory.createGlobalSettings();
        if (settings.get(OctaneConfigurationKeys.UUID) != null) {
            uuid = String.valueOf(settings.get(OctaneConfigurationKeys.UUID));
        } else {
            // generate new UUID
            uuid = UUID.randomUUID().toString();
            settings.put(OctaneConfigurationKeys.UUID, uuid);
        }
        if (settings.get(OctaneConfigurationKeys.OCTANE_URL) != null) {
            octaneUrl = String.valueOf(settings.get(OctaneConfigurationKeys.OCTANE_URL));
        }

        if (settings.get(OctaneConfigurationKeys.ACCESS_KEY) != null) {
            accessKey = String.valueOf(settings.get(OctaneConfigurationKeys.ACCESS_KEY));
        }
        if (settings.get(OctaneConfigurationKeys.API_SECRET) != null) {
            apiSecret = String.valueOf(settings.get(OctaneConfigurationKeys.API_SECRET));
        }
        if (settings.get(OctaneConfigurationKeys.IMPERSONATION_USER) != null) {
            userName = String.valueOf(settings.get(OctaneConfigurationKeys.IMPERSONATION_USER));
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        userName = username;
    }

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}