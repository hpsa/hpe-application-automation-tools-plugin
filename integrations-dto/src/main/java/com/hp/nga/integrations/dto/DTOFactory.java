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
 * Created by gullery on 08/02/2016.
 * <p>
 * Base generator of the DTOs, this is the entry point of any DTO creation/serialization/deserialization
 */

public abstract class DTOFactory {
	private static final List<DTOFactory> dtoFactories;

	static {
		dtoFactories = new ArrayList<DTOFactory>();
		dtoFactories.add(DTOFactoryGeneral.instance);
		dtoFactories.add(DTOFactoryConfigs.instance);
		dtoFactories.add(DTOFactoryPipelines.instance);
		dtoFactories.add(DTOFactorySnapshots.instance);
	}

	protected abstract <T> boolean ownsDTO(Class<T> targetType);

	protected abstract <T> T innerNewDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException;

	protected abstract <T> String innerDtoToJson(T dto, Class<T> targetType) throws JsonProcessingException;

	protected abstract <T> T innerDtoFromJson(String json, Class<T> targetType) throws IOException;

	public static <T> T newDTO(Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		T result = null;
		for (DTOFactory factory : dtoFactories) {
			if (factory.ownsDTO(targetType)) {
				try {
					result = factory.innerNewDTO(targetType);
				} catch (InstantiationException ie) {
					//  [YG] TODO: add logger here
					throw new RuntimeException("failed to instantiate " + targetType + "; error: " + ie.getMessage());
				} catch (IllegalAccessException iae) {
					//  [YG] TODO: add logger here
					throw new RuntimeException("access denied to " + targetType + "; error: " + iae.getMessage());
				}
				break;
			}
		}
		if (result != null) {
			return result;
		} else {
			//  [YG] TODO: add logger here
			throw new RuntimeException("failed to construct DTO");
		}
	}

	public static <T> String dtoToJson(T dto, Class<T> targetType) {
		if (dto == null) {
			throw new IllegalArgumentException("dto MUST NOT be null");
		}

		String result = null;
		for (DTOFactory factory : dtoFactories) {
			if (factory.ownsDTO(targetType)) {
				try {
					result = factory.innerDtoToJson(dto, targetType);
				} catch (JsonProcessingException ioe) {
					//  [YG] TODO: add logger here
					throw new RuntimeException("failed to serialize DTO " + targetType + " from JSON; error: " + ioe.getMessage());
				}
				break;
			}
		}
		return result;
	}

	public static <T> T dtoFromJson(String json, Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST be an Interface");
		}

		T result = null;
		for (DTOFactory factory : dtoFactories) {
			if (factory.ownsDTO(targetType)) {
				try {
					result = factory.innerDtoFromJson(json, targetType);
				} catch (IOException ioe) {
					//  [YG] TODO: add logger here
					throw new RuntimeException("failed to deserialize DTO " + targetType + " from JSON; error: " + ioe.getMessage());
				}
				break;
			}
		}
		if (result != null) {
			return result;
		} else {
			//  [YG] TODO: add logger here
			throw new RuntimeException("failed to construct DTO from JSON");
		}
	}
}
