package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import org.apache.http.HttpStatus;

import java.io.IOException;

public class TCConfigurationService {

	public static String checkConfiguration(NGAConfiguration ngaConfiguration) {
		String resultMessage = "Connection succeeded";

		try {
			NGAResponse result = SDKManager.getService(ConfigurationService.class).validateConfiguration(ngaConfiguration);
			if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
				resultMessage = "Authentication failed";
			} else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
				resultMessage = ngaConfiguration.getApiKey() + " not authorized to shared space " + ngaConfiguration.getSharedSpace();
			} else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
				resultMessage = "Shared space " + ngaConfiguration.getSharedSpace() + " not exists";
			}
		} catch (IOException ioe) {
			resultMessage = "Connection failed: " + ioe.getMessage();
		}

		return resultMessage;
	}
}
