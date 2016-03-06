package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Service - default implementation
 */

public class NGARestServiceImpl implements NGARestService {
	private static final Object CLIENT_INIT_LOCK = new Object();
	private volatile NGARestClient defaultNGARestClient;

	private NGARestServiceImpl() {
	}

	public static NGARestService getInstance() {
		return INSTANCE_HOLDER.instance;
	}

	public NGARestClient obtainClient() {
		if (defaultNGARestClient == null) {
			synchronized (CLIENT_INIT_LOCK) {
				if (defaultNGARestClient == null) {
					defaultNGARestClient = new NGARestClientImpl();
				}
			}
		}
		return defaultNGARestClient;
	}

	public NGAResponse testConnection(NGAConfiguration configuration) {
		//  TODO: implement test connection NOT on the default rest client but creating the new one
		return null;
	}

	private static final class INSTANCE_HOLDER {
		private static final NGARestService instance = new NGARestServiceImpl();
	}
}
