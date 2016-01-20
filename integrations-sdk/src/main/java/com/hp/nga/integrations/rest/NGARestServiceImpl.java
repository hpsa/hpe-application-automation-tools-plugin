package com.hp.nga.integrations.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Client to provide connectivity to NGA Server - default implementation
 */

public class NGARestServiceImpl implements NGARestService {
	public static final NGARestService instance = new NGARestServiceImpl();

	private final CloseableHttpClient defaultHttpClient;

	private NGARestServiceImpl() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		defaultHttpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	public NGARestClient obtainClient() {
		return new NGARestClient(defaultHttpClient);
	}
}
