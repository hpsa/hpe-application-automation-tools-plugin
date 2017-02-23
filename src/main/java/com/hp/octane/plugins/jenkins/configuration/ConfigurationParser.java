package com.hp.octane.plugins.jenkins.configuration;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.*;
import com.hp.octane.plugins.jenkins.Messages;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import hudson.Extension;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@Extension
public class ConfigurationParser {

	private final static Logger logger = LogManager.getLogger(ConfigurationParser.class);

	private static final String PARAM_SHARED_SPACE = "p"; // NON-NLS

	private JenkinsMqmRestClientFactory clientFactory;

	public static MqmProject parseUiLocation(String uiLocation) throws FormValidation {
		try {
			URL url = new URL(uiLocation);
			String location;
			int contextPos = uiLocation.indexOf("/ui");
			if (contextPos < 0) {
				throw FormValidation.errorWithMarkup(markup("red", Messages.ApplicationContextNotFound()));
			} else {
				location = uiLocation.substring(0, contextPos);
			}
			List<NameValuePair> params = URLEncodedUtils.parse(url.toURI(), "UTF-8");
			for (NameValuePair param : params) {
				if (param.getName().equals(PARAM_SHARED_SPACE)) {
					String[] sharedSpaceAndWorkspace = param.getValue().split("/");
					// we are relaxed and allow parameter without workspace in order not to force user to makeup
					// workspace value when configuring manually or via config API and not via copy & paste
					if (sharedSpaceAndWorkspace.length < 1 || StringUtils.isEmpty(sharedSpaceAndWorkspace[0])) {
						throw FormValidation.errorWithMarkup(markup("red", Messages.UnexpectedSharedSpace()));
					}
					return new MqmProject(location, sharedSpaceAndWorkspace[0]);
				}
			}
			throw FormValidation.errorWithMarkup(markup("red", Messages.MissingSharedSpace()));
		} catch (MalformedURLException e) {
			throw FormValidation.errorWithMarkup(markup("red", Messages.ConfigurationUrInvalid()));
		} catch (URISyntaxException e) {
			throw FormValidation.errorWithMarkup(markup("red", Messages.ConfigurationUrInvalid()));
		}
	}

	public FormValidation checkConfiguration(String location, String sharedSpace, String username, String password) {
		MqmRestClient client = clientFactory.obtainTemp(location, sharedSpace, username, password);
		try {
			client.validateConfiguration();
		} catch (AuthenticationException ae) {
			logger.warn("Authentication failure", ae);
			return FormValidation.errorWithMarkup(markup("red", Messages.AuthenticationFailure()));
		} catch (AuthorizationException ae) {
			logger.warn("Authorization failure", ae);
			return FormValidation.errorWithMarkup(markup("red", Messages.AuthorizationFailure()));
		} catch (SharedSpaceNotExistException ssnee) {
			logger.warn("Shared space validation failure", ssnee);
			return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionSharedSpaceInvalid()));
		} catch (LoginErrorException lee) {
			logger.warn("General logic failure", lee);
			return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionFailure()));
		} catch (RequestErrorException ree) {
			logger.warn("Connection check failed due to communication problem", ree);
			return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionFailure()));
		}
		return FormValidation.okWithMarkup(markup("green", Messages.ConnectionSuccess()));
	}

	public static String markup(String color, String message) {
		return "<font color=\"" + color + "\"><b>" + message + "</b></font>";
	}

	@Inject
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}

	/*
	 * To be used in tests only.
	 */
	public void _setMqmRestClientFactory(JenkinsMqmRestClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

}
