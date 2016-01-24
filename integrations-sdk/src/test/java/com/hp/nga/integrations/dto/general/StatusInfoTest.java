package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.services.serialization.SerializationService;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gullery on 03/01/2016.
 */

public class StatusInfoTest {
	private final static String PLUGIN_VERSION = "2.3.4";

	private final static String SERVER_VERION = "1.2.3";
	private final static String INPUT_SERVER_URL = "http://some.url/";
	private final static String EXPECTED_SERVER_URL = "http://some.url";
	private final static String SERVER_UUID = UUID.randomUUID().toString();
	private final static Long SERVER_UUID_FROM = System.currentTimeMillis();
	private final static Long SYNC_TIME = System.currentTimeMillis();

	@Test
	public void testA() {
		AggregatedStatusInfo statusInfo = new AggregatedStatusInfo();

		PluginInfoDTO pluginInfoDTO = new PluginInfoDTO();
		pluginInfoDTO.setVersion(PLUGIN_VERSION);

		ServerInfoDTO serverInfoDTO = new ServerInfoDTO();
		serverInfoDTO.setType(CIServerTypes.JENKINS);
		serverInfoDTO.setVersion(SERVER_VERION);
		serverInfoDTO.setInstanceId(SERVER_UUID);
		serverInfoDTO.setInstanceIdFrom(SERVER_UUID_FROM);
		serverInfoDTO.setSendingTime(SYNC_TIME);
		serverInfoDTO.setUrl(INPUT_SERVER_URL);

		statusInfo.setPlugin(pluginInfoDTO);
		statusInfo.setServer(serverInfoDTO);

		String json = SerializationService.toJSON(statusInfo);

		AggregatedStatusInfo newStatus = SerializationService.fromJSON(json, AggregatedStatusInfo.class);

		assertNotNull(newStatus);

		assertNotNull(newStatus.getPlugin());
		assertEquals(PLUGIN_VERSION, newStatus.getPlugin().getVersion());

		assertNotNull(newStatus.getServer());
		assertEquals(CIServerTypes.JENKINS, newStatus.getServer().getType());
		assertEquals(SERVER_VERION, newStatus.getServer().getVersion());
		assertEquals(SERVER_UUID, newStatus.getServer().getInstanceId());
		assertEquals(SERVER_UUID_FROM, newStatus.getServer().getInstanceIdFrom());
		assertEquals(SYNC_TIME, newStatus.getServer().getSendingTime());
		assertEquals(EXPECTED_SERVER_URL, newStatus.getServer().getUrl());
	}
}
