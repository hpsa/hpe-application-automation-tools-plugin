package com.hp.octane.integrations.dto.parameters.impl;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.parameters.CIParameter;

/**
 * Created by gullery on 10/02/2016.
 *
 * Job/Build parameters DTOs definitions provider
 */

public final class DTOParametersProvider extends DTOInternalProviderBase {

	public DTOParametersProvider(DTOFactory.DTOConfiguration configuration) {
		dtoPairs.put(CIParameter.class, CIParameterImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
