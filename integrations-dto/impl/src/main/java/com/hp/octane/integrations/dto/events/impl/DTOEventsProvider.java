package com.hp.octane.integrations.dto.events.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventsList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * Events related DTOs definitions provider
 */

public final class DTOEventsProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOEventsProvider() {
		dtoPairs.put(CIEvent.class, CIEventImpl.class);
		dtoPairs.put(CIEventsList.class, CIEventsListImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(CIEvent.class, CIEventImpl.class);
		dtoImplResolver.addMapping(CIEventsList.class, CIEventsListImpl.class);
	}

	@Override
	protected Set<Class<? extends DTOBase>> getJSONAbleDTOs() {
		return dtoPairs.keySet();
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
