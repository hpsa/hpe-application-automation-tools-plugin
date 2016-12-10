package com.hp.octane.integrations.dto.snapshots.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.snapshots.SnapshotPhase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * Snapshots data related DTOs definitions provider
 */

public final class DTOSnapshotsProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOSnapshotsProvider() {
		dtoPairs.put(SnapshotNode.class, SnapshotNodeImpl.class);
		dtoPairs.put(SnapshotPhase.class, SnapshotPhaseImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(SnapshotNode.class, SnapshotNodeImpl.class);
		dtoImplResolver.addMapping(SnapshotPhase.class, SnapshotPhaseImpl.class);
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
