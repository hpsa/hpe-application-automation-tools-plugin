package com.hp.octane.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * Connectivity related DTOs definitions provider
 */

public final class DTOConnectivityProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOConnectivityProvider() {
		dtoPairs.put(OctaneRequest.class, OctaneRequestImpl.class);
		dtoPairs.put(OctaneResponse.class, OctaneResponseImpl.class);
		dtoPairs.put(OctaneTaskAbridged.class, OctaneTaskAbridgedImpl.class);
		dtoPairs.put(OctaneResultAbridged.class, OctaneResultAbridgedImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(OctaneRequest.class, OctaneRequestImpl.class);
		dtoImplResolver.addMapping(OctaneResponse.class, OctaneResponseImpl.class);
		dtoImplResolver.addMapping(OctaneTaskAbridged.class, OctaneTaskAbridgedImpl.class);
		dtoImplResolver.addMapping(OctaneResultAbridged.class, OctaneResultAbridgedImpl.class);
	}

	@Override
	protected Set<Class<? extends DTOBase>> getJSONAbleDTOs() {
		return dtoPairs.keySet();
	}

	@Override
	protected Class[] getXMLAbleDTOs() {
		return new Class[0];
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
