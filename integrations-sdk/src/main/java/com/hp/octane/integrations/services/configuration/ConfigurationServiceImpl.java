package com.hp.octane.integrations.services.configuration;

import com.hp.octane.integrations.SDKServiceBase;
import com.hp.octane.integrations.api.ConfigurationService;
import com.hp.octane.integrations.api.RestClient;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by gullery on 09/03/2016.
 * <p/>
 * Base implementation of Configuration Service API
 */

public final class ConfigurationServiceImpl extends SDKServiceBase implements ConfigurationService {
	private static final Logger logger = LogManager.getLogger(ConfigurationServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String SHARED_SPACES_API_URI = "api/shared_spaces/";
	private static final String UI_CONTEXT_PATH = "/ui";
	private static final String PARAM_SHARED_SPACE = "p";

	public ConfigurationServiceImpl(Object configurator) {
		super(configurator);
	}

	public OctaneConfiguration buildConfiguration(String rawUrl, String apiKey, String secret) throws IllegalArgumentException {
		OctaneConfiguration result = null;
		try {
			String url;
			URL tmpUrl = new URL(rawUrl);
			int contextPathPosition = rawUrl.indexOf(UI_CONTEXT_PATH);
			if (contextPathPosition < 0) {
				throw new IllegalArgumentException("URL does not conform to the expected format");
			} else {
				url = rawUrl.substring(0, contextPathPosition);
			}
			List<NameValuePair> params = URLEncodedUtils.parse(tmpUrl.toURI(), "UTF-8");
			for (NameValuePair param : params) {
				if (param.getName().equals(PARAM_SHARED_SPACE)) {
					String[] sharedSpaceAndWorkspace = param.getValue().split("/");
					if (sharedSpaceAndWorkspace.length < 1 || sharedSpaceAndWorkspace[0].isEmpty()) {
						throw new IllegalArgumentException("shared space parameter MUST be present");
					}
					result = dtoFactory.newDTO(OctaneConfiguration.class)
							.setUrl(url)
							.setSharedSpace(sharedSpaceAndWorkspace[0])
							.setApiKey(apiKey)
							.setSecret(secret);
				}
			}
		} catch (MalformedURLException murle) {
			throw new IllegalArgumentException("invalid URL", murle);
		} catch (URISyntaxException uirse) {
			throw new IllegalArgumentException("invalid URL (parameters)", uirse);
		}

		if (result == null) {
			throw new IllegalArgumentException("failed to extract NGA server URL and shared space ID from '" + rawUrl + "'");
		} else {
			return result;
		}
	}

	public OctaneResponse validateConfiguration(OctaneConfiguration configuration) throws IOException {
		if (configuration == null) {
			throw new IllegalArgumentException("configuration MUST not be null");
		}
		if (!configuration.isValid()) {
			throw new IllegalArgumentException("configuration " + configuration + " is not valid");
		}

		CIProxyConfiguration proxyConfiguration = getPluginServices().getProxyConfiguration(configuration.getUrl());
		RestClient restClientImpl = getRestService().createClient(proxyConfiguration);
		OctaneRequest request = dtoFactory.newDTO(OctaneRequest.class)
				.setMethod(HttpMethod.GET)
				.setUrl(configuration.getUrl() + "/" + SHARED_SPACES_API_URI + configuration.getSharedSpace() + "/workspaces");
		return restClientImpl.execute(request, configuration);
	}

	public void notifyChange(OctaneConfiguration newConfiguration) {
		//  TODO:
		//  notify bridge service
	}

	public void notifyChange(CIProxyConfiguration newConfiguration) {
		//  TODO
		//  notify bridge service
	}
}
