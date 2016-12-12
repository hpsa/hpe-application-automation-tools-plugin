package com.hp.octane.integrations.dto.impl.events;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.api.events.CIEvent;
import com.hp.octane.integrations.dto.api.events.CIEventsList;

/**
 * Created by gullery on 10/02/2016.
 *
 * Events related DTOs definitions provider
 */

public final class DTOEventsProvider extends DTOInternalProviderBase {

	public DTOEventsProvider() {
		dtoPairs.put(CIEvent.class, CIEventImpl.class);
		dtoPairs.put(CIEventsList.class, CIEventsListImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
