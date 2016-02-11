package com.hp.nga.integrations.dto;

/**
 * Created by gullery on 11/02/2016.
 * <p>
 * DTO Factory API definition
 */

public interface DTOFactory {
	DTOFactory instance = DTOFactoryImpl.instance;

	<T extends DTO> T newDTO(Class<T> targetType);

	<T extends DTO> String dtoToJson(T dto, Class<T> targetType);

	<T extends DTO> T dtoFromJson(String json, Class<T> targetType);
}
