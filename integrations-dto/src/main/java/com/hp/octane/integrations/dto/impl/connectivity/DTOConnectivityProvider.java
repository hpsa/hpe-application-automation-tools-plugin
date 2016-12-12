package com.hp.octane.integrations.dto.impl.connectivity;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.api.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.api.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.api.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.api.connectivity.OctaneTaskAbridged;

/**
 * Created by gullery on 10/02/2016.
 *
 * Connectivity related DTOs definitions provider
 */

public final class DTOConnectivityProvider extends DTOInternalProviderBase {

	public DTOConnectivityProvider() {
		dtoPairs.put(OctaneRequest.class, OctaneRequestImpl.class);
		dtoPairs.put(OctaneResponse.class, OctaneResponseImpl.class);
		dtoPairs.put(OctaneTaskAbridged.class, OctaneTaskAbridgedImpl.class);
		dtoPairs.put(OctaneResultAbridged.class, OctaneResultAbridgedImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
