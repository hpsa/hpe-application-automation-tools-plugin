package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
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

final class ConfigurationServiceImpl implements ConfigurationService {
	private static final Logger logger = LogManager.getLogger(ConfigurationServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String SHARED_SPACES_API_URI = "api/shared_spaces/";
	private static final String UI_CONTEXT_PATH = "/ui";
	private static final String PARAM_SHARED_SPACE = "p";
	private final SDKManager sdk;

	ConfigurationServiceImpl(SDKManager sdk) {
		this.sdk = sdk;
	}

	public NGAConfiguration buildConfiguration(String rawUrl, String apiKey, String secret) throws IllegalArgumentException {
		NGAConfiguration result = null;
		try {
			String url;
			long sharedSpaceId;
			URL tmpUrl = new URL(rawUrl);
			int contextPos = rawUrl.indexOf(UI_CONTEXT_PATH);
			if (contextPos < 0) {
				throw new IllegalArgumentException("URL does not conform to the expected format");
			} else {
				url = rawUrl.substring(0, contextPos);
			}
			List<NameValuePair> params = URLEncodedUtils.parse(tmpUrl.toURI(), "UTF-8");
			for (NameValuePair param : params) {
				if (param.getName().equals(PARAM_SHARED_SPACE)) {
					String[] sharedSpaceAndWorkspace = param.getValue().split("/");
					if (sharedSpaceAndWorkspace.length < 1 || sharedSpaceAndWorkspace[0].isEmpty()) {
						throw new IllegalArgumentException("shared space parameter MUST be present");
					}
					sharedSpaceId = Long.parseLong(sharedSpaceAndWorkspace[0]);
					result = dtoFactory.newDTO(NGAConfiguration.class)
							.setUrl(url)
							.setSharedSpace(sharedSpaceId)
							.setApiKey(apiKey)
							.setSecret(secret);
				}
			}
		} catch (MalformedURLException murle) {
			throw new IllegalArgumentException("invalid URL", murle);
		} catch (URISyntaxException uirse) {
			throw new IllegalArgumentException("invalid URL (parameters)", uirse);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("shared space parameter MUST be a number");
		}

		if (result == null) {
			throw new IllegalArgumentException("failed to extract NGA server URL and shared space ID from '" + rawUrl + "'");
		} else {
			return result;
		}
	}

	public NGAResponse validateConfiguration(NGAConfiguration configuration) throws IOException {
		NGARestClient restClient = sdk.getInternalService(NGARestService.class).createClient();
		NGARequest request = dtoFactory.newDTO(NGARequest.class)
				.setMethod(NGAHttpMethod.GET)
				.setUrl(configuration.getUrl() + "/" + SHARED_SPACES_API_URI + configuration.getSharedSpace() + "/workspaces");
		return restClient.execute(request, configuration);
	}

	public void notifyChange(NGAConfiguration newConfiguration) {
		//  TODO...
	}

	public void notifyChange(CIProxyConfiguration newConfiguration) {
		//  TODO...
	}
}
