package com.hp.octane.integrations.dto;

import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gullery on 03/01/2016.
 */

public class StatusInfoTest {
	private final static DTOFactory dtoFactory = DTOFactory.getInstance();
	private final static String PLUGIN_VERSION = "2.3.4";
	private final static String SERVER_VERION = "1.2.3";
	private final static String INPUT_SERVER_URL = "http://some.url/";
	private final static String EXPECTED_SERVER_URL = "http://some.url";
	private final static String SERVER_UUID = UUID.randomUUID().toString();
	private final static Long SERVER_UUID_FROM = System.currentTimeMillis();
	private final static Long SYNC_TIME = System.currentTimeMillis();

	@Test
	public void testA() {
		CIProviderSummaryInfo statusInfo = dtoFactory.newDTO(CIProviderSummaryInfo.class);

		CIPluginInfo CIPluginInfo = dtoFactory.newDTO(CIPluginInfo.class)
				.setVersion(PLUGIN_VERSION);

		CIServerInfo CIServerInfo = dtoFactory.newDTO(CIServerInfo.class)
				.setType(CIServerTypes.JENKINS)
				.setVersion(SERVER_VERION)
				.setInstanceId(SERVER_UUID)
				.setInstanceIdFrom(SERVER_UUID_FROM)
				.setSendingTime(SYNC_TIME)
				.setUrl(INPUT_SERVER_URL);

		statusInfo.setPlugin(CIPluginInfo);
		statusInfo.setServer(CIServerInfo);

		String json = dtoFactory.dtoToJson(statusInfo);

		CIProviderSummaryInfo newStatus = dtoFactory.dtoFromJson(json, CIProviderSummaryInfo.class);

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
