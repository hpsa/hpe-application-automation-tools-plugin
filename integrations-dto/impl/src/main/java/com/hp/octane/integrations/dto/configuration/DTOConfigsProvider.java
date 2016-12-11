package com.hp.octane.integrations.dto.configuration;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;

/**
 * Created by gullery on 10/02/2016.
 *
 * Configurations DTOs definitions provider
 */

public final class DTOConfigsProvider extends DTOInternalProviderBase {

	public DTOConfigsProvider() {
		dtoPairs.put(OctaneConfiguration.class, OctaneConfigurationImpl.class);
		dtoPairs.put(CIProxyConfiguration.class, CIProxyConfigurationImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}