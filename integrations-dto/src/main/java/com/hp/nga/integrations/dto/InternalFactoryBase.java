package com.hp.nga.integrations.dto;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * Created by gullery on 08/02/2016.
 * <p>
 * Base generator of the DTOs, this is the entry point of any DTO creation/serialization/deserialization
 */

public abstract class InternalFactoryBase {
	protected abstract <T> boolean ownsDTO(Class<T> targetType);

	protected abstract <T> T innerNewDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException;

	protected abstract <T> String innerDtoToJson(T dto, Class<T> targetType) throws JsonProcessingException;

	protected abstract <T> T innerDtoFromJson(String json, Class<T> targetType) throws IOException;
}
