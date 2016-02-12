package com.hp.nga.integrations.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.dto.configuration.impl.DTOFactoryConfigs;
import com.hp.nga.integrations.dto.connectivity.impl.DTOFactoryConnectivity;
import com.hp.nga.integrations.dto.general.impl.DTOFactoryGeneral;
import com.hp.nga.integrations.dto.pipelines.impl.DTOFactoryPipelines;
import com.hp.nga.integrations.dto.scm.impl.DTOFactorySCM;
import com.hp.nga.integrations.dto.snapshots.impl.DTOFactorySnapshots;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 11/02/2016.
 * <p>
 * DTO Factory is a single entry point of DTOs management
 */

public final class DTOFactory {
	private final Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry = new HashMap<Class<? extends DTOBase>, DTOFactoryInternalBase>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private DTOFactory() {
		DTOFactoryConfigs.ensureInit(registry, objectMapper);
		DTOFactoryConnectivity.ensureInit(registry, objectMapper);
		DTOFactoryGeneral.ensureInit(registry, objectMapper);
		DTOFactoryPipelines.ensureInit(registry, objectMapper);
		DTOFactorySCM.ensureInit(registry, objectMapper);
		DTOFactorySnapshots.ensureInit(registry, objectMapper);
	}

	public static DTOFactory getInstance() {
		return INSTANCE_HOLDER.instance;
	}

	public <T extends DTOBase> T newDTO(Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}
		if (!registry.containsKey(targetType)) {
			throw new IllegalArgumentException("requested type " + targetType + " is not supported");
		}

		try {
			return registry.get(targetType).instantiateDTO(targetType);
		} catch (InstantiationException ie) {
			throw new RuntimeException("failed to instantiate " + targetType + "; error: " + ie.getMessage());
		} catch (IllegalAccessException iae) {
			throw new RuntimeException("access denied to " + targetType + "; error: " + iae.getMessage());
		}
	}

	public <T extends DTOBase> String dtoToJson(T dto) {
		if (dto == null) {
			throw new IllegalArgumentException("dto MUST NOT be null");
		}

		try {
			return objectMapper.writeValueAsString(dto);
		} catch (JsonProcessingException ioe) {
			throw new RuntimeException("failed to serialize " + dto + " from JSON; error: " + ioe.getMessage());
		}
	}

	public <T extends DTOBase> T dtoFromJson(String json, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		try {
			return objectMapper.readValue(json, targetType);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to deserialize " + json + " into " + targetType + "; error: " + ioe.getMessage());
		}
	}

	private static final class INSTANCE_HOLDER {
		private static final DTOFactory instance = new DTOFactory();
	}
}