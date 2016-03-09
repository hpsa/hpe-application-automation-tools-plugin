package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Service - default implementation
 */

public class NGARestServiceImpl implements NGARestService {
	private static final Logger logger = LogManager.getLogger(NGARestClientImpl.class);

	private NGARestServiceImpl() {
	}

	public static NGARestService getInstance() {
		return SERVICE_INSTANCE_HOLDER.instance;
	}

	public NGARestClient obtainClient() {
		return DEFAULT_CLIENT_INSTANCE_HOLDER.defaultClient;
	}

	public NGAResponse testConnection(NGAConfiguration configuration) throws RuntimeException {
		NGARestClientImpl ngaRestClientNGARestClient = new NGARestClientImpl();
		try {
			return ngaRestClientNGARestClient.connectToSharedSpace(configuration);
		} catch (IOException ioe) {
			logger.error("failed to connect to " + configuration, ioe);
			throw new RuntimeException("failed to connect to " + configuration, ioe);
		}
	}

	private static final class SERVICE_INSTANCE_HOLDER {
		private static final NGARestService instance;

		static {
			logger.warn("service initialized");
			instance = new NGARestServiceImpl();
		}
	}

	private static final class DEFAULT_CLIENT_INSTANCE_HOLDER {
		private static final NGARestClient defaultClient;

		static {
			logger.warn("default http client initialized");
			defaultClient = new NGARestClientImpl();
		}
	}
}
