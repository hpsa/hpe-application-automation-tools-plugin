package com.hp.nga.integrations.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.nga.integrations.dto.configuration.DTOFactoryConfigs;
import com.hp.nga.integrations.dto.general.DTOFactoryGeneral;
import com.hp.nga.integrations.dto.pipelines.DTOFactoryPipelines;
import com.hp.nga.integrations.dto.snapshots.DTOFactorySnapshots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 11/02/2016.
 * <p>
 * Base implementation of DTOFactory - the only instance to be used
 */

class DTOFactoryImpl implements DTOFactory {
	static final DTOFactory instance = new DTOFactoryImpl();
	private final List<InternalFactoryBase> factories = new ArrayList<InternalFactoryBase>();

	private DTOFactoryImpl() {
		factories.add(DTOFactoryGeneral.instance);
		factories.add(DTOFactoryConfigs.instance);
		factories.add(DTOFactoryPipelines.instance);
		factories.add(DTOFactorySnapshots.instance);
	}

	public <T extends DTO> T newDTO(Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		T result = null;
		for (InternalFactoryBase factory : factories) {
			if (factory.ownsDTO(targetType)) {
				try {
					result = factory.innerNewDTO(targetType);
				} catch (InstantiationException ie) {
					throw new RuntimeException("failed to instantiate " + targetType + "; error: " + ie.getMessage());
				} catch (IllegalAccessException iae) {
					throw new RuntimeException("access denied to " + targetType + "; error: " + iae.getMessage());
				}
				break;
			}
		}
		if (result != null) {
			return result;
		} else {
			throw new RuntimeException("failed to construct DTO");
		}
	}

	public <T extends DTO> String dtoToJson(T dto, Class<T> targetType) {
		if (dto == null) {
			throw new IllegalArgumentException("dto MUST NOT be null");
		}

		String result = null;
		for (InternalFactoryBase factory : factories) {
			if (factory.ownsDTO(targetType)) {
				try {
					result = factory.innerDtoToJson(dto, targetType);
				} catch (JsonProcessingException ioe) {
					throw new RuntimeException("failed to serialize DTO " + targetType + " from JSON; error: " + ioe.getMessage());
				}
				break;
			}
		}
		return result;
	}

	public <T extends DTO> T dtoFromJson(String json, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		T result = null;
		for (InternalFactoryBase factory : factories) {
			if (factory.ownsDTO(targetType)) {
				try {
					result = factory.innerDtoFromJson(json, targetType);
				} catch (IOException ioe) {
					throw new RuntimeException("failed to deserialize DTO " + targetType + " from JSON; error: " + ioe.getMessage());
				}
				break;
			}
		}
		if (result != null) {
			return result;
		} else {
			throw new RuntimeException("failed to construct DTO from JSON");
		}
	}
}