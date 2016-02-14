package com.hp.nga.integrations.dto;

import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.CIPluginInfo;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		CIServerInfo ciServerInfo = dtoFactory.newDTO(CIServerInfo.class);
		ciServerInfo
				.setType(CIServerTypes.JENKINS)
				.setInstanceId("instance id")
				.setInstanceIdFrom(123456789L)
				.setSendingTime(123456789L)
				.setUrl("http://localhost:8080")
				.setVersion("1.2.3");

		assertEquals(CIServerTypes.JENKINS, ciServerInfo.getType());
		assertEquals("instance id", ciServerInfo.getInstanceId());
		assertEquals((Long) 123456789L, ciServerInfo.getInstanceIdFrom());
		assertEquals((Long) 123456789L, ciServerInfo.getSendingTime());
		assertEquals("http://localhost:8080", ciServerInfo.getUrl());
		assertEquals("1.2.3", ciServerInfo.getVersion());

		String json = dtoFactory.dtoToJson(ciServerInfo);
	}

	@Test
	public void test_C() {
		List<CIServerInfo> coll = new ArrayList<CIServerInfo>();
		CIServerInfo instA = dtoFactory.newDTO(CIServerInfo.class);
		instA
				.setType(CIServerTypes.JENKINS)
				.setInstanceId("instance id A")
				.setInstanceIdFrom(123456789L)
				.setSendingTime(123456789L)
				.setUrl("http://localhost:8080/A")
				.setVersion("1.2.3");
		CIServerInfo instB = dtoFactory.newDTO(CIServerInfo.class);
		instB
				.setType(CIServerTypes.JENKINS)
				.setInstanceId("instance id B")
				.setInstanceIdFrom(123456789L)
				.setSendingTime(123456789L)
				.setUrl("http://localhost:8080/B")
				.setVersion("1.2.4");
		CIServerInfo instC = dtoFactory.newDTO(CIServerInfo.class);
		instC
				.setType(CIServerTypes.JENKINS)
				.setInstanceId("instance id C")
				.setInstanceIdFrom(123456789L)
				.setSendingTime(123456789L)
				.setUrl("http://localhost:8080/C")
				.setVersion("1.2.5");
		coll.add(instA);
		coll.add(instB);
		coll.add(instC);

		String json = dtoFactory.dtoCollectionToJson(coll);

		assertNotNull(json);

		CIServerInfo[] newColl = dtoFactory.dtoCollectionFromJson(json, CIServerInfo[].class);
		assertNotNull(newColl);
		assertEquals(3, newColl.length);
	}
}
