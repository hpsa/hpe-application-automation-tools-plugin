package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.dto.DTOFactoryBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public class DTOFactoryGeneral extends DTOFactoryBase {
	public static final DTOFactoryGeneral instance = new DTOFactoryGeneral();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<Class, Class> dtoPairs;

	protected DTOFactoryGeneral() {
		dtoPairs = new HashMap<Class, Class>();
		dtoPairs.put(PluginInfo.class, PluginInfoImpl.class);
		dtoPairs.put(ServerInfo.class, ServerInfoImpl.class);
		dtoPairs.put(AggregatedInfo.class, AggregatedInfoImpl.class);
		dtoPairs.put(JobConfig.class, JobConfigImpl.class);
		dtoPairs.put(JobsList.class, JobsListImpl.class);
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
