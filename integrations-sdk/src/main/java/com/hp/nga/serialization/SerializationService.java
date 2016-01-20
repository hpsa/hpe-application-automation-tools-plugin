package com.hp.nga.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by gullery on 29/12/2015.
 * <p/>
 * Common Serialization logic to be found here
 */

public class SerializationService {
	private static final ObjectMapper BASE_OBJECT_MAPPER = new ObjectMapper();

	public static <T> String toJSON(T input) throws SerializationException {
		try {
			return BASE_OBJECT_MAPPER.writeValueAsString(input);
		} catch (JsonProcessingException jpe) {
			throw new SerializationException(jpe);
		}
	}

	public static <T> T fromJSON(String input, Class<T> targetType) throws SerializationException {
		try {
			return targetType.cast(BASE_OBJECT_MAPPER.readValue(input, targetType));
		} catch (ClassCastException cce) {
			throw new SerializationException(cce);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
}
