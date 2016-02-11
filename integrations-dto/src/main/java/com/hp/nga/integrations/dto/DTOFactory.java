package com.hp.nga.integrations.dto;

/**
 * Created by gullery on 11/02/2016.
 */

public interface DTOFactory {
	DTOFactory instance = DTOFactoryBase.instance;

	<T> T newDTO(Class<T> targetType);

	<T> String dtoToJson(T dto, Class<T> targetType);

	<T> T dtoFromJson(String json, Class<T> targetType);
}
