package com.hp.nga.integrations.dto;

/**
 * Created by gullery on 08/02/2016.
 * <p>
 * API definition of an internal DTO factories
 */

public interface DTOFactoryInternalBase {
	<T> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException;
}
