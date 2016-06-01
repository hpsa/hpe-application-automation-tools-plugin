package com.hp.nga.integrations.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.dto.causes.impl.DTOCausesProvider;
import com.hp.nga.integrations.dto.configuration.impl.DTOConfigsProvider;
import com.hp.nga.integrations.dto.connectivity.impl.DTOConnectivityProvider;
import com.hp.nga.integrations.dto.coverage.impl.DTOCoverageProvider;
import com.hp.nga.integrations.dto.events.impl.DTOEventsProvider;
import com.hp.nga.integrations.dto.general.impl.DTOGeneralProvider;
import com.hp.nga.integrations.dto.parameters.impl.DTOParametersProvider;
import com.hp.nga.integrations.dto.pipelines.impl.DTOPipelinesProvider;
import com.hp.nga.integrations.dto.scm.impl.DTOSCMProvider;
import com.hp.nga.integrations.dto.snapshots.impl.DTOSnapshotsProvider;
import com.hp.nga.integrations.dto.stormRunner.impl.DTOStormRunnerProvider;
import com.hp.nga.integrations.dto.tests.impl.DTOTestsProvider;

import javax.xml.bind.JAXBException;
import java.io.File;
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
	private final Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry = new HashMap<Class<? extends DTOBase>, DTOInternalProviderBase>();
	private final ObjectMapper jsonMapper = new ObjectMapper();

	private DTOFactory() {
		DTOCausesProvider.ensureInit(registry, jsonMapper);
		DTOConfigsProvider.ensureInit(registry, jsonMapper);
		DTOConnectivityProvider.ensureInit(registry, jsonMapper);
		DTOCoverageProvider.ensureInit(registry, jsonMapper);
		DTOEventsProvider.ensureInit(registry, jsonMapper);
		DTOGeneralProvider.ensureInit(registry, jsonMapper);
		DTOParametersProvider.ensureInit(registry, jsonMapper);
		DTOPipelinesProvider.ensureInit(registry, jsonMapper);
		DTOSCMProvider.ensureInit(registry, jsonMapper);
		DTOSnapshotsProvider.ensureInit(registry, jsonMapper);
		DTOTestsProvider.ensureInit(registry, jsonMapper);
		DTOStormRunnerProvider.ensureInit(registry, jsonMapper);
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

		DTOInternalProviderBase internalFactory = null;
		try {
			for (Class<? extends DTOBase> supported : registry.keySet()) {
				if (supported.isAssignableFrom(dto.getClass())) {
					internalFactory = registry.get(supported);
					break;
				}
			}
			if (internalFactory != null) {
				return internalFactory.toXML(dto);
			} else {
				throw new RuntimeException(dto.getClass() + " is not supported in this flow");
			}
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("failed to serialize " + dto + " from JSON; error: " + jaxbe.getMessage());
		}
	}

	public <T extends DTOBase> T dtoFromXml(String xml, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		DTOInternalProviderBase internalFactory = null;
		try {
			for (Class<? extends DTOBase> supported : registry.keySet()) {
				if (supported.equals(targetType)) {
					internalFactory = registry.get(supported);
					break;
				}
			}
			if (internalFactory != null) {
				return internalFactory.fromXml(xml);
			} else {
				throw new RuntimeException(targetType + " is not supported in this flow");
			}
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("failed to deserialize " + xml + " into " + targetType + "; error: " + jaxbe.getMessage());
		}
	}

	public <T extends DTOBase> T dtoFromXmlFile(File xml, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		DTOInternalProviderBase internalFactory = null;
		try {
			for (Class<? extends DTOBase> supported : registry.keySet()) {
				if (supported.equals(targetType)) {
					internalFactory = registry.get(supported);
					break;
				}
			}
			if (internalFactory != null) {
				return internalFactory.fromXmlFile(xml);
			} else {
				throw new RuntimeException(targetType + " is not supported in this flow");
			}
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("failed to deserialize " + xml.getName() + " into " + targetType + "; error: " + jaxbe.getMessage());
		}
	}

	private static final class INSTANCE_HOLDER {
		private static final DTOFactory instance = new DTOFactory();
	}
}