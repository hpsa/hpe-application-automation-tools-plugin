package com.hp.octane.integrations.dto.causes.impl;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.causes.CIEventCause;

/**
 * Created by gullery on 10/02/2016.
 *
 * CI Events causes DTO definitions provider
 */

public final class DTOCausesProvider extends DTOInternalProviderBase {

	public DTOCausesProvider(DTOFactory.DTOConfiguration configuration) {
		dtoPairs.put(CIEventCause.class, CIEventCauseImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
