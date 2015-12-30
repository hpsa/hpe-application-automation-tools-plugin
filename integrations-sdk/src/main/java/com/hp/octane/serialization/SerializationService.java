package com.hp.octane.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by gullery on 29/12/2015.
 * <p/>
 * Common Serialization logic to be found here
 */

public class SerializationService {
	private static final ObjectMapper BASE_OBJECT_MAPPER = new ObjectMapper();

	public static ObjectMapper getObjectMapper() {
		return BASE_OBJECT_MAPPER;
	}
}
