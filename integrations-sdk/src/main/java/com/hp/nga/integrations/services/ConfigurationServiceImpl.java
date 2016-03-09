package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 09/03/2016.
 * <p/>
 * Base implementation of Configuration Service API
 */

final class ConfigurationServiceImpl implements ConfigurationService {
	private static final Logger logger = LogManager.getLogger(ConfigurationServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String SHARED_SPACES_API_URI = "api/shared_spaces/";
	private final SDKManager sdk;

	ConfigurationServiceImpl(SDKManager sdk) {
		this.sdk = sdk;
	}

	public NGAResponse testConnection(NGAConfiguration configuration) throws RuntimeException {
		try {
			NGARestClient restClient = sdk.getInternalService(NGARestService.class).createClient();
			NGARequest request = dtoFactory.newDTO(NGARequest.class)
					.setMethod(NGAHttpMethod.GET)
					.setUrl(configuration.getUrl() + "/" + SHARED_SPACES_API_URI + configuration.getSharedSpace() + "/workspaces");
			return restClient.execute(request, configuration);
		} catch (RuntimeException re) {
			logger.error("failed to connect to " + configuration, re);
			throw new RuntimeException("failed to connect to " + configuration, re);
		}
	}

	public void notifyChange(NGAConfiguration newConfiguration) {
		//  TODO...
	}

	public void notifyChange(CIProxyConfiguration newConfiguration) {
		//  TODO...
	}
}
