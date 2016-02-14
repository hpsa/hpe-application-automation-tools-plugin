package com.hp.nga.integrations.dto;

import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.CIPluginInfo;
import com.hp.nga.integrations.dto.general.CIServerInfo;
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
		CIPluginInfo CIPluginInfo = dtoFactory.newDTO(CIPluginInfo.class);
		assertNotNull(CIPluginInfo);
		assertNull(CIPluginInfo.getVersion());

		CIPluginInfo newRef = CIPluginInfo.setVersion("1.2.3");
		assertNotNull(newRef);
		assertEquals(newRef, CIPluginInfo);
		assertEquals("1.2.3", CIPluginInfo.getVersion());
		assertEquals("1.2.3", newRef.getVersion());

		String jsonA = dtoFactory.dtoToJson(CIPluginInfo);
		String jsonB = dtoFactory.dtoToJson(newRef);
		assertEquals(jsonA, jsonB);

		CIPluginInfo CIPluginInfoImplDes = dtoFactory.dtoFromJson(jsonA, CIPluginInfo.class);
		assertNotNull(CIPluginInfoImplDes);
		assertEquals("1.2.3", CIPluginInfoImplDes.getVersion());
	}

	@Test
	public void test_B() {
		CIServerInfo CIServerInfo = dtoFactory.newDTO(CIServerInfo.class);
		CIServerInfo
				.setType(CIServerTypes.JENKINS)
				.setInstanceId("instance id")
				.setInstanceIdFrom(123456789L)
				.setSendingTime(123456789L)
				.setUrl("http://localhost:8080")
				.setVersion("1.2.3");

		assertEquals(CIServerTypes.JENKINS, CIServerInfo.getType());
		assertEquals("instance id", CIServerInfo.getInstanceId());
		assertEquals((Long) 123456789L, CIServerInfo.getInstanceIdFrom());
		assertEquals((Long) 123456789L, CIServerInfo.getSendingTime());
		assertEquals("http://localhost:8080", CIServerInfo.getUrl());
		assertEquals("1.2.3", CIServerInfo.getVersion());

		String json = dtoFactory.dtoToJson(CIServerInfo);
	}
}
