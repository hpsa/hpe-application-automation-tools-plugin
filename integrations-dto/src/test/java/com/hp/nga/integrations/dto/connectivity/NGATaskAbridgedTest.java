package com.hp.nga.integrations.dto.connectivity;

import com.hp.nga.integrations.dto.DTOFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gullery on 12/02/2016.
 */

public class NGATaskAbridgedTest {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Test
	public void test_A() {
		String id = UUID.randomUUID().toString();
		String serviceId = UUID.randomUUID().toString();
		String url = "http://non-existing/url";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		String body = "{}";

		NGATaskAbridged task = dtoFactory.newDTO(NGATaskAbridged.class)
				.setId(id)
				.setServiceId(serviceId)
				.setUrl(url)
				.setMethod(NGAHttpMethod.GET)
				.setHeaders(headers)
				.setBody(body);

		assertNotNull(task);

		String json = dtoFactory.dtoToJson(task);
		assertNotNull(json);
		assertFalse(json.isEmpty());

		NGATaskAbridged deTask = dtoFactory.dtoFromJson(json, NGATaskAbridged.class);
		assertNotNull(deTask);
		assertEquals(id, deTask.getId());
		assertEquals(serviceId, deTask.getServiceId());
		assertEquals(url, deTask.getUrl());
		assertEquals(NGAHttpMethod.GET, deTask.getMethod());
		assertNotNull(deTask.getHeaders());
		assertEquals(1, deTask.getHeaders().size());
		assertEquals("application/json", deTask.getHeaders().get("content-type"));
		assertEquals(body, deTask.getBody());
	}
}
