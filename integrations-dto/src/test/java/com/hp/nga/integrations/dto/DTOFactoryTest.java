package com.hp.nga.integrations.dto;

import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by gullery on 08/02/2016.
 */

public class DTOFactoryTest {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Test
	public void test_A() {
		PluginInfo pluginInfo = dtoFactory.newDTO(PluginInfo.class);
		assertNotNull(pluginInfo);
		assertNull(pluginInfo.getVersion());

		PluginInfo newRef = pluginInfo.setVersion("1.2.3");
		assertNotNull(newRef);
		assertEquals(newRef, pluginInfo);
		assertEquals("1.2.3", pluginInfo.getVersion());
		assertEquals("1.2.3", newRef.getVersion());

		String jsonA = dtoFactory.dtoToJson(pluginInfo);
		String jsonB = dtoFactory.dtoToJson(newRef);
		assertEquals(jsonA, jsonB);

		PluginInfo pluginInfoImplDes = dtoFactory.dtoFromJson(jsonA, PluginInfo.class);
		assertNotNull(pluginInfoImplDes);
		assertEquals("1.2.3", pluginInfoImplDes.getVersion());
	}

	@Test
	public void test_B() {
		ServerInfo serverInfo = dtoFactory.newDTO(ServerInfo.class);
		serverInfo
				.setType(CIServerTypes.JENKINS)
				.setInstanceId("instance id")
				.setInstanceIdFrom(123456789L)
				.setSendingTime(123456789L)
				.setUrl("http://localhost:8080")
				.setVersion("1.2.3");

		assertEquals(CIServerTypes.JENKINS, serverInfo.getType());
		assertEquals("instance id", serverInfo.getInstanceId());
		assertEquals((Long) 123456789L, serverInfo.getInstanceIdFrom());
		assertEquals((Long) 123456789L, serverInfo.getSendingTime());
		assertEquals("http://localhost:8080", serverInfo.getUrl());
		assertEquals("1.2.3", serverInfo.getVersion());

		String json = dtoFactory.dtoToJson(serverInfo);
	}
}
