package com.hp.nga.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Client to provide connectivity to NGA Server - default implementation
 */

public class NGARestClientServiceImpl implements NGARestClientService {
	public static final NGARestClientService instance = new NGARestClientServiceImpl();

	private final CloseableHttpClient defaultHttpClient;

	private NGARestClientServiceImpl() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		defaultHttpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	public NGARestClient obtainClient() {
		return new NGARestClient(defaultHttpClient);
	}
}
