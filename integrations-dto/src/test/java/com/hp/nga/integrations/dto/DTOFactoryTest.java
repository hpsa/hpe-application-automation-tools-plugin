package com.hp.nga.integrations.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.IPluginInfo;
import com.hp.nga.integrations.dto.general.IServerInfo;
import com.hp.nga.integrations.dto.general.PluginInfo;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by gullery on 08/02/2016.
 */

public class DTOFactoryTest {

	@Test
	public void test_A() {
		IPluginInfo pluginInfo = DTOFactory.createDTO(IPluginInfo.class);
		assertNotNull(pluginInfo);
		assertNull(pluginInfo.getVersion());

		IPluginInfo newRef = pluginInfo.setVersion("1.2.3");
		assertNotNull(newRef);
		assertEquals(newRef, pluginInfo);
		assertEquals("1.2.3", pluginInfo.getVersion());
		assertEquals("1.2.3", newRef.getVersion());

		try {
			String jsonA = new ObjectMapper().writeValueAsString(pluginInfo);
			String jsonB = new ObjectMapper().writeValueAsString(newRef);
			assertEquals(jsonA, jsonB);
			PluginInfo pluginInfoDes = new ObjectMapper().readValue(jsonA, PluginInfo.class);
		} catch (JsonProcessingException jpe) {
			fail("failed on serialization");
		} catch (IOException ioe) {

		}
	}

	@Test
	public void test_B() {
		IServerInfo serverInfo = DTOFactory.createDTO(IServerInfo.class);
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
	}

	@Test
	public void test_C() {
		IPluginInfo pluginInfo = DTOFactory.createDTO(IPluginInfo.class);
		assertNotNull(pluginInfo);
	}
}
