package com.hp.nga.integrations.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hp.nga.integrations.dto.causes.impl.DTOFactoryCauses;
import com.hp.nga.integrations.dto.configuration.impl.DTOFactoryConfigs;
import com.hp.nga.integrations.dto.connectivity.impl.DTOFactoryConnectivity;
import com.hp.nga.integrations.dto.coverage.impl.DTOFactoryCoverage;
import com.hp.nga.integrations.dto.general.impl.DTOFactoryGeneral;
import com.hp.nga.integrations.dto.pipelines.impl.DTOFactoryPipelines;
import com.hp.nga.integrations.dto.scm.impl.DTOFactorySCM;
import com.hp.nga.integrations.dto.snapshots.impl.DTOFactorySnapshots;
import com.hp.nga.integrations.dto.tests.impl.DTOFactoryTests;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gullery on 11/02/2016.
 * <p/>
 * DTO Factory is a single entry point of DTOs management
 */

public final class DTOFactory {
	private final Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry = new HashMap<Class<? extends DTOBase>, DTOFactoryInternalBase>();
	private final ObjectMapper jsonMapper = new ObjectMapper();
	private final ObjectMapper xmlMapper = new XmlMapper();

	private DTOFactory() {
		DTOFactoryCauses.ensureInit(registry, jsonMapper);
		DTOFactoryConfigs.ensureInit(registry, jsonMapper);
		DTOFactoryConnectivity.ensureInit(registry, jsonMapper);
		DTOFactoryCoverage.ensureInit(registry, jsonMapper);
		DTOFactoryGeneral.ensureInit(registry, jsonMapper);
		DTOFactoryPipelines.ensureInit(registry, jsonMapper);
		DTOFactorySCM.ensureInit(registry, jsonMapper);
		DTOFactorySnapshots.ensureInit(registry, jsonMapper);

		DTOFactoryTests.ensureInit(registry, xmlMapper);
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
			return jsonMapper.writeValueAsString(dto);
		} catch (JsonProcessingException ioe) {
			throw new RuntimeException("failed to serialize " + dto + " from JSON; error: " + ioe.getMessage());
		}
	}

	public <T extends DTOBase> String dtoCollectionToJson(List<T> dto) {
		if (dto == null) {
			throw new IllegalArgumentException("dto MUST NOT be null");
		}

		try {
			return jsonMapper.writeValueAsString(dto);
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
			return jsonMapper.readValue(json, targetType);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to deserialize " + json + " into " + targetType + "; error: " + ioe.getMessage());
		}
	}

	public <T extends DTOBase> T[] dtoCollectionFromJson(String json, Class<T[]> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isArray()) {
			throw new IllegalArgumentException("target type MUST be an Array");
		}

		try {
			return jsonMapper.readValue(json, targetType);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to deserialize " + json + " into " + targetType + "; error: " + ioe.getMessage());
		}
	}

	public <T extends DTOBase> String dtoToXml(T dto) {
		if (dto == null) {
			throw new IllegalArgumentException("dto MUST NOT be null");
		}

		try {
			return xmlMapper.writeValueAsString(dto);
		} catch (JsonProcessingException ioe) {
			throw new RuntimeException("failed to serialize " + dto + " from JSON; error: " + ioe.getMessage());
		}
	}

	public <T extends DTOBase> T dtoFromXml(String xml, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		try {
			return xmlMapper.readValue(xml, targetType);
		} catch (IOException ioe) {
			throw new RuntimeException("failed to deserialize " + xml + " into " + targetType + "; error: " + ioe.getMessage());
		}
	}

	private static final class INSTANCE_HOLDER {
		private static final DTOFactory instance = new DTOFactory();
	}
}