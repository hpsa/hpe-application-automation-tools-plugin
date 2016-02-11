package com.hp.nga.integrations.dto.snapshots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.dto.InternalFactoryBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public class DTOFactorySnapshots extends InternalFactoryBase {
	public static final DTOFactorySnapshots instance = new DTOFactorySnapshots();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<Class, Class> dtoPairs;

	protected DTOFactorySnapshots() {
		dtoPairs = new HashMap<Class, Class>();
		dtoPairs.put(SnapshotNode.class, SnapshotNodeImpl.class);
		dtoPairs.put(SnapshotPhase.class, SnapshotPhaseImpl.class);
	}

	@Override
	protected <T> boolean ownsDTO(Class<T> targetType) {
		return dtoPairs.containsKey(targetType);
	}

	@Override
	protected <T> T innerNewDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}

	@Override
	protected <T> String innerDtoToJson(T dto, Class<T> targetType) throws JsonProcessingException {
		String result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = objectMapper.writeValueAsString(dto);
		}
		return result;
	}

	@Override
	protected <T> T innerDtoFromJson(String json, Class<T> targetType) throws IOException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = objectMapper.readValue(json, targetType);
		}
		return result;
	}
}
