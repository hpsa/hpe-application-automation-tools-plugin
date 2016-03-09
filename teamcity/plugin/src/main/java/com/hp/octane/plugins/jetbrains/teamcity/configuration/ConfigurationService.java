package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ConfigurationService {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String UI_CONTEXT_PATH = "/ui";
	private static final String PARAM_SHARED_SPACE = "p";

	public static String checkConfiguration(NGAConfiguration ngaConfiguration) {
		String returnString = "Connection successful";
		NGAResponse result = SDKManager.getService(com.hp.nga.integrations.api.ConfigurationService.class).testConnection(ngaConfiguration);

//		MqmRestClient client = MqmRestClientFactory.create(
//				clientType, location, sharedSpace, username, password);
//
//		try {
//			client.tryToConnectSharedSpace();
//		} catch (AuthenticationException e) {
//			returnString = "Authentication failed.";
//			logger.log(Level.WARNING, returnString, e);
//		} catch (SessionCreationException e) {
//			returnString = "Session creation failed.";
//			logger.log(Level.WARNING, returnString, e);
//		} catch (SharedSpaceNotExistException e) {
//			returnString = "Shared space validation failed.";
//			logger.log(Level.WARNING, returnString, e);
//		} catch (RequestErrorException e) {
//			returnString = "Connection check failed due to communication problem.";
//			logger.log(Level.WARNING, returnString, e);
//		} catch (Throwable t) {
//			logger.log(Level.WARNING, "", t);
//			returnString = "Connection failed " + t.getMessage();
//		}

		return returnString;
	}

	public static NGAConfiguration buildConfiguration(String originalLocation, String apiKey, String secret) throws Exception {
		try {
			String url;
			long sharedSpaceId;
			URL tmpUrl = new URL(originalLocation);
			int contextPos = originalLocation.indexOf(UI_CONTEXT_PATH);
			if (contextPos < 0) {
				throw new Exception("Application context not found in URL");
			} else {
				url = originalLocation.substring(0, contextPos);
			}
			List<NameValuePair> params = URLEncodedUtils.parse(tmpUrl.toURI(), "UTF-8");
			for (NameValuePair param : params) {
				if (param.getName().equals(PARAM_SHARED_SPACE)) {
					String[] sharedSpaceAndWorkspace = param.getValue().split("/");
					if (sharedSpaceAndWorkspace.length < 1 || sharedSpaceAndWorkspace[0].isEmpty()) {
						throw new Exception("Unexpected shared space parameter value");
					}
					sharedSpaceId = Long.parseLong(sharedSpaceAndWorkspace[0]);
					return dtoFactory.newDTO(NGAConfiguration.class)
							.setUrl(url)
							.setSharedSpace(sharedSpaceId)
							.setApiKey(apiKey)
							.setSecret(secret);
				}
			}
			throw new Exception("Missing shared space parameter");
		} catch (NumberFormatException nfe) {
			throw new Exception("Shared space parameter MUST be a number");
		} catch (MalformedURLException e) {
			throw new Exception("Invalid URL");
		}
	}
}
